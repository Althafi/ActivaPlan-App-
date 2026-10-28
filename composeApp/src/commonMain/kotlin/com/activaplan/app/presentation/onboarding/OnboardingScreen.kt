package com.activaplan.app.presentation.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.activaplan.app.designsystem.ActivaDimens
import com.activaplan.app.designsystem.ActivaPlanTheme
import com.activaplan.app.designsystem.ActivaShapes
import com.activaplan.app.designsystem.components.ActivaBackButton
import com.activaplan.app.designsystem.components.ActivaButton
import com.activaplan.app.designsystem.components.ActivaIcon
import com.activaplan.app.designsystem.components.ActivaIconKind
import com.activaplan.app.designsystem.components.ActivaSegmentedControl
import com.activaplan.app.designsystem.components.ActivaSnackbarHost
import com.activaplan.app.designsystem.components.AlertType
import com.activaplan.app.designsystem.components.showActiva
import com.activaplan.app.domain.i18n.StringKey
import com.activaplan.app.domain.model.Difficulty
import com.activaplan.app.domain.model.Mode
import com.activaplan.app.domain.model.OnboardingChoices
import com.activaplan.app.domain.model.Settings
import com.activaplan.app.domain.reminder.NotificationPermission
import com.activaplan.app.domain.time.formatClock
import com.activaplan.app.domain.usecase.CompleteOnboardingUseCase
import com.activaplan.app.domain.usecase.ObserveSettingsUseCase
import com.activaplan.app.presentation.rememberNotificationPermissionRequester
import com.activaplan.app.presentation.schedule.TimePickerDialog
import com.activaplan.app.presentation.schedule.reminderSwitchColors
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

/** Aksi layar Onboarding, dipisah dari tampilan agar pratinjau tidak perlu ViewModel. */
class OnboardingActions(
    val onBack: () -> Unit,
    val onLevel: (Difficulty) -> Unit,
    val onToggleMode: (Mode) -> Unit,
    val onReminders: (Boolean) -> Unit,
    val onReminderTime: (Int) -> Unit,
    val onStart: () -> Unit,
    val onLater: () -> Unit,
)

private val levels = listOf(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD)
private val modes = listOf(Mode.NO_EQUIPMENT, Mode.PUSHUP_BOARD, Mode.BOARD_ROPE)

/** Layar "Atur awal sebentar". [onBack] kembali ke Perkenalan 3; selesai (atau "Nanti saja") ditangani `App` lewat `Settings.onboardingDone`. */
@Composable
fun OnboardingScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val observeSettings = koinInject<ObserveSettingsUseCase>()
    val complete = koinInject<CompleteOnboardingUseCase>()
    val permission = koinInject<NotificationPermission>()
    val strings = ActivaPlanTheme.strings
    val viewModel = viewModel(key = "onboarding") { OnboardingViewModel(observeSettings, complete, permission) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val requestPermission = rememberNotificationPermissionRequester(viewModel::onPermissionResult)

    LaunchedEffect(viewModel, strings) {
        viewModel.events.collect { event ->
            when (event) {
                OnboardingEvent.RequestPermission -> requestPermission()
                OnboardingEvent.PermissionDenied ->
                    launch { snackbarHostState.showActiva(strings[StringKey.ONBOARD_REMINDER_DENIED], AlertType.INFO) }
                is OnboardingEvent.Failed -> launch {
                    val retry = snackbarHostState.showActiva(
                        strings[StringKey.ALERT_SAVE_FAILED], AlertType.ERROR, strings[StringKey.ACTION_RETRY],
                    )
                    if (retry) event.retry()
                }
            }
        }
    }

    val actions = OnboardingActions(
        onBack = onBack,
        onLevel = viewModel::onLevel,
        onToggleMode = viewModel::onToggleMode,
        onReminders = viewModel::onReminders,
        onReminderTime = viewModel::onReminderTime,
        onStart = viewModel::onStart,
        onLater = viewModel::onLater,
    )
    OnboardingContent(state, actions, snackbarHostState, modifier)
}

@Composable
fun OnboardingContent(
    state: OnboardingUiState,
    actions: OnboardingActions,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val c = ActivaPlanTheme.colors
    val strings = ActivaPlanTheme.strings
    val choices = state.choices
    var pickingTime by remember { mutableStateOf(false) }

    Box(modifier.fillMaxSize().background(c.bg).statusBarsPadding().navigationBarsPadding()) {
        Column(Modifier.fillMaxSize()) {
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState())
                    .padding(horizontal = ActivaDimens.screenPadding).padding(top = 8.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                ActivaBackButton(actions.onBack)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(strings[StringKey.ONBOARD_TITLE], style = ActivaPlanTheme.type.largeTitle, color = c.text)
                    Text(strings[StringKey.ONBOARD_SUBTITLE], style = ActivaPlanTheme.type.body, color = c.textMuted)
                }

                Section(strings[StringKey.ONBOARD_LEVEL]) {
                    ActivaSegmentedControl(
                        options = levels.map { strings.label(it) },
                        selectedIndex = levels.indexOf(choices.startLevel),
                        onSelect = { actions.onLevel(levels[it]) },
                    )
                }

                Section(strings[StringKey.ONBOARD_EQUIPMENT]) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        modes.forEach { mode ->
                            ModeCard(strings.label(mode), mode in choices.ownedModes) { actions.onToggleMode(mode) }
                        }
                    }
                    if (Mode.BOARD_ROPE in choices.ownedModes) RopeNote()
                }

                Section(strings[StringKey.ONBOARD_REMINDER]) {
                    ReminderCard(choices, actions.onReminders) { pickingTime = true }
                }
            }

            Column(
                Modifier.fillMaxWidth().padding(horizontal = ActivaDimens.screenPadding).padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (choices.ownedModes.isEmpty()) {
                    Text(
                        strings[StringKey.ONBOARD_PICK_ONE],
                        style = ActivaPlanTheme.type.label,
                        color = c.error,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
                ActivaButton(
                    text = strings[StringKey.ONBOARD_CTA],
                    onClick = actions.onStart,
                    enabled = choices.isValid && !state.saving,
                    modifier = Modifier.fillMaxWidth(),
                )
                Box(
                    Modifier.defaultMinSize(minHeight = ActivaDimens.minTouchTarget)
                        .clickable(enabled = !state.saving, role = Role.Button, onClick = actions.onLater)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(strings[StringKey.ONBOARD_LATER], style = ActivaPlanTheme.type.cardTitle, color = c.textMuted)
                }
            }
        }
        ActivaSnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter).padding(bottom = 132.dp))
    }

    if (pickingTime) {
        TimePickerDialog(
            initialMinutes = choices.reminderTimeMinutes,
            onConfirm = { actions.onReminderTime(it); pickingTime = false },
            onDismiss = { pickingTime = false },
        )
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = ActivaPlanTheme.type.cardTitle, color = ActivaPlanTheme.colors.text)
        content()
    }
}

/** Kartu pilih ganda: border dan latar primary saat dipilih, ditambah tanda centang (bukan hanya warna). */
@Composable
private fun ModeCard(label: String, selected: Boolean, onToggle: () -> Unit) {
    val c = ActivaPlanTheme.colors
    Surface(
        color = if (selected) c.primarySoft else c.surface,
        contentColor = c.text,
        shape = ActivaShapes.card,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) c.primary else c.border),
        modifier = Modifier.fillMaxWidth().toggleable(value = selected, role = Role.Checkbox, onValueChange = { onToggle() }),
    ) {
        Row(
            Modifier.defaultMinSize(minHeight = 60.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = ActivaPlanTheme.type.cardTitle, color = c.text, modifier = Modifier.weight(1f))
            Box(
                Modifier.size(24.dp).background(if (selected) c.primary else c.surfaceVar, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) ActivaIcon(ActivaIconKind.CHECK, c.onPrimary, size = 16.dp)
            }
        }
    }
}

@Composable
private fun RopeNote() {
    val c = ActivaPlanTheme.colors
    Row(
        Modifier.fillMaxWidth().background(c.warningSoft, ActivaShapes.button).padding(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        ActivaIcon(ActivaIconKind.WARNING, c.text, size = 20.dp)
        Spacer(Modifier.width(10.dp))
        Text(ActivaPlanTheme.strings[StringKey.ONBOARD_ROPE_NOTE], style = ActivaPlanTheme.type.body, color = c.text)
    }
}

/** Seluruh baris bisa disentuh; [Switch] hanya penanda. Baris jam muncul saat pengingat menyala. */
@Composable
private fun ReminderCard(choices: OnboardingChoices, onToggle: (Boolean) -> Unit, onPickTime: () -> Unit) {
    val c = ActivaPlanTheme.colors
    val strings = ActivaPlanTheme.strings
    Surface(
        color = c.surface,
        contentColor = c.text,
        shape = ActivaShapes.card,
        border = BorderStroke(1.dp, c.border),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            Row(
                Modifier.fillMaxWidth().defaultMinSize(minHeight = 56.dp)
                    .toggleable(value = choices.remindersOn, role = Role.Switch, onValueChange = onToggle)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ActivaIcon(ActivaIconKind.BELL, c.primary, size = 22.dp)
                Spacer(Modifier.width(12.dp))
                Text(strings[StringKey.ONBOARD_REMINDER], style = ActivaPlanTheme.type.cardTitle, color = c.text, modifier = Modifier.weight(1f))
                Switch(checked = choices.remindersOn, onCheckedChange = null, colors = reminderSwitchColors())
            }
            if (choices.remindersOn) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
                Row(
                    Modifier.fillMaxWidth().defaultMinSize(minHeight = 52.dp)
                        .clickable(role = Role.Button, onClick = onPickTime)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(strings[StringKey.ONBOARD_REMINDER_TIME], style = ActivaPlanTheme.type.cardTitle, color = c.text, modifier = Modifier.weight(1f))
                    Text(
                        formatClock(choices.reminderTimeMinutes),
                        style = ActivaPlanTheme.type.body.copy(fontWeight = FontWeight.SemiBold),
                        color = c.primary,
                    )
                }
            }
        }
    }
}

// region Pratinjau

private val previewActions = OnboardingActions({}, {}, {}, {}, {}, {}, {})

@Composable
private fun OnboardingPreview(darkTheme: Boolean, choices: OnboardingChoices) = ActivaPlanTheme(darkTheme = darkTheme) {
    OnboardingContent(OnboardingUiState(choices), previewActions, remember { SnackbarHostState() })
}

private val previewDefault = OnboardingChoices.initial(Settings.DEFAULT.defaultTimeMinutes)

@Preview(widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun OnboardingDefaultLightPreview() = OnboardingPreview(false, previewDefault)

@Preview(widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun OnboardingDefaultDarkPreview() = OnboardingPreview(true, previewDefault)

@Preview(widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun OnboardingRopeReminderLightPreview() = OnboardingPreview(
    false, previewDefault.copy(startLevel = Difficulty.MEDIUM, ownedModes = setOf(Mode.PUSHUP_BOARD, Mode.BOARD_ROPE), remindersOn = true),
)

@Preview(widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun OnboardingNoGearDarkPreview() = OnboardingPreview(true, previewDefault.copy(ownedModes = emptySet()))

// endregion
