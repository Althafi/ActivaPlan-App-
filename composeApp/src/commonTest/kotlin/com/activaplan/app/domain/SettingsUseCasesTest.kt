package com.activaplan.app.domain

import com.activaplan.app.data.InMemoryPreferencesDataStore
import com.activaplan.app.data.local.UserDataStore
import com.activaplan.app.data.repository.ExerciseDataSource
import com.activaplan.app.data.repository.ExerciseFiles
import com.activaplan.app.data.repository.ExerciseRepository
import com.activaplan.app.data.repository.UserDataRepository
import com.activaplan.app.designsystem.isDark
import com.activaplan.app.domain.model.Routine
import com.activaplan.app.domain.model.RoutineItem
import com.activaplan.app.domain.model.ScheduleEntry
import com.activaplan.app.domain.model.SessionLog
import com.activaplan.app.domain.model.SessionStatus
import com.activaplan.app.domain.model.Settings
import com.activaplan.app.domain.model.ThemeMode
import com.activaplan.app.domain.usecase.ClearAllDataUseCase
import com.activaplan.app.domain.usecase.ClearSessionHistoryUseCase
import com.activaplan.app.domain.usecase.ObserveSettingsUseCase
import com.activaplan.app.domain.usecase.SetDefaultTimeUseCase
import com.activaplan.app.domain.usecase.SetKeepHistoryUseCase
import com.activaplan.app.domain.usecase.SetRemindersEnabledUseCase
import com.activaplan.app.domain.usecase.SetThemeUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingsUseCasesTest {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val files = mapOf(
        ExerciseFiles.GENERAL to """{"schemaVersion":1,"exercises":[
            {"id":"a","name":"A","mode":"NO_EQUIPMENT","category":"CHEST","difficulty":"EASY","primaryMuscles":["chest"],
             "sets":3,"reps":10,"restSeconds":30,"steps":[],"tips":[],"commonMistakes":[]}],"templates":[]}""",
        ExerciseFiles.PUSHUP_BOARD to """{"schemaVersion":1,"exercises":[],"templates":[]}""",
        ExerciseFiles.I18N to """{"schemaVersion":1,"baseLanguage":"id","muscles":[],"packs":{}}""",
    )
    private val exercises = ExerciseRepository(ExerciseDataSource { files.getValue(it) }, json)
    private val userData = UserDataRepository(UserDataStore(InMemoryPreferencesDataStore(), json), exercises)

    private suspend fun settings() = ObserveSettingsUseCase(userData)().first()

    private suspend fun seedUserData() {
        userData.saveRoutine(Routine("r1", "Rutinitas", listOf(RoutineItem("a", 3, 10, null, 30))))
        userData.saveScheduleEntry(ScheduleEntry("s1", 1, 990, "r1", reminder = true))
        userData.appendSessionLog(SessionLog("2026-10-26", "r1", SessionStatus.COMPLETED, 3, 3))
    }

    @Test
    fun themeModeResolvesDarkFromSystemOnlyWhenSystem() {
        assertFalse(ThemeMode.LIGHT.isDark(systemDark = true))
        assertTrue(ThemeMode.DARK.isDark(systemDark = false))
        assertTrue(ThemeMode.SYSTEM.isDark(systemDark = true))
        assertFalse(ThemeMode.SYSTEM.isDark(systemDark = false))
    }

    @Test
    fun setThemeIsStoredAndObserved() = runTest {
        assertEquals(ThemeMode.SYSTEM, settings().theme)

        SetThemeUseCase(userData)(ThemeMode.DARK)

        assertEquals(ThemeMode.DARK, settings().theme)
    }

    @Test
    fun remindersAndDefaultTimeChangeOnlyTheirOwnField() = runTest {
        SetRemindersEnabledUseCase(userData)(false)
        SetDefaultTimeUseCase(userData)(7 * 60 + 5)

        assertEquals(Settings.DEFAULT.copy(remindersEnabled = false, defaultTimeMinutes = 7 * 60 + 5), settings())
    }

    @Test
    fun defaultTimeOutsideADayIsRejected() = runTest {
        assertFailsWith<IllegalArgumentException> { SetDefaultTimeUseCase(userData)(24 * 60) }
        assertFailsWith<IllegalArgumentException> { SetDefaultTimeUseCase(userData)(-1) }

        assertEquals(Settings.DEFAULT.defaultTimeMinutes, settings().defaultTimeMinutes)
    }

    @Test
    fun turningHistoryOffKeepsOldLogsButStopsNewOnes() = runTest {
        seedUserData()

        SetKeepHistoryUseCase(userData)(false)
        val written = userData.appendSessionLog(SessionLog("2026-10-27", "r1", SessionStatus.PARTIAL, 1, 3))

        assertFalse(written)
        assertEquals(1, userData.sessionLogs.first().size)
    }

    @Test
    fun clearSessionHistoryOnlyEmptiesLogs() = runTest {
        seedUserData()

        ClearSessionHistoryUseCase(userData)()

        assertEquals(emptyList(), userData.sessionLogs.first())
        assertEquals(1, userData.routines.first().size)
        assertEquals(1, userData.schedule.first().size)
    }

    @Test
    fun clearAllEmptiesUserDataAndKeepsSettings() = runTest {
        seedUserData()
        SetThemeUseCase(userData)(ThemeMode.DARK)

        ClearAllDataUseCase(userData)()

        assertEquals(emptyList(), userData.routines.first())
        assertEquals(emptyList(), userData.schedule.first())
        assertEquals(emptyList(), userData.sessionLogs.first())
        assertEquals(ThemeMode.DARK, settings().theme)
    }

    @Test
    fun clearAllLeavesBundledExerciseCatalogUntouched() = runTest {
        seedUserData()
        val before = exercises.catalog().exercises.map { it.id }

        ClearAllDataUseCase(userData)()

        assertEquals(listOf("a"), before)
        assertEquals(before, exercises.catalog().exercises.map { it.id })
    }
}
