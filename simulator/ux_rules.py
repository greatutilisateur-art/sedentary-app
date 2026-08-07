def should_request_notification_permission(selected_workdays, active_slots):
    """Permission is useful only when an automatic proposal can exist."""
    return bool(selected_workdays) and bool(active_slots)

def posture_badge(activity, can_stand):
    if activity.get("standing_required"):
        return "Debout nécessaire"

    if activity.get("preferred_posture") == "standing":
        if can_stand:
            return "Debout conseillé"
        if "seated" in activity.get("compatible_postures", []):
            return "Assis possible"

    return None

ZERO_ELIGIBLE_COPY = "Aucune activité ne correspond actuellement à ces réglages."
