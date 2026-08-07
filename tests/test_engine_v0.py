
import copy
import json
import unittest
from pathlib import Path

from simulator.ux_rules import ZERO_ELIGIBLE_COPY, posture_badge, should_request_notification_permission
from simulator.engine import (
    allowed_snooze_options,
    apply_capability_change,
    choose_activity,
    filter_candidates,
    next_selected_workday,
    opportunity_allowed,
    repetition_rule,
    schedule_has_opportunities,
    score_candidates,
    to_minutes,
    validate_catalog_for_selection,
    validate_same_day_work_window,
    validate_standard_slots,
    weighted_select,
)

ROOT = Path(__file__).resolve().parents[1]
POLICY = json.loads((ROOT / "policy" / "decision_policy_v1.json").read_text(encoding="utf-8"))
CATALOG = json.loads((ROOT / "catalog" / "activities_v1.json").read_text(encoding="utf-8"))
ACTS = CATALOG["activities"]
MIN_GAP = POLICY["scheduling"]["minimum_gap_between_distinct_opportunities_minutes"]

class SlotConfigTests(unittest.TestCase):
    def test_c01_exact_20_valid(self):
        self.assertEqual([], validate_standard_slots(["10:10","10:30"],"08:30","12:30","13:30","17:45",MIN_GAP))
    def test_c02_15_invalid(self):
        errs=validate_standard_slots(["10:10","10:25"],"08:30","12:30","13:30","17:45",MIN_GAP)
        self.assertTrue(any(e[0]=="GAP_TOO_SMALL" for e in errs))
    def test_c03_lunch_invalid(self):
        errs=validate_standard_slots(["12:45"],"08:30","12:30","13:30","17:45",MIN_GAP)
        self.assertTrue(any(e[0]=="DURING_LUNCH" for e in errs))
    def test_c04_outside_work_invalid(self):
        errs=validate_standard_slots(["18:00"],"08:30","12:30","13:30","17:45",MIN_GAP)
        self.assertTrue(any(e[0]=="OUTSIDE_WORK" for e in errs))
    def test_c05_end_exact_valid(self):
        self.assertEqual([],validate_standard_slots(["17:45"],"08:30","12:30","13:30","17:45",MIN_GAP))
    def test_c06_defaults_valid(self):
        self.assertEqual([],validate_standard_slots(POLICY["default_slots"],"08:30","12:30","13:30","17:45",MIN_GAP))
    def test_c07_start_20_before_slot_valid(self):
        self.assertEqual([],validate_standard_slots(["09:50","10:10"],"08:30","12:30","13:30","17:45",MIN_GAP))
    def test_c08_start_15_before_slot_invalid(self):
        errs=validate_standard_slots(["09:55","10:10"],"08:30","12:30","13:30","17:45",MIN_GAP)
        self.assertTrue(any(e[0]=="GAP_TOO_SMALL" for e in errs))

class OpportunityTests(unittest.TestCase):
    def test_o01_on_time(self):
        self.assertTrue(opportunity_allowed(to_minutes("10:10"),to_minutes("10:10"),to_minutes("11:50"),15,MIN_GAP)[0])
    def test_o02_processed(self):
        self.assertFalse(opportunity_allowed(610,610,710,15,MIN_GAP,processed=True)[0])
    def test_o03_late_12_allowed(self):
        self.assertTrue(opportunity_allowed(610,622,710,15,MIN_GAP)[0])
    def test_o04_late_20_denied(self):
        self.assertEqual("MISSED_SLOT",opportunity_allowed(610,630,710,15,MIN_GAP)[1])
    def test_o06_idempotent_pure_result(self):
        a=opportunity_allowed(610,622,710,15,MIN_GAP)
        b=opportunity_allowed(610,622,710,15,MIN_GAP)
        self.assertEqual(a,b)
    def test_o07_late_too_close_to_next_fixed(self):
        allowed,reason=opportunity_allowed(to_minutes("10:10"),to_minutes("10:22"),to_minutes("10:35"),15,MIN_GAP)
        self.assertFalse(allowed)
        self.assertEqual("TOO_CLOSE_TO_NEXT_FIXED_SLOT",reason)
    def test_o08_suspended(self):
        self.assertEqual("SUSPENDED",opportunity_allowed(610,610,710,15,MIN_GAP,suspended=True)[1])

class SnoozeTests(unittest.TestCase):
    def test_r01_plus10_allowed(self):
        fixed=list(map(to_minutes,["10:10","11:50","15:10","17:00"]))
        self.assertIn(10,allowed_snooze_options(to_minutes("10:10"),fixed,to_minutes("10:10"),[10,30],MIN_GAP))
    def test_r02_plus30_allowed(self):
        fixed=list(map(to_minutes,["10:10","11:50","15:10","17:00"]))
        self.assertIn(30,allowed_snooze_options(to_minutes("10:10"),fixed,to_minutes("10:10"),[10,30],MIN_GAP))
    def test_r03_cross_lunch_allowed(self):
        fixed=list(map(to_minutes,["11:50","15:10","17:00"]))
        self.assertIn(30,allowed_snooze_options(to_minutes("11:50"),fixed,to_minutes("11:50"),[30],MIN_GAP))
    def test_r05_after_work_allowed(self):
        fixed=list(map(to_minutes,["10:10","11:50","15:10","17:00"]))
        self.assertIn(30,allowed_snooze_options(to_minutes("17:00"),fixed,to_minutes("17:00"),[30],MIN_GAP))
    def test_r06_conflicting_plus10_hidden(self):
        fixed=list(map(to_minutes,["10:10","10:50"]))
        self.assertNotIn(10,allowed_snooze_options(to_minutes("10:30"),fixed,to_minutes("10:10"),[10,30],MIN_GAP))
    def test_r07_exact20_allowed(self):
        fixed=list(map(to_minutes,["10:10","10:50"]))
        self.assertIn(10,allowed_snooze_options(to_minutes("10:20"),fixed,to_minutes("10:10"),[10],MIN_GAP))
    def test_report_after_fixed_too_close_also_hidden(self):
        fixed=list(map(to_minutes,["10:10","10:30"]))
        # Source 10:10 shown at 10:05 cannot happen in production, but property is symmetric:
        # candidate 10:45 is 15 min after 10:30 and therefore conflicts.
        self.assertNotIn(40,allowed_snooze_options(to_minutes("10:05"),fixed,to_minutes("10:10"),[40],MIN_GAP))

class FilterTests(unittest.TestCase):
    def test_f01_category_disabled(self):
        c=filter_candidates(ACTS,enabled_categories={"mobilite"},opportunity_type="standard")
        self.assertTrue(c)
        self.assertTrue(all(a["user_category"]=="mobilite" for a in c))
    def test_f02_intensity_disabled(self):
        c=filter_candidates(ACTS,enabled_intensities={"active"},opportunity_type="standard")
        self.assertTrue(c)
        self.assertTrue(all(a["intensity"]=="active" for a in c))
    def test_f03_cannot_stand(self):
        c=filter_candidates(ACTS,can_stand=False,can_walk=False,opportunity_type="standard")
        self.assertTrue(all(not a["standing_required"] for a in c))
        self.assertEqual(11,len(c))
    def test_f04_preferred_standing_not_blocking_is_encoded_as_not_required(self):
        mob02=next(a for a in ACTS if a["id"]=="MOB02")
        self.assertFalse(mob02["standing_required"])
        ids={a["id"] for a in filter_candidates(ACTS,can_stand=False,can_walk=False,opportunity_type="standard")}
        self.assertIn("MOB02",ids)
    def test_f05_cannot_walk(self):
        c=filter_candidates(ACTS,can_walk=False,opportunity_type="standard")
        self.assertTrue(all(not a["walking_required"] for a in c))
    def test_f06_duration(self):
        c=filter_candidates(ACTS,available_seconds=30,opportunity_type="standard")
        self.assertTrue(c)
        self.assertTrue(all(a["duration_seconds_min"]<=30 for a in c))
        self.assertEqual([],filter_candidates(ACTS,available_seconds=20,opportunity_type="standard"))
    def test_f07_start_only(self):
        ids={a["id"] for a in filter_candidates(ACTS,opportunity_type="start_of_day")}
        self.assertEqual({"START01","START02"},ids)
    def test_f08_start_excluded_standard(self):
        ids={a["id"] for a in filter_candidates(ACTS,opportunity_type="standard")}
        self.assertNotIn("START01",ids)
        self.assertNotIn("START02",ids)
        self.assertEqual(21,len(ids))
    def test_f09_alternative_excludes_current_only(self):
        ids={a["id"] for a in filter_candidates(ACTS,opportunity_type="standard",current_activity_id="MOUV01")}
        self.assertNotIn("MOUV01",ids)
        self.assertIn("MOUV02",ids)
    def test_f10_previous_activity_can_return(self):
        ids={a["id"] for a in filter_candidates(ACTS,opportunity_type="standard",current_activity_id="MOUV02")}
        self.assertIn("MOUV01",ids)
    def test_f11_no_candidate(self):
        c=filter_candidates(ACTS,enabled_categories=set(),opportunity_type="standard")
        self.assertEqual([],c)

class ScoringTests(unittest.TestCase):
    def a(self,id): return next(a for a in ACTS if a["id"]==id)
    def synthetic(self,id,family,base=4):
        return {"id":id,"status":"published","user_category":"x","internal_family":family,
                "intensity":"gentle","duration_seconds_min":30,"duration_seconds_max":30,
                "standing_required":False,"walking_required":False,
                "eligible_opportunity_types":["standard"],"base_weight":base,
                "sedentary_break_effect":"optional","evidence_level":"limited"}
    def test_s01_same_activity_previous(self):
        a=self.a("MOB01")
        self.assertEqual(0.15,repetition_rule(a,[a],POLICY)[0])
    def test_s02_same_family_has_no_runtime_effect(self):
        a=self.synthetic("A","fam")
        b=self.synthetic("B","fam")
        self.assertEqual(1.0,repetition_rule(a,[b],POLICY)[0])
    def test_s03_same_activity_recent(self):
        a=self.a("MOB01"); other=self.a("VIS01")
        self.assertEqual(0.55,repetition_rule(a,[a,other],POLICY)[0])
    def test_s04_family_twice_recent_still_has_no_effect(self):
        a=self.synthetic("A","fam")
        h1=self.synthetic("B","fam"); h2=self.synthetic("C","fam"); other=self.synthetic("D","other")
        self.assertEqual(1.0,repetition_rule(a,[h1,h2,other],POLICY)[0])
    def test_s05_none(self):
        self.assertEqual(1.0,repetition_rule(self.a("MOB01"),[self.a("VIS01")],POLICY)[0])
    def test_s06_hierarchy_no_cumulative(self):
        a=self.a("MOB01")
        coeff,_=repetition_rule(a,[a,a],POLICY)
        self.assertEqual(0.15,coeff)
    def test_s08_evidence_not_read(self):
        a=copy.deepcopy(self.a("MOB01"))
        b=copy.deepcopy(a); b["evidence_level"]="high"
        self.assertEqual(score_candidates([a],[],POLICY)[0]["weight"],
                         score_candidates([b],[],POLICY)[0]["weight"])
    def test_s09_min_clamp(self):
        a=self.synthetic("A","f",base=0.1)
        hist=[copy.deepcopy(a)]
        self.assertEqual(0.05,score_candidates([a],hist,POLICY)[0]["weight"])
    def test_s10_max_clamp(self):
        a=self.synthetic("A","f",base=100)
        self.assertEqual(20.0,score_candidates([a],[],POLICY)[0]["weight"])
    def test_internal_family_coefficients_absent_from_policy(self):
        coeff=POLICY["scoring"]["repetition_coefficients"]
        self.assertNotIn("same_as_previous_family",coeff)
        self.assertNotIn("family_at_least_twice_in_recent_window",coeff)
        self.assertFalse(POLICY["scoring"]["use_internal_family_in_v0"])

class SelectorTests(unittest.TestCase):
    def scored(self,ids):
        return [{"activity":next(a for a in ACTS if a["id"]==i),"weight":1.0,"rule":"x"} for i in ids]
    def test_w01_same_seed(self):
        s=self.scored(["MOB01","VIS01","DYN01"])
        self.assertEqual(weighted_select(s,"seed")["id"],weighted_select(s,"seed")["id"])
    def test_w02_order_independent(self):
        s=self.scored(["MOB01","VIS01","DYN01"])
        self.assertEqual(weighted_select(s,"seed")["id"],weighted_select(list(reversed(s)),"seed")["id"])
    def test_w03_single(self):
        self.assertEqual("MOB01",weighted_select(self.scored(["MOB01"]),"x")["id"])
    def test_w04_zero_invalid(self):
        s=self.scored(["MOB01"]); s[0]["weight"]=0
        with self.assertRaises(ValueError): weighted_select(s,"x")
    def test_w05_negative_invalid(self):
        s=self.scored(["MOB01"]); s[0]["weight"]=-1
        with self.assertRaises(ValueError): weighted_select(s,"x")
    def test_w06_duplicate_invalid(self):
        s=self.scored(["MOB01","MOB01"])
        with self.assertRaises(ValueError): weighted_select(s,"x")
    def test_w07_alt_current_removed(self):
        c=filter_candidates(ACTS,opportunity_type="standard",current_activity_id="MOB01")
        a=choose_activity(c,[],POLICY,"alt",opportunity_type="standard")
        self.assertNotEqual("MOB01",a["id"])
    def test_w09_no_other_candidate(self):
        only=[next(a for a in ACTS if a["id"]=="MOB01")]
        c=filter_candidates(only,opportunity_type="standard",current_activity_id="MOB01")
        self.assertEqual([],c)

class CatalogTests(unittest.TestCase):
    def test_23_activities(self):
        self.assertEqual(23,len(ACTS))
    def test_21_standard(self):
        self.assertEqual(21,len([a for a in ACTS if "standard" in a["eligible_opportunity_types"]]))
    def test_ids_unique(self):
        self.assertEqual(len(ACTS),len({a["id"] for a in ACTS}))
    def test_duration_invariant(self):
        self.assertTrue(all(30 <= a["duration_seconds_min"] <= a["duration_seconds_max"] <= 300 for a in ACTS))
    def test_walking_implies_standing(self):
        self.assertTrue(all((not a["walking_required"]) or a["standing_required"] for a in ACTS))
    def test_weights_positive(self):
        validate_catalog_for_selection(ACTS)
    def test_audit_content_revisions(self):
        self.assertEqual("Un petit tour par les escaliers ?",next(a for a in ACTS if a["id"]=="MOUV03")["title"])
        self.assertEqual(300,next(a for a in ACTS if a["id"]=="MOUV04")["duration_seconds_max"])
        self.assertEqual("certain",next(a for a in ACTS if a["id"]=="ETIR01")["sedentary_break_effect"])
        self.assertEqual("certain",next(a for a in ACTS if a["id"]=="ETIR02")["sedentary_break_effect"])
    def test_recommended_standing_remains_non_blocking(self):
        mob03=next(a for a in ACTS if a["id"]=="MOB03")
        self.assertFalse(mob03["standing_required"])
        self.assertEqual("standing",mob03["preferred_posture"])
        self.assertEqual("probable",mob03["sedentary_break_effect"])


class FlexibleConfigurationTests(unittest.TestCase):
    def test_one_active_slot_is_valid(self):
        self.assertEqual([], validate_standard_slots(["10:10"], "08:30", "12:30", "13:30", "17:30", MIN_GAP))

    def test_two_active_slots_are_valid(self):
        self.assertEqual([], validate_standard_slots(["10:10", "15:10"], "08:30", "12:30", "13:30", "17:30", MIN_GAP))

    def test_empty_workdays_create_no_opportunities(self):
        self.assertFalse(schedule_has_opportunities([], ["10:10"]))

    def test_workday_without_slots_creates_no_opportunities(self):
        self.assertFalse(schedule_has_opportunities([0,1,2,3,4], []))

    def test_next_selected_workday_weekend(self):
        self.assertEqual(3, next_selected_workday(4, {0,1,2,3,4}))

    def test_next_selected_workday_sparse_schedule(self):
        self.assertEqual(3, next_selected_workday(4, {0,3}))

    def test_no_selected_workday_has_no_next_day(self):
        self.assertIsNone(next_selected_workday(2, set()))

class CapabilityDependencyTests(unittest.TestCase):
    def test_disabling_standing_disables_walking(self):
        self.assertEqual((False, False), apply_capability_change(True, True, "can_stand", False))

    def test_reenabling_standing_does_not_reenable_walking(self):
        self.assertEqual((True, False), apply_capability_change(False, False, "can_stand", True))

    def test_walking_cannot_enable_when_standing_false(self):
        self.assertEqual((False, False), apply_capability_change(False, False, "can_walk", True))

class LocalTimePolicyTests(unittest.TestCase):
    def test_same_day_work_window(self):
        self.assertTrue(validate_same_day_work_window("09:00", "17:30"))

    def test_overnight_work_window_not_supported(self):
        self.assertFalse(validate_same_day_work_window("22:00", "06:00"))

    def test_policy_uses_local_wall_clock(self):
        self.assertTrue(POLICY["local_time"]["configured_slots_are_wall_clock_local_time"])

    def test_policy_future_only_on_timezone_change(self):
        self.assertTrue(POLICY["local_time"]["recompute_future_opportunities_after_timezone_change"])
        self.assertTrue(POLICY["local_time"]["never_duplicate_already_processed_slot"])

class ReactivationPolicyTests(unittest.TestCase):
    def test_manual_suspension_only(self):
        self.assertEqual("manual_until_resumed", POLICY["suspension"]["mode"])
        self.assertFalse(POLICY["suspension"]["automatic_resume_next_workday"])

    def test_all_reactivation_paths_are_future_only(self):
        r = POLICY["reactivation"]
        self.assertTrue(r["future_opportunities_only"])
        self.assertTrue(r["no_catch_up_after_notification_permission_restore"])
        self.assertTrue(r["no_catch_up_after_manual_resume"])
        self.assertTrue(r["no_catch_up_after_onboarding_completion"])
        self.assertTrue(r["no_catch_up_after_device_unavailable"])

    def test_eligibility_change_cancels_snooze(self):
        self.assertTrue(POLICY["settings_changes"]["cancel_active_snooze_on_eligibility_change"])

    def test_visible_notification_not_rewritten(self):
        self.assertTrue(POLICY["settings_changes"]["already_visible_notification_is_not_retroactively_rewritten"])
        self.assertTrue(POLICY["settings_changes"]["alternative_uses_latest_settings"])

    def test_snooze_cannot_cross_midnight(self):
        self.assertEqual([], allowed_snooze_options(
            to_minutes("23:50"),
            [to_minutes("17:00")],
            to_minutes("17:00"),
            [30],
            MIN_GAP
        ))


class OnboardingPolishTests(unittest.TestCase):
    def activity(self, activity_id):
        return next(a for a in ACTS if a["id"] == activity_id)

    def test_no_workday_does_not_request_notification_permission(self):
        self.assertFalse(should_request_notification_permission([], ["10:10", "11:50"]))

    def test_workday_and_active_slot_request_notification_permission(self):
        self.assertTrue(should_request_notification_permission([0], ["10:10"]))

    def test_no_active_slot_does_not_request_permission(self):
        self.assertFalse(should_request_notification_permission([0], []))

    def test_required_standing_badge(self):
        self.assertEqual("Debout nécessaire", posture_badge(self.activity("ETIR01"), False))

    def test_recommended_standing_badge(self):
        self.assertEqual("Debout conseillé", posture_badge(self.activity("MOB02"), True))

    def test_seated_possible_badge(self):
        self.assertEqual("Assis possible", posture_badge(self.activity("MOB02"), False))

    def test_neutral_instruction_does_not_repeat_standing_recommendation(self):
        for activity in ACTS:
            self.assertNotIn("Si tu peux te lever, c’est mieux", activity["instruction"])

    def test_zero_eligible_copy_is_exact(self):
        self.assertEqual(
            "Aucune activité ne correspond actuellement à ces réglages.",
            ZERO_ELIGIBLE_COPY,
        )

    def test_empty_categories_are_allowed_and_produce_no_candidates(self):
        self.assertEqual([], filter_candidates(ACTS, enabled_categories=set(), opportunity_type="standard"))

    def test_empty_intensities_are_allowed_and_produce_no_candidates(self):
        self.assertEqual([], filter_candidates(ACTS, enabled_intensities=set(), opportunity_type="standard"))

class AlternativeInteractionTests(unittest.TestCase):
    def test_policy_excludes_all_seen_in_interaction(self):
        alt = POLICY["alternative"]
        self.assertFalse(alt["exclude_current_activity_only"])
        self.assertTrue(alt["exclude_all_activities_seen_in_current_interaction"])
        self.assertFalse(alt["allow_activities_seen_earlier_in_interaction"])

    def test_a_b_c_no_return(self):
        ids={a["id"] for a in filter_candidates(ACTS, opportunity_type="standard", excluded_activity_ids={"MOUV01","MOUV02"})}
        self.assertNotIn("MOUV01", ids)
        self.assertNotIn("MOUV02", ids)
        self.assertIn("MOUV03", ids)

    def test_exclusions_reset_for_new_interaction(self):
        ids={a["id"] for a in filter_candidates(ACTS, opportunity_type="standard")}
        self.assertIn("MOUV01", ids)
        self.assertIn("MOUV02", ids)

    def test_no_alternative_when_all_compatible_seen(self):
        compatible=filter_candidates(ACTS, enabled_categories={"visuel"}, opportunity_type="standard")
        seen={a["id"] for a in compatible}
        remaining=filter_candidates(ACTS, enabled_categories={"visuel"}, opportunity_type="standard", excluded_activity_ids=seen)
        self.assertEqual([], remaining)

if __name__ == "__main__":
    unittest.main(verbosity=2)
