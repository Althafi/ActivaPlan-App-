package com.activaplan.app.presentation.routines

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.activaplan.app.designsystem.ActivaDimens
import com.activaplan.app.designsystem.ActivaPlanTheme
import com.activaplan.app.designsystem.components.ActivaBackButton
import com.activaplan.app.designsystem.components.ActivaButton
import com.activaplan.app.designsystem.components.ActivaCard
import com.activaplan.app.designsystem.components.ActivaIcon
import com.activaplan.app.designsystem.components.ActivaIconKind
import com.activaplan.app.designsystem.components.ActivaSnackbarHost
import com.activaplan.app.designsystem.components.AlertType
import com.activaplan.app.designsystem.components.ButtonStyle
import com.activaplan.app.designsystem.components.LevelBadge
import com.activaplan.app.designsystem.components.TextBadge
import com.activaplan.app.designsystem.components.ZoneChip
import com.activaplan.app.designsystem.components.showActiva
import com.activaplan.app.domain.i18n.StringKey
import com.activaplan.app.domain.model.BoardZone
import com.activaplan.app.domain.model.Category
import com.activaplan.app.domain.model.Difficulty
import com.activaplan.app.domain.model.Exercise
import com.activaplan.app.domain.model.Mode
import com.activaplan.app.domain.model.TemplateDetail
import com.activaplan.app.domain.usecase.CreateRoutineFromTemplateUseCase
import com.activaplan.app.domain.usecase.GetTemplateDetailUseCase
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

/** Pratinjau template dari Beranda atau daftar. [onUsed] menerima ID rutinitas salinan dan membuka Builder untuknya. */
@Composable
fun TemplatePreviewScreen(
    id: String,
    onBack: () -> Unit,
    onUsed: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val getDetail = koinInject<GetTemplateDetailUseCase>()
    val createFromTemplate = koinInject<CreateRoutineFromTemplateUseCase>()
    val strings = ActivaPlanTheme.strings
    val viewModel = viewModel(key = "template-$id-${strings.language.code}") {
        TemplatePreviewViewModel(id, strings.language, getDetail, createFromTemplate)
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel, strings) {
        viewModel.events.collect { event ->
            when (event) {
                is TemplateUseEvent.Copied -> onUsed(event.routineId)
                TemplateUseEvent.Unavailable ->
                    snackbarHostState.showActiva(strings[StringKey.TEMPLATE_NOT_FOUND], AlertType.WARNING)
                is TemplateUseEvent.Failed -> {
                    val retry = snackbarHostState.showActiva(
                        strings[StringKey.ALERT_SAVE_FAILED], AlertType.ERROR, strings[StringKey.ACTION_RETRY],
                    )
                    if (retry) event.retry()
                }
            }
        }
    }
    TemplatePreviewContent(state, onBack, viewModel::use, viewModel::reload, snackbarHostState, modifier)
}

@Composable
fun TemplatePreviewContent(
    state: TemplatePreviewUiState,
    onBack: () -> Unit,
    onUse: () -> Unit,
    onReload: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val strings = ActivaPlanTheme.strings
    Box(modifier.fillMaxSize().background(ActivaPlanTheme.colors.bg)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Row(Modifier.padding(horizontal = ActivaDimens.screenPadding).padding(top = 8.dp, bottom = 12.dp)) {
                ActivaBackButton(onBack)
            }
            when (state) {
                TemplatePreviewUiState.Loading -> Unit
                TemplatePreviewUiState.Error -> Message(strings[StringKey.ALERT_LOAD_FAILED], strings[StringKey.ACTION_RELOAD], onReload)
                TemplatePreviewUiState.NotFound -> Message(strings[StringKey.TEMPLATE_NOT_FOUND])
                is TemplatePreviewUiState.Ready -> ReadyContent(state.detail, onUse)
            }
        }
        ActivaSnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 96.dp))
    }
}

@Composable
private fun ColumnScope.ReadyContent(detail: TemplateDetail, onUse: () -> Unit) {
    val c = ActivaPlanTheme.colors
    val strings = ActivaPlanTheme.strings
    Column(
        Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = ActivaDimens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(detail.name, style = ActivaPlanTheme.type.largeTitle, color = c.text)
        LevelBadge(detail.difficulty)
        Text(
            strings.routineMeta(detail.exercises.size, detail.estimatedMinutes),
            style = ActivaPlanTheme.type.body,
            color = c.textMuted,
        )
        Spacer(Modifier.height(4.dp))
        Text(strings[StringKey.TEMPLATE_EXERCISES], style = ActivaPlanTheme.type.cardTitle, color = c.text)
        detail.exercises.forEach { ExerciseRow(it) }
        Spacer(Modifier.height(12.dp))
    }
    Surface(color = c.surface, border = BorderStroke(1.dp, c.border), shape = RectangleShape) {
        Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = ActivaDimens.screenPadding, vertical = 14.dp)) {
            ActivaButton(strings[StringKey.ROUTINES_USE], onUse, Modifier.fillMaxWidth(), icon = ActivaIconKind.PLUS)
        }
    }
}

@Composable
private fun ExerciseRow(exercise: Exercise) {
    val c = ActivaPlanTheme.colors
    val strings = ActivaPlanTheme.strings
    ActivaCard(Modifier.fillMaxWidth()) {
        Text(exercise.name, style = ActivaPlanTheme.type.cardTitle.copy(fontSize = 15.sp), color = c.text)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            LevelBadge(exercise.difficulty)
            val zone = exercise.boardZone
            if (zone != null) ZoneChip(zone) else TextBadge(strings.label(exercise.category))
        }
        Spacer(Modifier.height(6.dp))
        Text(
            strings.summary(exercise),
            style = ActivaPlanTheme.type.body.copy(fontSize = 13.sp, lineHeight = 18.sp),
            color = c.textMuted,
        )
    }
}

@Composable
private fun Message(text: String, actionLabel: String? = null, onAction: () -> Unit = {}) {
    val c = ActivaPlanTheme.colors
    Column(
        Modifier.fillMaxSize().padding(ActivaDimens.screenPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ActivaIcon(ActivaIconKind.WARNING, c.textMuted, size = 32.dp)
        Spacer(Modifier.height(12.dp))
        Text(text, style = ActivaPlanTheme.type.body, color = c.textMuted, textAlign = TextAlign.Center)
        if (actionLabel != null) {
            Spacer(Modifier.height(16.dp))
            ActivaButton(actionLabel, onAction, style = ButtonStyle.SOFT)
        }
    }
}

private fun previewExercise(id: String, name: String, difficulty: Difficulty, zone: BoardZone?, reps: Int) = Exercise(
    id = id, name = name, mode = Mode.PUSHUP_BOARD, category = Category.CHEST, difficulty = difficulty,
    boardZone = zone, primaryMuscles = emptyList(), sets = 3, reps = reps, restSeconds = 60,
    steps = emptyList(), tips = emptyList(), commonMistakes = emptyList(),
)

private val previewDetail = TemplateDetail(
    id = "t1",
    name = "Dada & Triceps Sedang",
    difficulty = Difficulty.MEDIUM,
    exercises = listOf(
        previewExercise("a", "Push-up Dada Standar", Difficulty.MEDIUM, BoardZone.BLUE, 10),
        previewExercise("b", "Push-up Triceps", Difficulty.MEDIUM, BoardZone.GREEN, 10),
        previewExercise("c", "Push-up Zona Kuning dengan Jeda", Difficulty.HARD, BoardZone.YELLOW, 8),
    ),
    estimatedMinutes = 38,
)

@Composable
private fun TemplatePreview(darkTheme: Boolean, state: TemplatePreviewUiState) = ActivaPlanTheme(darkTheme = darkTheme) {
    TemplatePreviewContent(state, {}, {}, {}, remember { SnackbarHostState() })
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun TemplatePreviewLightPreview() = TemplatePreview(false, TemplatePreviewUiState.Ready(previewDetail))

@Preview(widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun TemplatePreviewDarkPreview() = TemplatePreview(true, TemplatePreviewUiState.Ready(previewDetail))
