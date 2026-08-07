
from __future__ import annotations
from dataclasses import dataclass
from typing import Iterable, Optional
import hashlib
import math

def to_minutes(value: str | int) -> int:
    if isinstance(value, int):
        return value
    h, m = map(int, value.split(":"))
    return h * 60 + m

def validate_standard_slots(
    slots: Iterable[str | int],
    work_start: str | int,
    lunch_start: str | int,
    lunch_end: str | int,
    work_end: str | int,
    min_gap_minutes: int,
):
    slots_m = sorted(to_minutes(s) for s in slots)
    ws, ls, le, we = map(to_minutes, (work_start, lunch_start, lunch_end, work_end))
    errors = []
    for t in slots_m:
        if t < ws or t > we:
            errors.append(("OUTSIDE_WORK", t))
        if ls <= t < le:
            errors.append(("DURING_LUNCH", t))
    for a, b in zip(slots_m, slots_m[1:]):
        if b - a < min_gap_minutes:
            errors.append(("GAP_TOO_SMALL", (a, b)))
    return errors

def opportunity_allowed(
    target_minute: int,
    actual_minute: int,
    next_fixed_slot_minute: Optional[int],
    lateness_tolerance_minutes: int,
    min_gap_minutes: int,
    *,
    processed: bool = False,
    suspended: bool = False,
):
    if suspended:
        return False, "SUSPENDED"
    if processed:
        return False, "ALREADY_PROCESSED"
    lateness = actual_minute - target_minute
    if lateness < 0:
        return False, "TOO_EARLY"
    if lateness > lateness_tolerance_minutes:
        return False, "MISSED_SLOT"
    if (
        next_fixed_slot_minute is not None
        and actual_minute < next_fixed_slot_minute
        and next_fixed_slot_minute - actual_minute < min_gap_minutes
    ):
        return False, "TOO_CLOSE_TO_NEXT_FIXED_SLOT"
    return True, "ALLOWED"

def allowed_snooze_options(
    action_minute: int,
    fixed_slots: Iterable[int],
    source_slot_minute: int,
    options_minutes: Iterable[int],
    min_gap_minutes: int,
):
    allowed = []
    fixed_slots = list(fixed_slots)
    for delta in options_minutes:
        candidate = action_minute + delta
        # Minute values are local wall-clock minutes in the current civil day.
        # A report may pass lunch/work end but never midnight.
        if candidate >= 24 * 60:
            continue
        conflict = any(
            fixed != source_slot_minute and abs(candidate - fixed) < min_gap_minutes
            for fixed in fixed_slots
        )
        if not conflict:
            allowed.append(delta)
    return allowed

def filter_candidates(
    activities,
    *,
    enabled_categories=None,
    enabled_intensities=None,
    can_stand=True,
    can_walk=True,
    opportunity_type="standard",
    available_seconds=None,
    current_activity_id=None,
    excluded_activity_ids=None,
):
    categories = None if enabled_categories is None else set(enabled_categories)
    intensities = None if enabled_intensities is None else set(enabled_intensities)
    excluded = set(excluded_activity_ids or [])
    if current_activity_id is not None:
        excluded.add(current_activity_id)
    result = []
    for a in activities:
        if a["status"] != "published":
            continue
        if categories is not None and a["user_category"] not in categories:
            continue
        if intensities is not None and a["intensity"] not in intensities:
            continue
        if a["standing_required"] and not can_stand:
            continue
        if a["walking_required"] and not can_walk:
            continue
        if opportunity_type not in a["eligible_opportunity_types"]:
            continue
        if available_seconds is not None and a["duration_seconds_min"] > available_seconds:
            continue
        if a["id"] in excluded:
            continue
        result.append(a)
    return result

def repetition_rule(activity, history, policy):
    coeff = policy["scoring"]["repetition_coefficients"]
    recent = history[-policy["scoring"]["history_window_proposals"]:]
    if not recent:
        return coeff["no_repetition"], "no_repetition"
    previous = recent[-1]
    if activity["id"] == previous["id"]:
        return coeff["same_as_previous_activity"], "same_as_previous_activity"
    if any(activity["id"] == h["id"] for h in recent):
        return coeff["same_activity_in_recent_window"], "same_activity_in_recent_window"
    return coeff["no_repetition"], "no_repetition"

def validate_catalog_for_selection(activities):
    ids = [a["id"] for a in activities]
    if len(ids) != len(set(ids)):
        raise ValueError("duplicate activity id")
    for a in activities:
        w = a["base_weight"]
        if not isinstance(w, (int, float)) or not math.isfinite(w) or w <= 0:
            raise ValueError(f"invalid base_weight for {a['id']}")

def score_candidates(candidates, history, policy):
    validate_catalog_for_selection(candidates)
    lo = policy["scoring"]["min_weight"]
    hi = policy["scoring"]["max_weight"]
    scored = []
    for a in candidates:
        coeff, reason = repetition_rule(a, history, policy)
        weight = max(lo, min(hi, a["base_weight"] * coeff))
        scored.append({"activity": a, "weight": weight, "rule": reason})
    return scored

def deterministic_fraction(seed: str) -> float:
    digest = hashlib.sha256(seed.encode("utf-8")).digest()
    value = int.from_bytes(digest[:8], "big")
    return value / (1 << 64)

def weighted_select(scored, seed: str):
    if not scored:
        return None
    ids = [x["activity"]["id"] for x in scored]
    if len(ids) != len(set(ids)):
        raise ValueError("duplicate activity id")
    for x in scored:
        w = x["weight"]
        if not isinstance(w, (int, float)) or not math.isfinite(w) or w <= 0:
            raise ValueError("invalid selection weight")
    ordered = sorted(scored, key=lambda x: x["activity"]["id"])
    total = sum(x["weight"] for x in ordered)
    cursor = deterministic_fraction(seed) * total
    cumulative = 0.0
    for x in ordered:
        cumulative += x["weight"]
        if cursor < cumulative:
            return x["activity"]
    return ordered[-1]["activity"]

def choose_activity(activities, history, policy, seed, **filter_kwargs):
    candidates = filter_candidates(activities, **filter_kwargs)
    if not candidates:
        return None
    scored = score_candidates(candidates, history, policy)
    return weighted_select(scored, seed)

def apply_capability_change(can_stand: bool, can_walk: bool, field: str, value: bool):
    """Apply the explicit V0 dependency without inferring medical state."""
    if field == "can_stand":
        can_stand = bool(value)
        if not can_stand:
            can_walk = False
    elif field == "can_walk":
        # Walking cannot be enabled while standing is unavailable.
        can_walk = bool(value) and can_stand
    else:
        raise ValueError("unknown capability field")
    return can_stand, can_walk

def next_selected_workday(current_weekday: int, selected_weekdays):
    """
    Monday=0 ... Sunday=6.
    Returns the number of days until the next selected workday, strictly after
    the current day. Returns None when no workday is selected.
    """
    selected = set(selected_weekdays)
    if not selected:
        return None
    for delta in range(1, 8):
        if (current_weekday + delta) % 7 in selected:
            return delta
    return None

def schedule_has_opportunities(selected_weekdays, active_slots):
    """Derived V0 condition; not a new persisted suspension state."""
    return bool(selected_weekdays) and bool(active_slots)

def validate_same_day_work_window(work_start, work_end):
    ws = to_minutes(work_start)
    we = to_minutes(work_end)
    return ws < we
