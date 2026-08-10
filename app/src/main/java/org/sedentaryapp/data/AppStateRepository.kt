package org.sedentaryapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import java.time.DayOfWeek
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.appStateDataStore: DataStore<Preferences> by preferencesDataStore(
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

    private val appContext = context.applicationContext

    private val slotDefaults = defaultSlotTimes.also {
        require(it.size == 4) {
            "Exactly 4 default slots are required."
        }
    }

    val defaultScheduleSettings = ScheduleSettings(
        selectedWeekdays = emptySet(),
        workStart = DEFAULT_WORK_START,
        workEnd = DEFAULT_WORK_END,
        lunchEnabled = true,
        lunchStart = DEFAULT_LUNCH_START,
        lunchEnd = DEFAULT_LUNCH_END,
        slots = slotDefaults.mapIndexed { index, time ->
            StandardSlotSetting(
                index = index,
                enabled = true,
                time = time,
            )
        },
    )

    val lastActivityId: Flow<String?> =
        appContext.appStateDataStore.data
            .safePreferences()
            .map { preferences ->
                preferences[LAST_ACTIVITY_ID]
            }

    val scheduleSettings: Flow<ScheduleSettings> =
        appContext.appStateDataStore.data
            .safePreferences()
            .map { preferences ->

                val weekdays =
                    preferences[SELECTED_WEEKDAYS]
                        ?.mapNotNull { value ->
                            runCatching {
                                DayOfWeek.valueOf(value)
                            }.getOrNull()
                        }
                        ?.toSet()
                        ?: emptySet()

                val workStart = parseTime(
                    preferences[WORK_START],
                    DEFAULT_WORK_START,
                )

                val workEnd = parseTime(
                    preferences[WORK_END],
                    DEFAULT_WORK_END,
                )

                val lunchEnabled =
                    preferences[LUNCH_ENABLED] ?: true

                val lunchStart = parseTime(
                    preferences[LUNCH_START],
                    DEFAULT_LUNCH_START,
                )

                val lunchEnd = parseTime(
                    preferences[LUNCH_END],
                    DEFAULT_LUNCH_END,
                )

                val slots =
                    slotDefaults.mapIndexed { index, defaultTime ->

                        val enabled =
                            preferences[SLOT_ENABLED_KEYS[index]]
                                ?: true

                        val time = parseTime(
                            preferences[SLOT_TIME_KEYS[index]],
                            defaultTime,
                        )

                        StandardSlotSetting(
                            index = index,
                            enabled = enabled,
                            time = time,
                        )
                    }

                ScheduleSettings(
                    selectedWeekdays = weekdays,
                    workStart = workStart,
                    workEnd = workEnd,
                    lunchEnabled = lunchEnabled,
                    lunchStart = lunchStart,
                    lunchEnd = lunchEnd,
                    slots = slots,
                )
            }
    suspend fun saveScheduleSettings(
        settings: ScheduleSettings,
    ) {
        appContext.appStateDataStore.edit { preferences ->

            preferences[SELECTED_WEEKDAYS] =
                settings.selectedWeekdays
                    .map { it.name }
                    .toSet()

            preferences[WORK_START] =
                settings.workStart.toString()

            preferences[WORK_END] =
                settings.workEnd.toString()

            preferences[LUNCH_ENABLED] =
                settings.lunchEnabled

            preferences[LUNCH_START] =
                settings.lunchStart.toString()

            preferences[LUNCH_END] =
                settings.lunchEnd.toString()

            settings.slots.forEach { slot ->
                preferences[
                    SLOT_ENABLED_KEYS[slot.index]
                ] = slot.enabled

                preferences[
                    SLOT_TIME_KEYS[slot.index]
                ] = slot.time.toString()
            }
        }
    }
    suspend fun saveLastActivityId(
        activityId: String,
    ) {
        appContext.appStateDataStore.edit { preferences ->
            preferences[LAST_ACTIVITY_ID] = activityId
        }
    }

    suspend fun setWeekdayEnabled(
        day: DayOfWeek,
        enabled: Boolean,
    ) {
        appContext.appStateDataStore.edit { preferences ->

            val days =
                preferences[SELECTED_WEEKDAYS]
                    ?.toMutableSet()
                    ?: mutableSetOf()

            if (enabled) {
                days.add(day.name)
            } else {
                days.remove(day.name)
            }

            preferences[SELECTED_WEEKDAYS] = days
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
        appContext.appStateDataStore.edit { preferences ->
            preferences[LUNCH_ENABLED] = enabled
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
        require(index in 0..3)

        appContext.appStateDataStore.edit { preferences ->

            if (!enabled) {
                val activeCount =
                    (0..3).count { slotIndex ->
                        preferences[SLOT_ENABLED_KEYS[slotIndex]]
                            ?: true
                    }

                if (activeCount <= 1) {
                    return@edit
                }
            }

            preferences[SLOT_ENABLED_KEYS[index]] = enabled
        }
    }

    suspend fun setSlotTime(
        index: Int,
        time: LocalTime,
    ) {
        require(index in 0..3)

        saveTime(
            SLOT_TIME_KEYS[index],
            time,
        )
    }

    private suspend fun saveTime(
        key: Preferences.Key<String>,
        time: LocalTime,
    ) {
        appContext.appStateDataStore.edit { preferences ->
            preferences[key] = time.toString()
        }
    }

    private fun parseTime(
        value: String?,
        default: LocalTime,
    ): LocalTime {
        return value
            ?.let {
                runCatching {
                    LocalTime.parse(it)
                }.getOrNull()
            }
            ?: default
    }

    private fun Flow<Preferences>.safePreferences(): Flow<Preferences> {
        return catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
    }

    private companion object {

        val DEFAULT_WORK_START =
            LocalTime.of(8, 30)

        val DEFAULT_WORK_END =
            LocalTime.of(17, 30)

        val DEFAULT_LUNCH_START =
            LocalTime.of(12, 30)

        val DEFAULT_LUNCH_END =
            LocalTime.of(13, 30)

        val LAST_ACTIVITY_ID =
            stringPreferencesKey("last_activity_id")

        val SELECTED_WEEKDAYS =
            stringSetPreferencesKey("selected_weekdays")

        val WORK_START =
            stringPreferencesKey("work_start")

        val WORK_END =
            stringPreferencesKey("work_end")

        val LUNCH_ENABLED =
            booleanPreferencesKey("lunch_enabled")

        val LUNCH_START =
            stringPreferencesKey("lunch_start")

        val LUNCH_END =
            stringPreferencesKey("lunch_end")

        val SLOT_ENABLED_KEYS =
            listOf(
                booleanPreferencesKey("slot_1_enabled"),
                booleanPreferencesKey("slot_2_enabled"),
                booleanPreferencesKey("slot_3_enabled"),
                booleanPreferencesKey("slot_4_enabled"),
            )

        val SLOT_TIME_KEYS =
            listOf(
                stringPreferencesKey("slot_1_time"),
                stringPreferencesKey("slot_2_time"),
                stringPreferencesKey("slot_3_time"),
                stringPreferencesKey("slot_4_time"),
            )
    }
}