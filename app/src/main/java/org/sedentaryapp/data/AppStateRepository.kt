package org.sedentaryapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.stringSetPreferencesKey
import java.io.IOException
import java.time.DayOfWeek
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.appStateDataStore:
        DataStore<Preferences> by preferencesDataStore(
    name = "app_state"
)

data class StandardSlotSetting(
    val index: Int,
    val enabled: Boolean,
    val time: LocalTime,
)

data class ScheduleSettings(
    val selectedWeekdays: Set<DayOfWeek>,
    val workStart: LocalTime,
    val workEnd: LocalTime,
    val lunchEnabled: Boolean,
    val lunchStart: LocalTime,
    val lunchEnd: LocalTime,
    val slots: List<StandardSlotSetting>,
)

class AppStateRepository(
    context: Context,
    defaultSlotTimes: List<LocalTime>,
) {

    private val appContext =
        context.applicationContext

    private val slotDefaults =
        defaultSlotTimes.also {
            require(
                it.isNotEmpty() &&
                        it.size <= MAX_SLOTS
            ) {
                "Default slots must contain between 1 and $MAX_SLOTS entries."
            }
        }
            .distinct()
            .sorted()

    val defaultScheduleSettings =
        ScheduleSettings(
            selectedWeekdays =
                DEFAULT_WEEKDAYS,
            workStart =
                DEFAULT_WORK_START,
            workEnd =
                DEFAULT_WORK_END,
            lunchEnabled = true,
            lunchStart =
                DEFAULT_LUNCH_START,
            lunchEnd =
                DEFAULT_LUNCH_END,
            slots =
                toSlotSettings(
                    slotDefaults
                ),
        )

    val lastActivityId:
            Flow<String?> =
        appContext.appStateDataStore.data
            .safePreferences()
            .map { preferences ->
                preferences[
                    LAST_ACTIVITY_ID
                ]
            }

    val scheduleSettings:
            Flow<ScheduleSettings> =
        appContext.appStateDataStore.data
            .safePreferences()
            .map { preferences ->

                val weekdays =
                    if (
                        preferences.contains(
                            SELECTED_WEEKDAYS
                        )
                    ) {
                        preferences[
                            SELECTED_WEEKDAYS
                        ]
                            ?.mapNotNull { value ->
                                runCatching {
                                    DayOfWeek.valueOf(
                                        value
                                    )
                                }.getOrNull()
                            }
                            ?.toSet()
                            ?: emptySet()
                    } else {
                        DEFAULT_WEEKDAYS
                    }

                val workStart =
                    parseTime(
                        preferences[
                            WORK_START
                        ],
                        DEFAULT_WORK_START,
                    )

                val workEnd =
                    parseTime(
                        preferences[
                            WORK_END
                        ],
                        DEFAULT_WORK_END,
                    )

                val lunchEnabled =
                    preferences[
                        LUNCH_ENABLED
                    ] ?: true

                val lunchStart =
                    parseTime(
                        preferences[
                            LUNCH_START
                        ],
                        DEFAULT_LUNCH_START,
                    )

                val lunchEnd =
                    parseTime(
                        preferences[
                            LUNCH_END
                        ],
                        DEFAULT_LUNCH_END,
                    )

                val slotTimes =
                    readSlotTimes(
                        preferences
                    )

                ScheduleSettings(
                    selectedWeekdays =
                        weekdays,
                    workStart =
                        workStart,
                    workEnd =
                        workEnd,
                    lunchEnabled =
                        lunchEnabled,
                    lunchStart =
                        lunchStart,
                    lunchEnd =
                        lunchEnd,
                    slots =
                        toSlotSettings(
                            slotTimes
                        ),
                )
            }

    suspend fun saveScheduleSettings(
        settings: ScheduleSettings,
    ) {
        appContext.appStateDataStore.edit {
                preferences,
            ->

            preferences[
                SELECTED_WEEKDAYS
            ] =
                settings.selectedWeekdays
                    .map { it.name }
                    .toSet()

            preferences[
                WORK_START
            ] =
                settings.workStart
                    .toString()

            preferences[
                WORK_END
            ] =
                settings.workEnd
                    .toString()

            preferences[
                LUNCH_ENABLED
            ] =
                settings.lunchEnabled

            preferences[
                LUNCH_START
            ] =
                settings.lunchStart
                    .toString()

            preferences[
                LUNCH_END
            ] =
                settings.lunchEnd
                    .toString()

            writeSlotTimes(
                preferences = preferences,
                times =
                    settings.slots
                        .filter {
                            it.enabled
                        }
                        .map {
                            it.time
                        },
            )
        }
    }

    suspend fun saveLastActivityId(
        activityId: String,
    ) {
        appContext.appStateDataStore.edit {
                preferences,
            ->
            preferences[
                LAST_ACTIVITY_ID
            ] = activityId
        }
    }
    suspend fun clearLastActivityId() {
        appContext.appStateDataStore.edit { preferences ->
            preferences.remove(
                LAST_ACTIVITY_ID
            )
        }
    }
    suspend fun setWeekdayEnabled(
        day: DayOfWeek,
        enabled: Boolean,
    ) {
        appContext.appStateDataStore.edit {
                preferences,
            ->

            val days =
                if (
                    preferences.contains(
                        SELECTED_WEEKDAYS
                    )
                ) {
                    preferences[
                        SELECTED_WEEKDAYS
                    ]
                        ?.toMutableSet()
                        ?: mutableSetOf()
                } else {
                    DEFAULT_WEEKDAYS
                        .map { it.name }
                        .toMutableSet()
                }

            if (enabled) {
                days.add(
                    day.name
                )
            } else {
                days.remove(
                    day.name
                )
            }

            preferences[
                SELECTED_WEEKDAYS
            ] = days
        }
    }

    suspend fun setWorkStart(
        time: LocalTime,
    ) {
        saveTime(
            WORK_START,
            time,
        )
    }

    suspend fun setWorkEnd(
        time: LocalTime,
    ) {
        saveTime(
            WORK_END,
            time,
        )
    }

    suspend fun setLunchEnabled(
        enabled: Boolean,
    ) {
        appContext.appStateDataStore.edit {
                preferences,
            ->
            preferences[
                LUNCH_ENABLED
            ] = enabled
        }
    }

    suspend fun setLunchStart(
        time: LocalTime,
    ) {
        saveTime(
            LUNCH_START,
            time,
        )
    }

    suspend fun setLunchEnd(
        time: LocalTime,
    ) {
        saveTime(
            LUNCH_END,
            time,
        )
    }

    suspend fun setSlotEnabled(
        index: Int,
        enabled: Boolean,
    ) {
        appContext.appStateDataStore.edit {
                preferences,
            ->

            val times =
                readSlotTimes(
                    preferences
                ).toMutableList()

            require(
                index in times.indices
            )

            if (!enabled) {
                if (
                    times.size <=
                    MIN_SLOTS
                ) {
                    return@edit
                }

                times.removeAt(
                    index
                )
            }

            writeSlotTimes(
                preferences =
                    preferences,
                times = times,
            )
        }
    }

    suspend fun setSlotTime(
        index: Int,
        time: LocalTime,
    ) {
        appContext.appStateDataStore.edit {
                preferences,
            ->

            val times =
                readSlotTimes(
                    preferences
                ).toMutableList()

            require(
                index in times.indices
            )

            times[index] =
                time

            writeSlotTimes(
                preferences =
                    preferences,
                times = times,
            )
        }
    }

    suspend fun addSlot(
        time: LocalTime,
    ): Boolean {

        var added = false

        appContext.appStateDataStore.edit {
                preferences,
            ->

            val times =
                readSlotTimes(
                    preferences
                ).toMutableList()

            if (
                times.size >= MAX_SLOTS ||
                time in times
            ) {
                return@edit
            }

            times.add(
                time
            )

            writeSlotTimes(
                preferences =
                    preferences,
                times = times,
            )

            added = true
        }

        return added
    }

    suspend fun removeSlot(
        index: Int,
    ): Boolean {

        var removed = false

        appContext.appStateDataStore.edit {
                preferences,
            ->

            val times =
                readSlotTimes(
                    preferences
                ).toMutableList()

            if (
                times.size <= MIN_SLOTS ||
                index !in times.indices
            ) {
                return@edit
            }

            times.removeAt(
                index
            )

            writeSlotTimes(
                preferences =
                    preferences,
                times = times,
            )

            removed = true
        }

        return removed
    }

    private fun readSlotTimes(
        preferences: Preferences,
    ): List<LocalTime> {

        val newValue =
            preferences[
                SCHEDULE_SLOTS
            ]

        if (newValue != null) {
            val parsed =
                parseSlotList(
                    newValue
                )

            if (parsed.isNotEmpty()) {
                return parsed
            }
        }

        val legacyExists =
            LEGACY_SLOT_ENABLED_KEYS
                .any {
                    preferences
                        .contains(it)
                } ||
                    LEGACY_SLOT_TIME_KEYS
                        .any {
                            preferences
                                .contains(it)
                        }

        if (!legacyExists) {
            return slotDefaults
        }

        val legacyTimes =
            LEGACY_DEFAULT_SLOTS
                .mapIndexedNotNull {
                        index,
                        defaultTime,
                    ->

                    val enabled =
                        preferences[
                            LEGACY_SLOT_ENABLED_KEYS[
                                index
                            ]
                        ] ?: true

                    if (!enabled) {
                        return@mapIndexedNotNull null
                    }

                    parseTime(
                        preferences[
                            LEGACY_SLOT_TIME_KEYS[
                                index
                            ]
                        ],
                        defaultTime,
                    )
                }
                .distinct()
                .sorted()

        return if (
            legacyTimes.isEmpty()
        ) {
            slotDefaults
        } else {
            legacyTimes
        }
    }

    private fun writeSlotTimes(
        preferences:
        androidx.datastore.preferences.core.MutablePreferences,
        times: List<LocalTime>,
    ) {
        val normalized =
            times
                .distinct()
                .sorted()
                .take(
                    MAX_SLOTS
                )

        if (
            normalized.isEmpty()
        ) {
            return
        }

        preferences[
            SCHEDULE_SLOTS
        ] =
            normalized.joinToString(
                separator = "|"
            ) {
                it.toString()
            }
    }

    private fun parseSlotList(
        value: String,
    ): List<LocalTime> {
        return value
            .split("|")
            .mapNotNull {
                runCatching {
                    LocalTime.parse(
                        it
                    )
                }.getOrNull()
            }
            .distinct()
            .sorted()
            .take(
                MAX_SLOTS
            )
    }

    private fun toSlotSettings(
        times: List<LocalTime>,
    ): List<StandardSlotSetting> {
        return times
            .sorted()
            .mapIndexed {
                    index,
                    time,
                ->
                StandardSlotSetting(
                    index = index,
                    enabled = true,
                    time = time,
                )
            }
    }

    private suspend fun saveTime(
        key:
        Preferences.Key<String>,
        time: LocalTime,
    ) {
        appContext.appStateDataStore.edit {
                preferences,
            ->
            preferences[
                key
            ] =
                time.toString()
        }
    }

    private fun parseTime(
        value: String?,
        default: LocalTime,
    ): LocalTime {
        return value
            ?.let {
                runCatching {
                    LocalTime.parse(
                        it
                    )
                }.getOrNull()
            }
            ?: default
    }

    private fun Flow<Preferences>
            .safePreferences():
            Flow<Preferences> {

        return catch {
                exception,
            ->

            if (
                exception is
                        IOException
            ) {
                emit(
                    emptyPreferences()
                )
            } else {
                throw exception
            }
        }
    }

    private companion object {

        const val MIN_SLOTS = 1
        const val MAX_SLOTS = 6

        val DEFAULT_WEEKDAYS =
            setOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
            )

        val DEFAULT_WORK_START =
            LocalTime.of(
                8,
                30,
            )

        val DEFAULT_WORK_END =
            LocalTime.of(
                17,
                30,
            )

        val DEFAULT_LUNCH_START =
            LocalTime.of(
                12,
                30,
            )

        val DEFAULT_LUNCH_END =
            LocalTime.of(
                13,
                30,
            )

        val LEGACY_DEFAULT_SLOTS =
            listOf(
                LocalTime.of(
                    10,
                    10,
                ),
                LocalTime.of(
                    11,
                    50,
                ),
                LocalTime.of(
                    15,
                    10,
                ),
                LocalTime.of(
                    17,
                    0,
                ),
            )

        val LAST_ACTIVITY_ID =
            stringPreferencesKey(
                "last_activity_id"
            )

        val SELECTED_WEEKDAYS =
            stringSetPreferencesKey(
                "selected_weekdays"
            )

        val WORK_START =
            stringPreferencesKey(
                "work_start"
            )

        val WORK_END =
            stringPreferencesKey(
                "work_end"
            )

        val LUNCH_ENABLED =
            booleanPreferencesKey(
                "lunch_enabled"
            )

        val LUNCH_START =
            stringPreferencesKey(
                "lunch_start"
            )

        val LUNCH_END =
            stringPreferencesKey(
                "lunch_end"
            )

        val SCHEDULE_SLOTS =
            stringPreferencesKey(
                "schedule_slots"
            )

        val LEGACY_SLOT_ENABLED_KEYS =
            listOf(
                booleanPreferencesKey(
                    "slot_1_enabled"
                ),
                booleanPreferencesKey(
                    "slot_2_enabled"
                ),
                booleanPreferencesKey(
                    "slot_3_enabled"
                ),
                booleanPreferencesKey(
                    "slot_4_enabled"
                ),
            )

        val LEGACY_SLOT_TIME_KEYS =
            listOf(
                stringPreferencesKey(
                    "slot_1_time"
                ),
                stringPreferencesKey(
                    "slot_2_time"
                ),
                stringPreferencesKey(
                    "slot_3_time"
                ),
                stringPreferencesKey(
                    "slot_4_time"
                ),
            )
    }
}