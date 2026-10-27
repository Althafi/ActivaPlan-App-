package com.activaplan.app.presentation.session

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.activaplan.app.designsystem.ActivaDimens
import com.activaplan.app.designsystem.ActivaPlanTheme
import com.activaplan.app.domain.model.BoardZone
import com.activaplan.app.domain.model.Category
import com.activaplan.app.domain.model.Difficulty
import com.activaplan.app.domain.model.Exercise
import com.activaplan.app.domain.model.Mode
import com.activaplan.app.domain.model.SessionExercise
import com.activaplan.app.domain.model.SessionPhase
import com.activaplan.app.domain.model.SessionPlan
import com.activaplan.app.domain.model.SessionStatus
import com.activaplan.app.domain.model.SessionSummary
import com.activaplan.app.domain.session.SessionState
import org.jetbrains.compose.ui.tooling.preview.Preview

private val pushUpKnee = Exercise(
    id = "pb_chest_knee_pushup", name = "Push-up dari Lutut", mode = Mode.PUSHUP_BOARD, category = Category.CHEST,
    difficulty = Difficulty.EASY, boardZone = BoardZone.BLUE, primaryMuscles = listOf("chest"),
    sets = 3, reps = 8, restSeconds = 45, steps = listOf("Tumpu pada lutut."), tips = emptyList(), commonMistakes = emptyList(),
)

private val pushUp = Exercise(
    id = "pb_chest_pushup", name = "Push-up Dada Standar", mode = Mode.PUSHUP_BOARD, category = Category.CHEST,
    difficulty = Difficulty.MEDIUM, boardZone = BoardZone.BLUE, primaryMuscles = listOf("chest"),
    sets = 3, reps = 10, restSeconds = 60,
    steps = listOf("Tangan pada lingkaran biru."),
    tips = listOf("Turunkan dada terkontrol 3 detik, siku sekitar 45°."),
    commonMistakes = emptyList(), regressionId = pushUpKnee.id,
)

private val plank = Exercise(
    id = "plank", name = "Plank", mode = Mode.NO_EQUIPMENT, category = Category.ABS,
    difficulty = Difficulty.EASY, primaryMuscles = listOf("abs"), sets = 3, durationSeconds = 30, restSeconds = 45,
    steps = listOf("Badan lurus dari kepala sampai tumit."), tips = emptyList(), commonMistakes = emptyList(),
)

private val ropeHold = Exercise(
    id = "rope_grip_hold", name = "Tahan Genggaman Tali", mode = Mode.BOARD_ROPE, category = Category.ARMS,
    difficulty = Difficulty.EASY, primaryMuscles = listOf("forearms"), sets = 3, durationSeconds = 20, restSeconds = 45,
    steps = listOf("Genggam tali dengan kedua tangan."), tips = emptyList(), commonMistakes = emptyList(),
)

private fun plan(safety: Boolean = false) = SessionPlan(
    routineId = "r1",
    routineName = "Board Pemula",
    exercises = listOf(
        SessionExercise(plank, 3, null, 30, 45),
        SessionExercise(pushUp, 3, 10, null, 60),
        SessionExercise(plank, 3, null, 30, 45),
        SessionExercise(pushUp, 3, 10, null, 60),
        SessionExercise(ropeHold, 3, null, 20, 45),
    ),
    catalog = listOf(pushUp, pushUpKnee, plank, ropeHold).associateBy { it.id },
    requiresSafetyCheck = safety,
    safetyNotes = listOf(
        "Letakkan papan di lantai datar dan tidak licin; pastikan tidak bergeser sebelum mulai.",
        "Periksa tali dari retak atau serat terkelupas sebelum dipakai.",
    ),
    skippedItems = 0,
)

/** Latihan 2 dari 5, set 2 dari 3, 7 dari 10 repetisi, seperti di Figma. */
private fun working() =
    SessionState.start(plan()).copy(index = 1, setsDone = listOf(3, 1, 0, 0, 0), phase = SessionPhase.Working(repsDone = 7))

private fun timed() = SessionState.start(plan()).copy(
    setsDone = listOf(1, 0, 0, 0, 0), phase = SessionPhase.Working(remainingMillis = 21_000),
)

private fun resting() = working().copy(
    setsDone = listOf(3, 2, 0, 0, 0), phase = SessionPhase.Resting(remainingMillis = 38_000, totalMillis = 60_000),
)

private val completeSummary = SessionSummary(SessionStatus.COMPLETED, 14, 14, 5, 5, 42)

private val previewActions = SessionActions({}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})

@Composable
private fun SessionPreview(darkTheme: Boolean, state: SessionUiState) = ActivaPlanTheme(darkTheme = darkTheme) {
    SessionContent(state, previewActions, remember { SnackbarHostState() })
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun WorkingLightPreview() = SessionPreview(false, SessionUiState.Ready(working()))

@Preview(widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun WorkingDarkPreview() = SessionPreview(true, SessionUiState.Ready(working()))

@Preview(widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun WorkingTimedLightPreview() = SessionPreview(false, SessionUiState.Ready(timed()))

@Preview(widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun WorkingPausedDarkPreview() = SessionPreview(true, SessionUiState.Ready(working().copy(paused = true)))

@Preview(widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun RestLightPreview() = SessionPreview(false, SessionUiState.Ready(resting()))

@Preview(widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun RestDarkPreview() = SessionPreview(true, SessionUiState.Ready(resting()))

@Preview(widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun SafetyCheckLightPreview() = SessionPreview(false, SessionUiState.Ready(SessionState.start(plan(safety = true))))

@Preview(widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun SafetyCheckDarkPreview() = SessionPreview(true, SessionUiState.Ready(SessionState.start(plan(safety = true))))

@Preview(widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun CompleteLightPreview() = SessionPreview(
    false,
    SessionUiState.Ready(working().copy(phase = SessionPhase.Finished(completeSummary, stopped = false)), streak = 5),
)

@Preview(widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun CompleteDarkPreview() = SessionPreview(
    true,
    SessionUiState.Ready(working().copy(phase = SessionPhase.Finished(completeSummary, stopped = false)), streak = 5),
)

@Preview(widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun CompletePartialLightPreview() = SessionPreview(
    false,
    SessionUiState.Ready(
        working().copy(
            phase = SessionPhase.Finished(SessionSummary(SessionStatus.PARTIAL, 9, 14, 3, 5, 20), stopped = false),
        ),
    ),
)

@Composable
private fun FailedSheetPreview(darkTheme: Boolean, regression: Exercise?) = ActivaPlanTheme(darkTheme = darkTheme) {
    Column(Modifier.background(ActivaPlanTheme.colors.surface).padding(ActivaDimens.screenPadding)) {
        FailedSheetContent(regression, previewActions)
    }
}

@Preview(widthDp = 390, showBackground = true)
@Composable
private fun FailedSheetLightPreview() = FailedSheetPreview(false, pushUpKnee)

@Preview(widthDp = 390, showBackground = true)
@Composable
private fun FailedSheetNoEasierDarkPreview() = FailedSheetPreview(true, null)
