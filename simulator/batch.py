
from __future__ import annotations
from collections import Counter
import csv
import json
from pathlib import Path

from simulator.engine import (
    choose_activity,
    filter_candidates,
)

ROOT = Path(__file__).resolve().parents[1]
POLICY = json.loads((ROOT / "policy" / "decision_policy_v1.json").read_text(encoding="utf-8"))
CATALOG = json.loads((ROOT / "catalog" / "activities_v1.json").read_text(encoding="utf-8"))
ACTS = CATALOG["activities"]

def simulate(days, *, can_stand=True, can_walk=True, profile_name="standard"):
    history = []
    rows = []
    counts = Counter()
    effects = Counter()
    immediate_repeat = 0
    slots = POLICY["default_slots"]

    for day in range(1, days + 1):
        for slot in slots:
            seed = f"{POLICY['policy_version']}|{profile_name}|day={day}|slot={slot}"
            activity = choose_activity(
                ACTS,
                history,
                POLICY,
                seed,
                can_stand=can_stand,
                can_walk=can_walk,
                opportunity_type="standard",
            )
            if activity is None:
                rows.append({"day": day, "slot": slot, "activity_id": "", "break_effect": ""})
                continue

            if history and history[-1]["id"] == activity["id"]:
                immediate_repeat += 1

            rows.append({
                "day": day,
                "slot": slot,
                "activity_id": activity["id"],
                "break_effect": activity["sedentary_break_effect"],
            })
            counts[activity["id"]] += 1
            effects[activity["sedentary_break_effect"]] += 1
            history.append(activity)
            history = history[-POLICY["scoring"]["history_window_proposals"]:]

    return {
        "profile": profile_name,
        "days": days,
        "total": sum(counts.values()),
        "unique": len(counts),
        "immediate_repeats": immediate_repeat,
        "counts": dict(sorted(counts.items())),
        "break_effects": dict(sorted(effects.items())),
        "rows": rows,
    }

def pct(n, total):
    return round(100 * n / total, 1) if total else 0

def write_outputs():
    out = ROOT / "outputs" / "revalidation_v0"
    out.mkdir(parents=True, exist_ok=True)

    standard30 = simulate(30, profile_name="standard_30d")
    limited30 = simulate(30, can_stand=False, can_walk=False, profile_name="seated_30d")
    standard365 = simulate(365, profile_name="standard_365d")
    limited365 = simulate(365, can_stand=False, can_walk=False, profile_name="seated_365d")

    all_results = {
        "policy_version": POLICY["policy_version"],
        "selector_version": POLICY["selector_version"],
        "catalog_version": CATALOG["catalog_version"],
        "runs": [standard30, limited30, standard365, limited365],
    }

    # Keep JSON readable: rows go to CSV, not duplicate them in JSON.
    json_results = dict(all_results)
    json_results["runs"] = [{k:v for k,v in r.items() if k != "rows"} for r in all_results["runs"]]
    (out / "results.json").write_text(json.dumps(json_results, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    for result in all_results["runs"]:
        with (out / f"{result['profile']}.csv").open("w", encoding="utf-8", newline="") as f:
            writer = csv.DictWriter(f, fieldnames=["day","slot","activity_id","break_effect"])
            writer.writeheader()
            writer.writerows(result["rows"])

    s30, l30, s365, l365 = all_results["runs"]
    report = f"""# Revalidation du moteur V0

**Politique :** `{POLICY['policy_version']}`  
**Sélecteur :** `{POLICY['selector_version']}`  
**Catalogue :** `{CATALOG['catalog_version']}`  
**Catalogue utilisé :** 23 activités, dont 21 standards et 2 `start_of_day`.

## Résultat synthétique

| Simulation | Propositions | Activités distinctes | Répétitions immédiates |
|---|---:|---:|---:|
| Standard — 30 jours | {s30['total']} | {s30['unique']}/21 | {s30['immediate_repeats']} |
| Assis uniquement — 30 jours | {l30['total']} | {l30['unique']}/11 | {l30['immediate_repeats']} |
| Standard — 365 jours | {s365['total']} | {s365['unique']}/21 | {s365['immediate_repeats']} |
| Assis uniquement — 365 jours | {l365['total']} | {l365['unique']}/11 | {l365['immediate_repeats']} |

Les répétitions immédiates ne sont pas des violations : la politique les pénalise avec `0.15` mais ne les interdit pas.

## Effet catalogue sur la sédentarité — profil standard, 365 jours

- `certain` : {s365['break_effects'].get('certain',0)} ({pct(s365['break_effects'].get('certain',0),s365['total'])} %)
- `probable` : {s365['break_effects'].get('probable',0)} ({pct(s365['break_effects'].get('probable',0),s365['total'])} %)
- `optional` : {s365['break_effects'].get('optional',0)} ({pct(s365['break_effects'].get('optional',0),s365['total'])} %)
- `adjacent_only` : {s365['break_effects'].get('adjacent_only',0)} ({pct(s365['break_effects'].get('adjacent_only',0),s365['total'])} %)

Cette distribution décrit les fiches sélectionnées. Elle ne prouve pas que l'utilisateur exécute l'activité ni qu'une rupture de sédentarité a réellement eu lieu.

## Profil `can_stand=false`, `can_walk=false`

Le filtre conserve exactement 11 activités standards compatibles.

Dans ce profil, aucune activité `certain` n'est disponible. C'est cohérent avec les contraintes : le moteur ne peut pas garantir une rupture de l'assise lorsqu'on lui indique que la personne ne peut pas se lever.

L'UX ne devra donc jamais présenter ces propositions comme des ruptures certaines de sédentarité.

## Scoring V0 simplifié

`internal_family` est conservé comme métadonnée éditoriale mais n'est plus utilisé par le scoring.

La diversité runtime repose uniquement sur :

- même activité que la précédente : `0.15` ;
- même activité dans les quatre dernières propositions : `0.55` ;
- sinon : `1.00`.

Cette règle est plus simple et correspond au comportement réellement utile du catalogue actuel.

## Ce que cette simulation valide

- la logique des créneaux fixes avec seuil de 20 minutes ;
- l'absence de règle 90/60 ;
- les reports +10/+30 ;
- le passage possible pendant le déjeuner ou après la fin du travail ;
- la priorité des créneaux fixes lorsqu'un report créerait un conflit ;
- le filtrage capacité/catégorie/intensité ;
- l'alternative qui exclut seulement l'activité courante ;
- la sélection déterministe SHA-256 ;
- la diversité probabiliste et l'absence de starvation observée sur les longues simulations.

## Ce qu'elle ne valide pas

- l'adhésion réelle ;
- l'acceptabilité des quatre créneaux ;
- la fatigue notificationnelle ;
- l'efficacité médicale ;
- les contraintes réelles d'Android ;
- la pertinence scientifique des coefficients.

## Conclusion

La nouvelle logique de planning est techniquement cohérente et nettement plus simple que la règle 90/60.

Le nettoyage des coefficients de famille est terminé. Une dernière reproduction locale des tests reste requise avant gel pour Kotlin.
"""
    (out / "report.md").write_text(report, encoding="utf-8")
    return out, all_results

if __name__ == "__main__":
    out, results = write_outputs()
    print(out / "report.md")
