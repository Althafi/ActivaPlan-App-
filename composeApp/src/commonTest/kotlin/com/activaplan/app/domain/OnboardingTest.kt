package com.activaplan.app.domain

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.activaplan.app.data.InMemoryPreferencesDataStore
import com.activaplan.app.data.local.UserDataStore
import com.activaplan.app.data.local.migrateOnboarding
import com.activaplan.app.data.repository.ExerciseDataSource
import com.activaplan.app.data.repository.ExerciseFiles
import com.activaplan.app.data.repository.ExerciseRepository
import com.activaplan.app.data.repository.UserDataRepository
import com.activaplan.app.domain.model.Difficulty
import com.activaplan.app.domain.model.Mode
import com.activaplan.app.domain.model.OnboardingChoices
import com.activaplan.app.domain.model.Settings
import com.activaplan.app.domain.model.ThemeMode
import com.activaplan.app.domain.model.initialExploreMode
import com.activaplan.app.domain.usecase.CompleteOnboardingUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OnboardingTest {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val dataStore = InMemoryPreferencesDataStore()
    private val files = mapOf(
        ExerciseFiles.GENERAL to """{"schemaVersion":1,"exercises":[],"templates":[]}""",
        ExerciseFiles.PUSHUP_BOARD to """{"schemaVersion":1,"exercises":[],"templates":[]}""",
        ExerciseFiles.I18N to """{"schemaVersion":1,"baseLanguage":"id","muscles":[],"packs":{}}""",
    )
    private val userData = UserDataRepository(
        UserDataStore(dataStore, json),
        ExerciseRepository(ExerciseDataSource { files.getValue(it) }, json),
    )
    private val complete = CompleteOnboardingUseCase(userData)

    private val legacySettings = """{"theme":"DARK","remindersEnabled":true,"defaultTimeMinutes":990,"keepHistory":true}"""
    private val legacyRoutines = """[{"id":"r1","name":"R","items":[{"exerciseId":"a","sets":3,"reps":10,"restSeconds":30}]}]"""

    private suspend fun seedLegacy(withRoutine: Boolean) = dataStore.edit {
        it[stringPreferencesKey("settings")] = legacySettings
        if (withRoutine) it[stringPreferencesKey("routines")] = legacyRoutines
    }

    private suspend fun settings() = userData.settings.first()

    private fun choices(
        level: Difficulty = Difficulty.MEDIUM,
        modes: Set<Mode> = setOf(Mode.PUSHUP_BOARD),
        remindersOn: Boolean = false,
        time: Int = 6 * 60 + 30,
    ) = OnboardingChoices(level, modes, remindersOn, time)

    @Test
    fun newUserStartsWithOnboardingPendingAndDefaults() = runTest {
        val s = settings()

        assertFalse(s.onboardingDone)
        assertEquals(Difficulty.EASY, s.startLevel)
        assertEquals(setOf(Mode.NO_EQUIPMENT), s.ownedModes)
    }

    @Test
    fun completingSavesChoicesAndMarksDone() = runTest {
        complete(choices(modes = setOf(Mode.PUSHUP_BOARD, Mode.BOARD_ROPE), remindersOn = true))

        val s = settings()
        assertTrue(s.onboardingDone)
        assertEquals(Difficulty.MEDIUM, s.startLevel)
        assertEquals(setOf(Mode.PUSHUP_BOARD, Mode.BOARD_ROPE), s.ownedModes)
        assertTrue(s.remindersEnabled)
        assertEquals(6 * 60 + 30, s.defaultTimeMinutes)
    }

    @Test
    fun leavingRemindersOffDoesNotTouchReminderSettings() = runTest {
        userData.updateSettings { it.copy(remindersEnabled = false, defaultTimeMinutes = 420) }

        complete(choices(remindersOn = false, time = 600))

        val s = settings()
        assertFalse(s.remindersEnabled)
        assertEquals(420, s.defaultTimeMinutes)
        assertTrue(s.onboardingDone)
    }

    @Test
    fun laterOnlyMarksDone() = runTest {
        complete(null)

        assertEquals(Settings.DEFAULT.copy(onboardingDone = true), settings())
    }

    @Test
    fun rejectsChoicesWithoutAnyGear() = runTest {
        assertFailsWith<IllegalArgumentException> { complete(choices(modes = emptySet())) }

        assertFalse(settings().onboardingDone)
    }

    @Test
    fun choicesAreInvalidWithoutGearOrWithBadTime() {
        assertTrue(choices().isValid)
        assertFalse(choices(modes = emptySet()).isValid)
        assertFalse(choices(time = 1440).isValid)
    }

    @Test
    fun completedOnboardingSurvivesDeletingAllData() = runTest {
        complete(null)

        userData.clearAllData()

        assertTrue(settings().onboardingDone)
    }

    @Test
    fun legacyUserWithRoutinesIsTreatedAsDoneWithAllModes() = runTest {
        seedLegacy(withRoutine = true)

        val s = settings()

        assertTrue(s.onboardingDone)
        assertEquals(Mode.entries.toSet(), s.ownedModes)
        assertEquals(ThemeMode.DARK, s.theme)
    }

    @Test
    fun legacyUserWithoutAnyDataStillGoesThroughOnboarding() = runTest {
        seedLegacy(withRoutine = false)

        assertFalse(settings().onboardingDone)
    }

    @Test
    fun legacyMigrationStaysAfterTheirDataIsDeleted() = runTest {
        seedLegacy(withRoutine = true)
        assertTrue(settings().onboardingDone)

        userData.clearAllData()

        assertEquals(emptyList(), userData.routines.first())
        assertTrue(settings().onboardingDone)
    }

    @Test
    fun migrationNeverOverridesAnExplicitFlag() {
        val explicit = Settings.DEFAULT.copy(onboardingDone = false, ownedModes = setOf(Mode.BOARD_ROPE))

        assertEquals(explicit, explicit.migrateOnboarding(hasFlag = true, hasUserContent = true))
        assertEquals(explicit, explicit.migrateOnboarding(hasFlag = false, hasUserContent = false))
    }

    @Test
    fun settingsWithoutNewFieldsDecodeWithDefaults() {
        val s = json.decodeFromString<Settings>(legacySettings)

        assertFalse(s.onboardingDone)
        assertEquals(Difficulty.EASY, s.startLevel)
        assertEquals(setOf(Mode.NO_EQUIPMENT), s.ownedModes)
    }

    @Test
    fun newSettingsFieldsRoundTrip() {
        val s = Settings.DEFAULT.copy(onboardingDone = true, startLevel = Difficulty.HARD, ownedModes = setOf(Mode.PUSHUP_BOARD, Mode.BOARD_ROPE))

        assertEquals(s, json.decodeFromString<Settings>(json.encodeToString(s)))
    }

    @Test
    fun exploreOpensOnTheFirstOwnedMode() {
        fun mode(vararg owned: Mode) = Settings.DEFAULT.copy(ownedModes = owned.toSet()).initialExploreMode()

        assertEquals(Mode.NO_EQUIPMENT, mode(Mode.BOARD_ROPE, Mode.NO_EQUIPMENT))
        assertEquals(Mode.PUSHUP_BOARD, mode(Mode.BOARD_ROPE, Mode.PUSHUP_BOARD))
        assertEquals(Mode.BOARD_ROPE, mode(Mode.BOARD_ROPE))
        assertEquals(Mode.NO_EQUIPMENT, mode())
    }
}
