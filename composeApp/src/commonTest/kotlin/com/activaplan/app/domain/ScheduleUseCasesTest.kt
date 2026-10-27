package com.activaplan.app.domain

import com.activaplan.app.data.InMemoryPreferencesDataStore
import com.activaplan.app.data.local.UserDataStore
import com.activaplan.app.data.repository.ExerciseDataSource
import com.activaplan.app.data.repository.ExerciseFiles
import com.activaplan.app.data.repository.ExerciseRepository
import com.activaplan.app.data.repository.UserDataRepository
import com.activaplan.app.domain.model.ConflictPolicy
import com.activaplan.app.domain.model.Difficulty
import com.activaplan.app.domain.model.Routine
import com.activaplan.app.domain.model.RoutineItem
import com.activaplan.app.domain.model.SaveScheduleResult
import com.activaplan.app.domain.model.ScheduleDraft
import com.activaplan.app.domain.model.ScheduleEntry
import com.activaplan.app.domain.time.LocalClock
import com.activaplan.app.domain.usecase.DeleteScheduleEntryUseCase
import com.activaplan.app.domain.usecase.ObserveScheduleUseCase
import com.activaplan.app.domain.usecase.PruneScheduleUseCase
import com.activaplan.app.domain.usecase.SaveScheduleUseCase
import com.activaplan.app.domain.usecase.SetScheduleReminderUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ScheduleUseCasesTest {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val files = mapOf(
        ExerciseFiles.GENERAL to """{"schemaVersion":1,"exercises":[
            {"id":"a","name":"A","mode":"NO_EQUIPMENT","category":"CHEST","difficulty":"EASY","primaryMuscles":["chest"],
             "sets":3,"reps":10,"restSeconds":30,"steps":[],"tips":[],"commonMistakes":[]},
            {"id":"b","name":"B","mode":"NO_EQUIPMENT","category":"CHEST","difficulty":"HARD","primaryMuscles":["chest"],
             "sets":3,"reps":10,"restSeconds":30,"steps":[],"tips":[],"commonMistakes":[]}],
            "templates":[]}""",
        ExerciseFiles.PUSHUP_BOARD to """{"schemaVersion":1,"exercises":[],"templates":[]}""",
        ExerciseFiles.I18N to """{"schemaVersion":1,"baseLanguage":"id","muscles":[],"packs":{}}""",
    )

    private val exercises = ExerciseRepository(ExerciseDataSource { files.getValue(it) }, json)
    private val userData = UserDataRepository(UserDataStore(InMemoryPreferencesDataStore(), json), exercises)

    /** Senin 26 Oktober 2026, 16.00. */
    private val clock = LocalClock { LocalDateTime(2026, 10, 26, 16, 0) }

    private var idCounter = 0
    private val save = SaveScheduleUseCase(userData, newId = { "new${++idCounter}" })

    /** 38 menit (6 set x (50 rep x 4 dtk + 180 dtk istirahat)). */
    private val long = Routine("long", "Panjang", listOf(RoutineItem("b", 6, 50, null, 180)))

    /** 4 menit. */
    private val short = Routine("short", "Pendek", listOf(RoutineItem("a", 3, 10, null, 30)))

    private suspend fun seed(vararg entries: ScheduleEntry) {
        userData.saveRoutine(long)
        userData.saveRoutine(short)
        entries.forEach { userData.saveScheduleEntry(it) }
    }

    private fun draft(days: Set<Int>, time: Int, routineId: String? = "short", editingId: String? = null, reminder: Boolean = true) =
        ScheduleDraft(editingId, days, time, routineId, reminder)

    private suspend fun stored() = userData.schedule.first()

    // region Simpan

    @Test
    fun savingCreatesOneEntryPerSelectedDay() = runTest {
        seed()

        val result = save(draft(setOf(5, 1, 3), 390))

        val saved = assertIs<SaveScheduleResult.Saved>(result)
        assertEquals(listOf(1, 3, 5), saved.entries.map { it.dayOfWeek })
        assertEquals(setOf("new1", "new2", "new3"), stored().map { it.id }.toSet())
        assertTrue(stored().all { it.timeMinutes == 390 && it.routineId == "short" && it.reminder })
    }

    @Test
    fun rejectsDraftWithoutDays() = runTest {
        seed()

        assertEquals(SaveScheduleResult.NoDay, save(draft(emptySet(), 390)))
        assertEquals(SaveScheduleResult.NoDay, save(draft(setOf(0, 8), 390)))
        assertTrue(stored().isEmpty())
    }

    @Test
    fun rejectsMissingOrEmptyRoutine() = runTest {
        seed()
        userData.saveRoutine(Routine("empty", "Kosong", emptyList()))

        assertEquals(SaveScheduleResult.NoRoutine, save(draft(setOf(1), 390, routineId = null)))
        assertEquals(SaveScheduleResult.NoRoutine, save(draft(setOf(1), 390, routineId = "tidak-ada")))
        assertEquals(SaveScheduleResult.NoRoutine, save(draft(setOf(1), 390, routineId = "empty")))
        assertTrue(stored().isEmpty())
    }

    @Test
    fun editingKeepsEntryIdAndAddsOtherDays() = runTest {
        seed(ScheduleEntry("s1", 1, 360, "short", reminder = false))

        val result = save(draft(setOf(1, 4), 420, routineId = "long", editingId = "s1", reminder = true))

        assertIs<SaveScheduleResult.Saved>(result)
        val byDay = stored().associateBy { it.dayOfWeek }
        assertEquals(setOf(1, 4), byDay.keys)
        assertEquals("s1", byDay.getValue(1).id)
        assertEquals("new1", byDay.getValue(4).id)
        assertTrue(stored().all { it.timeMinutes == 420 && it.routineId == "long" && it.reminder })
    }

    @Test
    fun editingMovesEntryToNewDayReusingItsId() = runTest {
        seed(ScheduleEntry("s1", 1, 360, "short", reminder = true))

        save(draft(setOf(2), 360, editingId = "s1"))

        assertEquals(listOf(ScheduleEntry("s1", 2, 360, "short", reminder = true)), stored())
    }

    @Test
    fun editedEntryDoesNotConflictWithItself() = runTest {
        seed(ScheduleEntry("s1", 1, 360, "long", reminder = true))

        val result = save(draft(setOf(1), 370, routineId = "long", editingId = "s1"))

        assertIs<SaveScheduleResult.Saved>(result)
        assertEquals(listOf(370), stored().map { it.timeMinutes })
    }

    // endregion

    // region Bentrok

    @Test
    fun conflictIsReportedAndNothingIsWritten() = runTest {
        val existing = ScheduleEntry("s1", 1, 360, "long", reminder = true)
        seed(existing)

        val result = save(draft(setOf(1, 2), 390))

        assertEquals(SaveScheduleResult.Conflict(listOf(existing)), result)
        assertEquals(listOf(existing), stored())
    }

    @Test
    fun noConflictWhenGapIsAtLeastEarlierRoutineDuration() = runTest {
        seed(ScheduleEntry("s1", 1, 360, "long", reminder = true))

        assertIs<SaveScheduleResult.Saved>(save(draft(setOf(1), 360 + 38)))
        assertEquals(2, stored().size)
    }

    @Test
    fun replaceRemovesConflictingEntriesOnly() = runTest {
        val conflicting = ScheduleEntry("s1", 1, 360, "long", reminder = true)
        val farAway = ScheduleEntry("s2", 1, 1000, "long", reminder = true)
        seed(conflicting, farAway)

        val result = save(draft(setOf(1), 390), ConflictPolicy.REPLACE)

        assertIs<SaveScheduleResult.Saved>(result)
        assertEquals(setOf("s2", "new1"), stored().map { it.id }.toSet())
    }

    @Test
    fun keepBothAddsNewEntryBesideConflictingOne() = runTest {
        seed(ScheduleEntry("s1", 1, 360, "long", reminder = true))

        val result = save(draft(setOf(1), 390), ConflictPolicy.KEEP_BOTH)

        assertIs<SaveScheduleResult.Saved>(result)
        assertEquals(setOf("s1", "new1"), stored().map { it.id }.toSet())
    }

    // endregion

    // region Ubah, hapus, bersihkan

    @Test
    fun setReminderTogglesOnlyThatEntry() = runTest {
        seed(ScheduleEntry("s1", 1, 360, "short", reminder = true), ScheduleEntry("s2", 2, 360, "short", reminder = true))

        SetScheduleReminderUseCase(userData)("s1", false)

        assertEquals(mapOf("s1" to false, "s2" to true), stored().associate { it.id to it.reminder })
    }

    @Test
    fun deleteRemovesEntry() = runTest {
        seed(ScheduleEntry("s1", 1, 360, "short", reminder = true), ScheduleEntry("s2", 2, 360, "short", reminder = true))

        DeleteScheduleEntryUseCase(userData)("s1")

        assertEquals(listOf("s2"), stored().map { it.id })
    }

    @Test
    fun pruneRemovesEntriesOfDeletedRoutines() = runTest {
        seed(ScheduleEntry("s1", 1, 360, "short", reminder = true), ScheduleEntry("orphan", 2, 360, "hilang", reminder = true))

        val removed = PruneScheduleUseCase(userData)()

        assertEquals(1, removed)
        assertEquals(listOf("s1"), stored().map { it.id })
    }

    @Test
    fun deletingRoutineAlsoRemovesItsSchedule() = runTest {
        seed(ScheduleEntry("s1", 1, 360, "short", reminder = true), ScheduleEntry("s2", 2, 360, "long", reminder = true))

        userData.deleteRoutine("short")

        assertEquals(listOf("s2"), stored().map { it.id })
    }

    // endregion

    // region Observe

    @Test
    fun observeBuildsWeekItemsAndNextTodayEntry() = runTest {
        seed(
            ScheduleEntry("late", 1, 19 * 60, "long", reminder = false),
            ScheduleEntry("next", 1, 16 * 60 + 30, "short", reminder = true),
            ScheduleEntry("past", 1, 6 * 60, "short", reminder = true),
            ScheduleEntry("wed", 3, 6 * 60, "long", reminder = true),
            ScheduleEntry("orphan", 4, 6 * 60, "hilang", reminder = true),
        )

        val data = ObserveScheduleUseCase(userData, exercises, clock, flowOf(Unit))().first()

        assertEquals(LocalDate(2026, 10, 26), data.today)
        assertEquals((26..31).map { it } + 1, data.week.map { it.date.day })
        assertEquals(listOf(true, false, true, false, false, false, false), data.week.map { it.hasSchedule })
        assertEquals(listOf(true, false, false, false, false, false, false), data.week.map { it.isToday })
        assertEquals(listOf("past", "next", "late", "wed"), data.items.map { it.id })
        assertEquals("next", data.nextTodayId)

        val wed = data.items.last()
        assertEquals("Panjang", wed.routineName)
        assertEquals(Difficulty.HARD, wed.level)
        assertEquals(38, wed.estimatedMinutes)
        assertEquals(1, wed.exerciseCount)
        assertEquals(listOf("long", "short"), data.routines.map { it.id }.sorted())
    }

    @Test
    fun nextTodayFallsBackToLastEntryWhenAllHavePassed() = runTest {
        seed(ScheduleEntry("a", 1, 6 * 60, "short", reminder = true), ScheduleEntry("b", 1, 8 * 60, "short", reminder = true))

        val data = ObserveScheduleUseCase(userData, exercises, clock, flowOf(Unit))().first()

        assertEquals("b", data.nextTodayId)
    }

    @Test
    fun nextTodayIsNullWithoutEntriesToday() = runTest {
        seed(ScheduleEntry("a", 2, 6 * 60, "short", reminder = true))

        val data = ObserveScheduleUseCase(userData, exercises, clock, flowOf(Unit))().first()

        assertNull(data.nextTodayId)
    }

    // endregion
}
