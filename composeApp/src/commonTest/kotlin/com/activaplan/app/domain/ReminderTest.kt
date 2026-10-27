package com.activaplan.app.domain

import com.activaplan.app.data.InMemoryPreferencesDataStore
import com.activaplan.app.data.local.UserDataStore
import com.activaplan.app.data.repository.ExerciseDataSource
import com.activaplan.app.data.repository.ExerciseFiles
import com.activaplan.app.data.repository.ExerciseRepository
import com.activaplan.app.data.repository.UserDataRepository
import com.activaplan.app.domain.model.Routine
import com.activaplan.app.domain.model.RoutineItem
import com.activaplan.app.domain.model.ScheduleEntry
import com.activaplan.app.domain.reminder.NotificationPermission
import com.activaplan.app.domain.reminder.PermissionState
import com.activaplan.app.domain.reminder.Reminder
import com.activaplan.app.domain.reminder.ReminderScheduler
import com.activaplan.app.domain.reminder.ReminderSynchronizer
import com.activaplan.app.domain.reminder.nearestReminders
import com.activaplan.app.domain.reminder.nextOccurrence
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReminderTest {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    /** Senin 26 Oktober 2026, 16.00. */
    private val now = LocalDateTime(2026, 10, 26, 16, 0)

    // region Waktu berikutnya

    @Test
    fun laterTodayStaysToday() {
        assertEquals(LocalDateTime(2026, 10, 26, 16, 30), nextOccurrence(1, 16 * 60 + 30, now))
    }

    @Test
    fun sameMinuteOrEarlierTodayMovesToNextWeek() {
        assertEquals(LocalDateTime(2026, 11, 2, 16, 0), nextOccurrence(1, 16 * 60, now))
        assertEquals(LocalDateTime(2026, 11, 2, 15, 0), nextOccurrence(1, 15 * 60, now))
    }

    @Test
    fun otherDaysCountForwardAndCrossMonth() {
        assertEquals(LocalDateTime(2026, 10, 28, 6, 30), nextOccurrence(3, 6 * 60 + 30, now))
        assertEquals(LocalDateTime(2026, 11, 1, 7, 0), nextOccurrence(7, 7 * 60, now))
    }

    @Test
    fun nearestRemindersKeepsSoonestOnes() {
        fun reminder(id: String, day: Int, time: Int) = Reminder(id, day, time, "t", "b")
        val all = listOf(reminder("sun", 7, 600), reminder("mon-late", 1, 17 * 60), reminder("mon-early", 1, 8 * 60), reminder("tue", 2, 600))

        assertEquals(listOf("mon-late", "tue"), nearestReminders(all, now, 2).map { it.id })
        assertEquals(4, nearestReminders(all, now, 64).size)
    }

    // endregion

    // region Penyelaras

    private class FakeScheduler : ReminderScheduler {
        val calls = mutableListOf<List<Reminder>>()
        override suspend fun replaceAll(reminders: List<Reminder>) {
            calls.add(reminders)
        }
        val last: List<Reminder> get() = calls.last()
    }

    private class FakePermission(var state: PermissionState) : NotificationPermission {
        override suspend fun status() = state
        override fun openSettings() = Unit
    }

    private val files = mapOf(
        ExerciseFiles.GENERAL to """{"schemaVersion":1,"exercises":[
            {"id":"a","name":"A","mode":"NO_EQUIPMENT","category":"CHEST","difficulty":"EASY","primaryMuscles":["chest"],
             "sets":3,"reps":10,"restSeconds":30,"steps":[],"tips":[],"commonMistakes":[]}],"templates":[]}""",
        ExerciseFiles.PUSHUP_BOARD to """{"schemaVersion":1,"exercises":[],"templates":[]}""",
        ExerciseFiles.I18N to """{"schemaVersion":1,"baseLanguage":"id","muscles":[],"packs":{}}""",
    )
    private val userData = UserDataRepository(
        UserDataStore(InMemoryPreferencesDataStore(), json),
        ExerciseRepository(ExerciseDataSource { files.getValue(it) }, json),
    )
    private val scheduler = FakeScheduler()
    private val permission = FakePermission(PermissionState.GRANTED)
    private val synchronizer = ReminderSynchronizer(userData, scheduler, permission)

    private suspend fun seed() {
        userData.saveRoutine(Routine("r1", "Rutinitas Satu", listOf(RoutineItem("a", 3, 10, null, 30))))
        userData.saveRoutine(Routine("empty", "Kosong", emptyList()))
        userData.saveScheduleEntry(ScheduleEntry("on", 1, 390, "r1", reminder = true))
        userData.saveScheduleEntry(ScheduleEntry("off", 2, 390, "r1", reminder = false))
        userData.saveScheduleEntry(ScheduleEntry("orphan", 3, 390, "hilang", reminder = true))
        userData.saveScheduleEntry(ScheduleEntry("no-items", 4, 390, "empty", reminder = true))
    }

    @Test
    fun schedulesOnlyEntriesWithReminderAndExistingRoutine() = runTest {
        seed()

        synchronizer.sync()

        val reminder = scheduler.last.single()
        assertEquals("on", reminder.id)
        assertEquals(1, reminder.dayOfWeek)
        assertEquals(390, reminder.timeMinutes)
        assertTrue("Rutinitas Satu" in reminder.body)
        assertTrue("06.30" in reminder.title)
    }

    @Test
    fun deniedOrUndeterminedPermissionCancelsEverything() = runTest {
        seed()

        permission.state = PermissionState.DENIED
        synchronizer.sync()
        assertTrue(scheduler.last.isEmpty())

        permission.state = PermissionState.UNDETERMINED
        synchronizer.sync()
        assertTrue(scheduler.last.isEmpty())
    }

    @Test
    fun grantingPermissionLaterSchedulesAgain() = runTest {
        seed()
        permission.state = PermissionState.DENIED
        synchronizer.sync()

        permission.state = PermissionState.GRANTED
        synchronizer.sync()

        assertEquals(listOf("on"), scheduler.last.map { it.id })
    }

    @Test
    fun disabledRemindersSettingCancelsEverything() = runTest {
        seed()
        userData.updateSettings { it.copy(remindersEnabled = false) }

        synchronizer.sync()

        assertTrue(scheduler.last.isEmpty())
    }

    @Test
    fun syncIsIdempotent() = runTest {
        seed()

        synchronizer.sync()
        synchronizer.sync()

        assertEquals(2, scheduler.calls.size)
        assertEquals(scheduler.calls[0], scheduler.calls[1])
    }

    // endregion
}
