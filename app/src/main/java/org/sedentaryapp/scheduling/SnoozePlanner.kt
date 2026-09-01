package org.sedentaryapp.scheduling

import java.time.Duration
import java.time.LocalDateTime
import org.sedentaryapp.data.NextProposalOverride
import org.sedentaryapp.data.ScheduleSettings

/**
 * Décide si un report peut être proposé sans créer deux opportunités
 * séparées de moins que le minimum configuré.
 *
 * Le report remplace la proposition courante : la clé de cette proposition
 * est donc exclue des conflits. La fonction est pure et ne fait aucun accès
 * disque / DataStore.
 */
object SnoozePlanner {

    fun canSchedule(
        target: LocalDateTime,
        currentProposalKey: String,
        now: LocalDateTime,
        settings: ScheduleSettings,
        consumedEarlyOpportunityKeys: Set<String>,
        nextProposalOverride: NextProposalOverride?,
        minimumGapMinutes: Long,
    ): Boolean {

        require(minimumGapMinutes > 0L)

        if (!target.isAfter(now)) {
            return false
        }

        val overrideAt =
            nextProposalOverride
                ?.at
                ?.takeIf { it.isAfter(now) }

        val candidates =
            buildList {

                if (
                    overrideAt != null &&
                    overrideAt.toString() != currentProposalKey
                ) {
                    add(overrideAt)
                }

                val firstDate =
                    target.toLocalDate()
                        .minusDays(1)

                repeat(3) { dayOffset ->
                    val date =
                        firstDate.plusDays(
                            dayOffset.toLong()
                        )

                    if (
                        date.dayOfWeek !in
                        settings.selectedWeekdays
                    ) {
                        return@repeat
                    }

                    settings.slots
                        .forEach { slot ->
                            val opportunity =
                                LocalDateTime.of(
                                    date,
                                    slot.time,
                                )

                            val opportunityKey =
                                opportunity.toString()

                            if (
                                !opportunity.isAfter(now) ||
                                opportunityKey == currentProposalKey ||
                                opportunityKey in
                                    consumedEarlyOpportunityKeys
                            ) {
                                return@forEach
                            }

                            // Une modification ponctuelle de la prochaine
                            // proposition remplace les créneaux fixes qui la
                            // précèdent. Les créneaux suivants restent normaux.
                            if (
                                overrideAt != null &&
                                !opportunity.isAfter(overrideAt)
                            ) {
                                return@forEach
                            }

                            add(opportunity)
                        }
                }
            }

        val minimumGapSeconds =
            minimumGapMinutes * 60L

        return candidates.none { candidate ->
            kotlin.math.abs(
                Duration.between(
                    target,
                    candidate,
                ).seconds
            ) < minimumGapSeconds
        }
    }
}
