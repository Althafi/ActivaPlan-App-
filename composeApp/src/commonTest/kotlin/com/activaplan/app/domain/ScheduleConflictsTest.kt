package com.activaplan.app.domain

import com.activaplan.app.domain.model.ScheduleEntry
import com.activaplan.app.domain.schedule.ScheduleConflicts
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScheduleConflictsTest {
    private val minutes = mapOf("long" to 38, "short" to 4)
    private val minutesOf = { id: String -> minutes[id] ?: 0 }

    private fun entry(id: String, day: Int, time: Int, routine: String) = ScheduleEntry(id, day, time, routine, reminder = true)

    @Test
    fun differentDaysNeverConflict() {
        assertFalse(ScheduleConflicts.overlaps(entry("a", 1, 360, "long"), entry("b", 2, 360, "long"), minutesOf))
    }

    @Test
    fun sameStartTimeConflictsEvenWithUnknownDuration() {
        assertTrue(ScheduleConflicts.overlaps(entry("a", 1, 360, "x"), entry("b", 1, 360, "y"), minutesOf))
    }

    @Test
    fun gapShorterThanEarlierRoutineConflicts() {
        assertTrue(ScheduleConflicts.overlaps(entry("a", 1, 360, "long"), entry("b", 1, 390, "short"), minutesOf))
    }

    @Test
    fun gapEqualToEarlierRoutineDoesNotConflict() {
        assertFalse(ScheduleConflicts.overlaps(entry("a", 1, 360, "long"), entry("b", 1, 398, "short"), minutesOf))
    }

    @Test
    fun durationOfTheEarlierEntryDecidesRegardlessOfArgumentOrder() {
        val early = entry("early", 1, 358, "short")
        val late = entry("late", 1, 360, "long")
        // Yang lebih awal hanya 4 menit, jadi selisih 2 menit bentrok; selisih 10 menit tidak.
        assertTrue(ScheduleConflicts.overlaps(early, late, minutesOf))
        assertTrue(ScheduleConflicts.overlaps(late, early, minutesOf))
        assertFalse(ScheduleConflicts.overlaps(entry("e", 1, 350, "short"), late, minutesOf))
    }

    @Test
    fun findReturnsOnlyOverlappingExistingEntries() {
        val existing = listOf(entry("a", 1, 360, "long"), entry("b", 1, 600, "long"), entry("c", 2, 360, "long"))
        val found = ScheduleConflicts.find(listOf(entry("new", 1, 380, "short")), existing, minutesOf)
        assertEquals(listOf("a"), found.map { it.id })
    }
}
