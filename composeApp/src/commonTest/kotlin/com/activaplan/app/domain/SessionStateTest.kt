package com.activaplan.app.domain

import com.activaplan.app.domain.model.BoardZone
import com.activaplan.app.domain.model.Category
import com.activaplan.app.domain.model.Difficulty
import com.activaplan.app.domain.model.Exercise
import com.activaplan.app.domain.model.Mode
import com.activaplan.app.domain.model.SessionExercise
import com.activaplan.app.domain.model.SessionOverlay
import com.activaplan.app.domain.model.SessionPhase
import com.activaplan.app.domain.model.SessionPlan
import com.activaplan.app.domain.model.SessionStatus
import com.activaplan.app.domain.session.SessionState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class SessionStateTest {

    private fun exercise(id: String, mode: Mode = Mode.NO_EQUIPMENT, regressionId: String? = null) = Exercise(
        id = id, name = "Latihan $id", mode = mode, category = Category.CHEST, difficulty = Difficulty.EASY,
        boardZone = if (mode == Mode.PUSHUP_BOARD) BoardZone.BLUE else null, primaryMuscles = listOf("chest"),
        sets = 3, reps = 10, restSeconds = 30, steps = listOf("langkah"), tips = emptyList(), commonMistakes = emptyList(),
        regressionId = regressionId,
    )

    private fun reps(id: String, sets: Int = 2, reps: Int = 10, rest: Int = 30, regressionId: String? = null) =
        SessionExercise(exercise(id, regressionId = regressionId), sets, reps, null, rest)

    private fun timed(id: String, sets: Int = 2, seconds: Int = 20, rest: Int = 30) =
        SessionExercise(exercise(id), sets, null, seconds, rest)

    private fun plan(
        vararg items: SessionExercise,
        safety: Boolean = false,
        extra: List<Exercise> = emptyList(),
    ) = SessionPlan(
        routineId = "r1",
        routineName = "Rutinitas",
        exercises = items.toList(),
        catalog = (items.map { it.exercise } + extra).associateBy { it.id },
        requiresSafetyCheck = safety,
        safetyNotes = if (safety) listOf("Cek tali.") else emptyList(),
        skippedItems = 0,
    )

    private fun start(vararg items: SessionExercise) = SessionState.start(plan(*items))

    private fun SessionState.working() = assertIs<SessionPhase.Working>(phase)

    private fun SessionState.resting() = assertIs<SessionPhase.Resting>(phase)

    private fun SessionState.finished() = assertIs<SessionPhase.Finished>(phase)

    // region Alur

    @Test
    fun startsAtSafetyCheckOnlyWhenPlanRequiresIt() {
        val safe = SessionState.start(plan(reps("a"), safety = true))
        assertEquals(SessionPhase.SafetyCheck, safe.phase)
        assertEquals(SessionPhase.Working(), SessionState.start(plan(reps("a"))).phase)

        // Tidak ada yang berjalan atau bisa diselesaikan sebelum pengguna mengonfirmasi.
        assertSame(safe, safe.advance(5_000))
        assertSame(safe, safe.completeSet())
        assertSame(safe, safe.requestStop())
        assertEquals(SessionPhase.Working(), safe.confirmSafety().phase)
    }

    @Test
    fun completingASetStartsRestThenNextSetOfSameExercise() {
        val rested = start(reps("a", sets = 2), reps("b")).completeSet()

        assertEquals(SessionPhase.Resting(30_000, 30_000), rested.phase)
        assertEquals(listOf(1, 0), rested.setsDone)
        assertEquals(2, rested.nextUp?.setNumber)
        assertEquals("a", rested.nextUp?.exercise?.exercise?.id)

        val next = rested.advance(30_000)
        assertEquals(0, next.index)
        assertEquals(2, next.setNumber)
        assertEquals(SessionPhase.Working(), next.phase)
    }

    @Test
    fun finishingAllSetsOfAnExerciseRestsThenMovesToNextExercise() {
        val rested = start(reps("a", sets = 1), reps("b")).completeSet()

        assertEquals("b", rested.nextUp?.exercise?.exercise?.id)
        assertEquals(1, rested.nextUp?.setNumber)
        val next = rested.skipRest()
        assertEquals(1, next.index)
        assertEquals(1, next.setNumber)
    }

    @Test
    fun lastSetOfLastExerciseFinishesWithoutRest() {
        val done = start(reps("a", sets = 1)).completeSet()

        val finished = done.finished()
        assertFalse(finished.stopped)
        assertTrue(finished.showsSummary)
        assertEquals(SessionStatus.COMPLETED, finished.summary.status)
        assertEquals(1, finished.summary.completedSets)
        assertEquals(1, finished.summary.completedExercises)
    }

    @Test
    fun zeroRestSkipsTheRestScreen() {
        val next = start(reps("a", sets = 2, rest = 0)).completeSet()
        assertEquals(SessionPhase.Working(), next.phase)
        assertEquals(2, next.setNumber)
    }

    @Test
    fun repCountStopsAtTargetAndIgnoresTimedExercises() {
        var state = start(reps("a", reps = 2))
        repeat(5) { state = state.countRep() }
        assertEquals(2, state.working().repsDone)

        val countdown = start(timed("t"))
        assertSame(countdown, countdown.countRep())
    }

    @Test
    fun finishingASetCountsEvenWithoutTappingReps() {
        val rested = start(reps("a", reps = 10)).completeSet()
        assertEquals(1, rested.setsDone[0])
    }

    // endregion

    // region Timer

    @Test
    fun timedExerciseCountsDownAndOpensFailedSheetWhenTimeRunsOut() {
        val s = start(timed("t", seconds = 20))
        assertEquals(20_000L, s.working().remainingMillis)

        val almost = s.advance(19_999)
        assertEquals(1L, almost.working().remainingMillis)
        assertEquals(SessionOverlay.NONE, almost.overlay)

        val expired = almost.advance(1)
        assertEquals(SessionOverlay.FAILED, expired.overlay)
        assertEquals(0L, expired.working().remainingMillis)
        assertEquals(0, expired.setsDone[0])

        // Setelah habis, timer berhenti dan set tidak bisa diselesaikan dari belakang sheet.
        assertSame(expired, expired.advance(5_000))
        assertSame(expired, expired.completeSet())
    }

    @Test
    fun repExerciseHasNoCountdown() {
        val s = start(reps("a")).advance(60_000)
        assertNull(s.working().remainingMillis)
        assertEquals(SessionOverlay.NONE, s.overlay)
        assertEquals(60_000L, s.activeMillis)
    }

    @Test
    fun restCountsDownAndEndsIntoNextSet() {
        val rested = start(reps("a", sets = 2, rest = 30)).completeSet().advance(10_000)
        assertEquals(20_000L, rested.resting().remainingMillis)
        assertEquals(30_000L, rested.resting().totalMillis)
        assertEquals(SessionPhase.Working(), rested.advance(20_000).phase)
    }

    @Test
    fun pauseStopsTimersUntilResumed() {
        val paused = start(timed("t", seconds = 20)).togglePause()
        assertTrue(paused.paused)
        assertSame(paused, paused.advance(10_000))
        assertSame(paused, paused.completeSet())
        assertSame(paused, paused.requestSkip())

        val resumed = paused.togglePause().advance(10_000)
        assertEquals(10_000L, resumed.working().remainingMillis)
        assertEquals(10_000L, resumed.activeMillis)
    }

    @Test
    fun pauseAlsoFreezesRest() {
        val rested = start(reps("a")).completeSet().togglePause()
        assertSame(rested, rested.advance(10_000))
        assertSame(rested, rested.skipRest())
        assertSame(rested, rested.addRest())
    }

    @Test
    fun advancingInChunksEqualsOneBigStep() {
        // Rotasi dan kembali dari background hanya mengubah seberapa sering waktu masuk, bukan hasilnya.
        val s = start(timed("t", seconds = 20))
        var chunked = s
        repeat(7) { chunked = chunked.advance(1_000) }
        assertEquals(s.advance(7_000), chunked)
    }

    @Test
    fun nonPositiveDeltaIsIgnored() {
        val s = start(timed("t"))
        assertSame(s, s.advance(0))
        assertSame(s, s.advance(-5))
    }

    @Test
    fun addRestExtendsBothRemainingAndTotal() {
        val rested = start(reps("a")).completeSet().addRest()
        assertEquals(SessionPhase.Resting(45_000, 45_000), rested.phase)
    }

    // endregion

    // region Gagal

    @Test
    fun skipOpensFailedSheetAndStopsTimer() {
        val failed = start(timed("t")).requestSkip()
        assertEquals(SessionOverlay.FAILED, failed.overlay)
        assertFalse(failed.isRunning)
        assertSame(failed, failed.advance(5_000))
    }

    @Test
    fun retryRepeatsTheSameSetFromTheStart() {
        val timedRetry = start(timed("t", seconds = 20)).advance(8_000).requestSkip().retry()
        assertEquals(SessionOverlay.NONE, timedRetry.overlay)
        assertEquals(20_000L, timedRetry.working().remainingMillis)
        assertEquals(1, timedRetry.setNumber)

        var counting = start(reps("a"))
        repeat(3) { counting = counting.countRep() }
        val repsRetry = counting.requestSkip().retry()
        assertEquals(0, repsRetry.working().repsDone)
        assertEquals(1, repsRetry.setNumber)
    }

    @Test
    fun regressReplacesOnlyTheCurrentExerciseInTheSession() {
        val easier = exercise("b").copy(reps = 8, restSeconds = 45)
        val original = plan(reps("a", sets = 3, regressionId = "b"), reps("c"), extra = listOf(easier))
        val afterOneSet = SessionState.start(original).completeSet().advance(30_000)

        val failed = afterOneSet.requestSkip()
        assertEquals("b", failed.regression?.id)

        val regressed = failed.regress()
        assertEquals("b", regressed.current.exercise.id)
        assertEquals(3, regressed.current.sets)
        assertEquals(8, regressed.current.reps)
        assertEquals(45, regressed.current.restSeconds)
        // Set yang sudah selesai tetap dihitung dan lanjut di set 2.
        assertEquals(2, regressed.setNumber)
        assertEquals(1, regressed.setsDone[0])
        assertEquals(SessionOverlay.NONE, regressed.overlay)
        assertEquals(SessionPhase.Working(), regressed.phase)
        // Rutinitas asli dan latihan lain tidak berubah.
        assertEquals("a", regressed.plan.exercises[0].exercise.id)
        assertEquals("c", regressed.exercises[1].exercise.id)
    }

    @Test
    fun regressToTimedExerciseStartsACountdown() {
        val hold = exercise("hold").copy(reps = null, durationSeconds = 25)
        val failed = SessionState.start(plan(reps("a", regressionId = "hold"), extra = listOf(hold))).requestSkip()

        val regressed = failed.regress()
        assertTrue(regressed.current.isTimed)
        assertEquals(25_000L, regressed.working().remainingMillis)
    }

    @Test
    fun regressIsUnavailableWithoutAKnownRegression() {
        val none = start(reps("a")).requestSkip()
        assertNull(none.regression)
        assertSame(none, none.regress())

        val dangling = SessionState.start(plan(reps("a", regressionId = "hilang"))).requestSkip()
        assertNull(dangling.regression)
        assertSame(dangling, dangling.regress())
    }

    @Test
    fun skipExerciseMovesOnWithoutRestAndLeavesSetsUncounted() {
        val next = start(reps("a", sets = 2), reps("b")).requestSkip().skipExercise()

        assertEquals(1, next.index)
        assertEquals(SessionPhase.Working(), next.phase)
        assertEquals(SessionOverlay.NONE, next.overlay)
        assertEquals(listOf(0, 0), next.setsDone)
    }

    @Test
    fun skippingTheLastExerciseEndsThePartialSession() {
        val partial = start(reps("a", sets = 2)).completeSet().skipExercise()
        // Tanpa sheet Gagal terbuka, lewati latihan tidak melakukan apa pun.
        assertIs<SessionPhase.Resting>(partial.phase)

        val ended = start(reps("a", sets = 2)).completeSet().advance(30_000).requestSkip().skipExercise()
        val finished = ended.finished()
        assertEquals(SessionStatus.PARTIAL, finished.summary.status)
        assertEquals(1, finished.summary.completedSets)
        assertEquals(2, finished.summary.totalSets)
        assertEquals(0, finished.summary.completedExercises)
        assertTrue(finished.showsSummary)
    }

    @Test
    fun skippingEverythingEndsAbandonedWithoutSummary() {
        val finished = start(reps("a")).requestSkip().skipExercise().finished()
        assertEquals(SessionStatus.ABANDONED, finished.summary.status)
        assertFalse(finished.showsSummary)
    }

    // endregion

    // region Hentikan

    @Test
    fun stopAsksConfirmationAndFreezesTimers() {
        val asking = start(timed("t")).requestStop()
        assertEquals(SessionOverlay.CONFIRM_STOP, asking.overlay)
        assertSame(asking, asking.advance(5_000))

        val resumed = asking.dismissStop()
        assertEquals(SessionOverlay.NONE, resumed.overlay)
        assertTrue(resumed.isRunning)
    }

    @Test
    fun stopWithCompletedSetsIsPartial() {
        val stopped = start(reps("a", sets = 2)).completeSet().advance(30_000).requestStop().confirmStop()

        val finished = stopped.finished()
        assertTrue(finished.stopped)
        assertFalse(finished.showsSummary)
        assertEquals(SessionStatus.PARTIAL, finished.summary.status)
        assertEquals(1, finished.summary.completedSets)
        assertEquals(2, finished.summary.totalSets)
    }

    @Test
    fun stopWithoutCompletedSetsIsAbandoned() {
        val finished = start(reps("a")).requestStop().confirmStop().finished()
        assertTrue(finished.stopped)
        assertEquals(SessionStatus.ABANDONED, finished.summary.status)
        assertEquals(0, finished.summary.completedSets)
    }

    @Test
    fun confirmStopRequiresTheDialog() {
        val s = start(reps("a"))
        assertSame(s, s.confirmStop())
    }

    @Test
    fun stopSummaryIsAvailableBeforeLeavingTheSession() {
        // ViewModel menyimpan log dari ringkasan ini sebelum sesi benar-benar ditutup.
        val asking = start(reps("a", sets = 2)).completeSet().advance(30_000).requestStop()
        assertEquals(SessionStatus.PARTIAL, asking.summarize().status)
        assertIs<SessionPhase.Working>(asking.phase)
    }

    // endregion

    // region Ringkasan

    @Test
    fun minutesComeFromRunningTimeRoundedAndAtLeastOneWhenSetsAreDone() {
        val long = start(reps("a", sets = 1)).advance(42 * 60_000L).completeSet().finished()
        assertEquals(42, long.summary.minutes)

        val short = start(reps("a", sets = 1)).advance(5_000).completeSet().finished()
        assertEquals(1, short.summary.minutes)

        val none = start(reps("a")).advance(5_000).requestStop().confirmStop().finished()
        assertEquals(0, none.summary.minutes)
    }

    @Test
    fun pausedTimeDoesNotCountTowardsMinutes() {
        val s = start(reps("a", sets = 1)).advance(60_000).togglePause().advance(600_000).togglePause().advance(60_000)
        assertEquals(120_000L, s.activeMillis)
    }

    @Test
    fun fullSessionCountsEveryExerciseAndSet() {
        var s = start(reps("a", sets = 2), timed("t", sets = 1, seconds = 10))
        repeat(2) { s = s.completeSet().advance(30_000) }
        s = s.completeSet()

        val summary = s.finished().summary
        assertEquals(SessionStatus.COMPLETED, summary.status)
        assertEquals(3, summary.completedSets)
        assertEquals(3, summary.totalSets)
        assertEquals(2, summary.completedExercises)
        assertEquals(2, summary.totalExercises)
    }

    @Test
    fun nothingHappensAfterFinish() {
        val done = start(reps("a", sets = 1)).completeSet()
        assertSame(done, done.advance(1_000))
        assertSame(done, done.completeSet())
        assertSame(done, done.requestSkip())
        assertSame(done, done.togglePause())
        assertSame(done, done.requestStop())
    }

    // endregion
}
