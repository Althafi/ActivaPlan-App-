package com.activaplan.app.domain

import com.activaplan.app.domain.model.BoardZone
import com.activaplan.app.domain.model.Category
import com.activaplan.app.domain.model.Difficulty
import com.activaplan.app.domain.model.Exercise
import com.activaplan.app.domain.model.Mode
import com.activaplan.app.domain.model.Routine
import com.activaplan.app.domain.model.RoutineItem
import com.activaplan.app.domain.model.ScheduleEntry
import com.activaplan.app.domain.model.Template
import com.activaplan.app.domain.validation.ExerciseCatalogValidator
import com.activaplan.app.domain.validation.UserDataValidator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidationTest {
    private fun ex(
        id: String,
        mode: Mode = Mode.NO_EQUIPMENT,
        zone: BoardZone? = null,
        reps: Int? = 10,
        duration: Int? = null,
        progression: String? = null,
        regression: String? = null,
    ) = Exercise(
        id = id, name = id, mode = mode, category = Category.CHEST, difficulty = Difficulty.EASY,
        boardZone = zone, primaryMuscles = emptyList(), sets = 3, reps = reps, durationSeconds = duration,
        restSeconds = 30, steps = emptyList(), tips = emptyList(), commonMistakes = emptyList(),
        progressionId = progression, regressionId = regression,
    )

    private fun tpl(vararg items: String) = Template("t1", "T", Difficulty.EASY, items.toList())

    private fun issuesFor(vararg e: Exercise, templates: List<Template> = emptyList()) =
        ExerciseCatalogValidator.validate(e.toList(), templates)

    @Test
    fun validCatalogHasNoIssues() {
        val issues = issuesFor(
            ex("a", progression = "b"),
            ex("b", mode = Mode.PUSHUP_BOARD, zone = BoardZone.RED, reps = null, duration = 30, regression = "a"),
            templates = listOf(tpl("a", "b")),
        )
        assertTrue(issues.isEmpty(), issues.toString())
    }

    @Test
    fun duplicateIdsReported() {
        val issues = issuesFor(ex("a"), ex("a"))
        assertEquals(listOf("a"), issues.map { it.subjectId })
    }

    @Test
    fun progressionAndRegressionMustExist() {
        val issues = issuesFor(ex("a", progression = "nope", regression = "gone"))
        assertEquals(2, issues.size)
    }

    @Test
    fun templateItemsMustExist() {
        val issues = issuesFor(ex("a"), templates = listOf(tpl("a", "missing")))
        assertEquals(1, issues.size)
        assertEquals("t1", issues.single().subjectId)
    }

    @Test
    fun pushupBoardRequiresZone() {
        assertEquals(1, issuesFor(ex("a", mode = Mode.PUSHUP_BOARD, zone = null)).size)
    }

    @Test
    fun otherModesMustNotHaveZone() {
        assertEquals(1, issuesFor(ex("a", mode = Mode.BOARD_ROPE, zone = BoardZone.BLUE)).size)
        assertEquals(1, issuesFor(ex("b", mode = Mode.NO_EQUIPMENT, zone = BoardZone.GREEN)).size)
        assertTrue(issuesFor(ex("c", mode = Mode.BOARD_ROPE, zone = null)).isEmpty())
    }

    @Test
    fun exactlyOneOfRepsOrDuration() {
        assertEquals(1, issuesFor(ex("both", reps = 10, duration = 30)).size)
        assertEquals(1, issuesFor(ex("none", reps = null, duration = null)).size)
        assertTrue(issuesFor(ex("reps", reps = 10, duration = null)).isEmpty())
        assertTrue(issuesFor(ex("dur", reps = null, duration = 30)).isEmpty())
    }

    private fun item(id: String) = RoutineItem(id, 3, 10, null, 30)
    private fun entry(id: String, routine: String, day: Int = 1, time: Int = 420) =
        ScheduleEntry(id, day, time, routine, true)

    @Test
    fun cleanUserDataPassesThrough() {
        val routines = listOf(Routine("r1", "R", listOf(item("a"))))
        val result = UserDataValidator.validate(routines, listOf(entry("s1", "r1")), setOf("a"))
        assertFalse(result.hasBrokenData)
        assertEquals(routines, result.routines)
        assertEquals(1, result.schedule.size)
    }

    @Test
    fun routineItemsWithUnknownExerciseAreDroppedAndReported() {
        val routines = listOf(Routine("r1", "R", listOf(item("a"), item("gone"))))
        val result = UserDataValidator.validate(routines, emptyList(), setOf("a"))
        assertEquals(listOf("a"), result.routines.single().items.map { it.exerciseId })
        assertEquals(listOf("r1"), result.issues.map { it.subjectId })
        assertTrue(result.hasBrokenData)
    }

    @Test
    fun scheduleWithUnknownRoutineIsDroppedAndReported() {
        val routines = listOf(Routine("r1", "R", listOf(item("a"))))
        val result = UserDataValidator.validate(
            routines, listOf(entry("s1", "r1"), entry("s2", "nope")), setOf("a"),
        )
        assertEquals(listOf("s1"), result.schedule.map { it.id })
        assertEquals(listOf("s2"), result.issues.map { it.subjectId })
    }

    @Test
    fun scheduleWithOutOfRangeDayOrTimeIsDropped() {
        val routines = listOf(Routine("r1", "R", listOf(item("a"))))
        val result = UserDataValidator.validate(
            routines,
            listOf(entry("d0", "r1", day = 0), entry("d8", "r1", day = 8), entry("t", "r1", time = 1440), entry("ok", "r1", day = 7, time = 1439)),
            setOf("a"),
        )
        assertEquals(listOf("ok"), result.schedule.map { it.id })
        assertEquals(3, result.issues.size)
    }
}
