package com.activaplan.app.domain

import com.activaplan.app.data.InMemoryPreferencesDataStore
import com.activaplan.app.data.local.UserDataStore
import com.activaplan.app.data.repository.ExerciseDataSource
import com.activaplan.app.data.repository.ExerciseFiles
import com.activaplan.app.data.repository.ExerciseRepository
import com.activaplan.app.data.repository.UserDataRepository
import com.activaplan.app.domain.i18n.Language
import com.activaplan.app.domain.model.Routine
import com.activaplan.app.domain.model.RoutineItem
import com.activaplan.app.domain.model.ScheduleEntry
import com.activaplan.app.domain.model.SessionLog
import com.activaplan.app.domain.model.SessionStatus
import com.activaplan.app.domain.model.SessionSummary
import com.activaplan.app.domain.time.LocalClock
import com.activaplan.app.domain.usecase.FinishSessionUseCase
import com.activaplan.app.domain.usecase.LoadSessionPlanUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SessionUseCasesTest {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val files = mapOf(
        ExerciseFiles.GENERAL to """{"schemaVersion":1,"exercises":[
            {"id":"a","name":"Latihan A","mode":"NO_EQUIPMENT","category":"CHEST","difficulty":"MEDIUM","primaryMuscles":["chest"],
             "sets":3,"reps":10,"restSeconds":60,"steps":[],"tips":[],"commonMistakes":[],"regressionId":"b"},
            {"id":"b","name":"Latihan B","mode":"NO_EQUIPMENT","category":"CHEST","difficulty":"EASY","primaryMuscles":["chest"],
             "sets":2,"reps":8,"restSeconds":30,"steps":[],"tips":[],"commonMistakes":[]}],
            "templates":[]}""",
        ExerciseFiles.PUSHUP_BOARD to """{"schemaVersion":1,"device":{"safetyNotes":["Cek tali."]},"exercises":[
            {"id":"r","name":"Latihan Tali","mode":"BOARD_ROPE","category":"ARMS","difficulty":"EASY","primaryMuscles":["forearms"],
             "sets":3,"durationSeconds":20,"restSeconds":45,"steps":[],"tips":[],"commonMistakes":[]}],
            "templates":[]}""",
        ExerciseFiles.I18N to """{"schemaVersion":1,"baseLanguage":"id","muscles":[],"packs":{}}""",
    )

    private val exercises = ExerciseRepository(ExerciseDataSource { files.getValue(it) }, json)
    private val userData = UserDataRepository(UserDataStore(InMemoryPreferencesDataStore(), json), exercises)

    /** Senin 26 Oktober 2026, 16.00. */
    private val clock = LocalClock { LocalDateTime(2026, 10, 26, 16, 0) }

    private val loadPlan = LoadSessionPlanUseCase(userData, exercises)
    private val finish = FinishSessionUseCase(userData, clock)

    private val routine = Routine("r1", "Rutinitas", listOf(RoutineItem("a", 3, 10, null, 60)))
    private val mondayEntry = ScheduleEntry("s1", dayOfWeek = 1, timeMinutes = 16 * 60, routineId = "r1", reminder = false)

    private fun summary(status: SessionStatus, done: Int = 3, total: Int = 3) =
        SessionSummary(status, completedSets = done, totalSets = total, completedExercises = 1, totalExercises = 1, minutes = 5)

    // region LoadSessionPlanUseCase

    @Test
    fun planUsesRoutineValuesAndSkipsUnknownExercises() = runTest {
        userData.saveRoutine(
            Routine(
                "r1", "Rutinitas",
                listOf(
                    RoutineItem("a", sets = 4, reps = 12, durationSeconds = null, restSeconds = 20),
                    RoutineItem("hilang", 3, 10, null, 60),
                    RoutineItem("r", sets = 2, reps = null, durationSeconds = 25, restSeconds = 15),
                ),
            ),
        )

        val plan = assertNotNull(loadPlan("r1", Language.ID))

        assertEquals("Rutinitas", plan.routineName)
        assertEquals(1, plan.skippedItems)
        assertEquals(listOf("a", "r"), plan.exercises.map { it.exercise.id })

        val first = plan.exercises[0]
        assertEquals(4, first.sets)
        assertEquals(12, first.reps)
        assertEquals(20, first.restSeconds)
        assertFalse(first.isTimed)

        val rope = plan.exercises[1]
        assertTrue(rope.isTimed)
        assertEquals(25, rope.durationSeconds)
        assertEquals(2, plan.exercises[1].sets)
        assertEquals(6, plan.totalSets)
    }

    @Test
    fun itemWithoutRepsOrDurationFallsBackToExerciseDefaults() = runTest {
        userData.saveRoutine(Routine("r1", "Rutinitas", listOf(RoutineItem("b", 2, null, null, 30))))

        val item = assertNotNull(loadPlan("r1", Language.ID)).exercises.single()

        assertEquals(8, item.reps)
        assertNull(item.durationSeconds)
    }

    @Test
    fun ropeExerciseRequiresSafetyCheckWithNotes() = runTest {
        userData.saveRoutine(Routine("r1", "Rutinitas", listOf(RoutineItem("a", 3, 10, null, 60), RoutineItem("r", 3, null, 20, 45))))

        val plan = assertNotNull(loadPlan("r1", Language.ID))

        assertTrue(plan.requiresSafetyCheck)
        assertEquals(listOf("Cek tali."), plan.safetyNotes)
    }

    @Test
    fun routineWithoutRopeSkipsSafetyCheck() = runTest {
        userData.saveRoutine(routine)

        val plan = assertNotNull(loadPlan("r1", Language.ID))

        assertFalse(plan.requiresSafetyCheck)
        assertTrue(plan.safetyNotes.isEmpty())
    }

    @Test
    fun catalogIncludesRegressionTargetsOutsideTheRoutine() = runTest {
        userData.saveRoutine(routine)

        val plan = assertNotNull(loadPlan("r1", Language.ID))

        assertEquals("Latihan B", plan.catalog["b"]?.name)
        assertEquals("b", plan.exercises.single().exercise.regressionId)
    }

    @Test
    fun planIsNullForMissingRoutineOrOnlyUnknownExercises() = runTest {
        assertNull(loadPlan("tidak-ada", Language.ID))

        userData.saveRoutine(Routine("kosong", "Kosong", listOf(RoutineItem("hilang", 3, 10, null, 60))))
        assertNull(loadPlan("kosong", Language.ID))
    }

    // endregion

    // region FinishSessionUseCase

    @Test
    fun recordWritesLogForTodayWhenHistoryIsKept() = runTest {
        val written = finish.record("r1", summary(SessionStatus.PARTIAL, done = 2, total = 3))

        assertTrue(written)
        assertEquals(
            listOf(SessionLog("2026-10-26", "r1", SessionStatus.PARTIAL, completedSets = 2, totalSets = 3)),
            userData.sessionLogs.first(),
        )
    }

    @Test
    fun recordWritesAbandonedLogToo() = runTest {
        finish.record("r1", summary(SessionStatus.ABANDONED, done = 0, total = 3))

        assertEquals(SessionStatus.ABANDONED, userData.sessionLogs.first().single().status)
    }

    @Test
    fun recordWritesNothingWhenHistoryIsOff() = runTest {
        userData.updateSettings { it.copy(keepHistory = false) }

        val written = finish.record("r1", summary(SessionStatus.COMPLETED))

        assertFalse(written)
        assertTrue(userData.sessionLogs.first().isEmpty())
    }

    @Test
    fun streakCountsTheFinishedSessionEvenWhenHistoryIsOff() = runTest {
        userData.saveRoutine(routine)
        userData.saveScheduleEntry(mondayEntry)
        userData.updateSettings { it.copy(keepHistory = false) }
        val done = summary(SessionStatus.COMPLETED)

        finish.record("r1", done)

        assertEquals(1, finish.streak("r1", done))
    }

    @Test
    fun streakDoesNotCountDoubleOnceTheLogIsSaved() = runTest {
        userData.saveRoutine(routine)
        userData.saveScheduleEntry(mondayEntry)
        val done = summary(SessionStatus.COMPLETED)

        finish.record("r1", done)

        assertEquals(1, finish.streak("r1", done))
    }

    @Test
    fun partialSessionDoesNotExtendTheStreak() = runTest {
        userData.saveRoutine(routine)
        userData.saveScheduleEntry(mondayEntry)
        val partial = summary(SessionStatus.PARTIAL, done = 1)

        finish.record("r1", partial)

        assertEquals(0, finish.streak("r1", partial))
    }

    // endregion
}
