package com.activaplan.app.presentation.schedule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.activaplan.app.designsystem.ActivaDimens
import com.activaplan.app.designsystem.ActivaPlanTheme
import com.activaplan.app.designsystem.ActivaShapes
import com.activaplan.app.designsystem.components.ActivaBottomSheet
import com.activaplan.app.designsystem.components.ActivaButton
import com.activaplan.app.designsystem.components.ActivaDialog
import com.activaplan.app.designsystem.components.ActivaDialogCard
import com.activaplan.app.designsystem.components.ActivaIcon
import com.activaplan.app.designsystem.components.ActivaIconKind
import com.activaplan.app.designsystem.components.ActivaSnackbarHost
import com.activaplan.app.designsystem.components.AlertType
import com.activaplan.app.designsystem.components.ButtonStyle
import com.activaplan.app.designsystem.components.LevelBadge
import com.activaplan.app.designsystem.components.showActiva
import com.activaplan.app.domain.i18n.StringKey
import com.activaplan.app.domain.i18n.Strings
import com.activaplan.app.domain.model.ConflictPolicy
import com.activaplan.app.domain.model.Difficulty
import com.activaplan.app.domain.model.RoutineOption
import com.activaplan.app.domain.model.ScheduleData
import com.activaplan.app.domain.model.ScheduleDraft
import com.activaplan.app.domain.model.ScheduleItem
import com.activaplan.app.domain.model.ScheduleWeekDay
import com.activaplan.app.domain.reminder.PermissionState
import com.activaplan.app.domain.time.formatClock
import com.activaplan.app.domain.usecase.DeleteScheduleEntryUseCase
import com.activaplan.app.domain.usecase.ObserveScheduleUseCase
import com.activaplan.app.domain.usecase.PruneScheduleUseCase
import com.activaplan.app.domain.usecase.SaveScheduleUseCase
import com.activaplan.app.domain.usecase.SetScheduleReminderUseCase
import com.activaplan.app.domain.reminder.NotificationPermission
import com.activaplan.app.domain.reminder.ReminderSynchronizer
import com.activaplan.app.presentation.rememberNotificationPermissionRequester
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

/** Aksi layar Jadwal, dipisah dari tampilan agar pratinjau tidak perlu ViewModel. */
class ScheduleActions(
    val onSelectDay: (Int) -> Unit,
    val onAdd: () -> Unit,
    val onEdit: (ScheduleItem) -> Unit,
    val onToggleReminder: (ScheduleItem, Boolean) -> Unit,
    val onOpenSettings: () -> Unit,
    val onReload: () -> Unit,
)

/** Sheet Tambah/Ubah yang sedang terbuka. */
private sealed interface SheetTarget {
    data object New : SheetTarget
    data class Edit(val item: ScheduleItem) : SheetTarget
}

/** Layar Jadwal: pemilih pekan, jadwal hari terpilih, dan sheet Tambah/Ubah. [onCreateRoutine] membuka Builder bila belum ada rutinitas. */
@Composable
fun ScheduleScreen(onCreateRoutine: () -> Unit, modifier: Modifier = Modifier) {
    val observe = koinInject<ObserveScheduleUseCase>()
    val save = koinInject<SaveScheduleUseCase>()
    val delete = koinInject<DeleteScheduleEntryUseCase>()
    val setReminder = koinInject<SetScheduleReminderUseCase>()
    val prune = koinInject<PruneScheduleUseCase>()
    val permission = koinInject<NotificationPermission>()
    val synchronizer = koinInject<ReminderSynchronizer>()
    val strings = ActivaPlanTheme.strings
    val viewModel = viewModel(key = "schedule") {
        ScheduleViewModel(observe, save, delete, setReminder, prune, permission, synchronizer)
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionState by viewModel.permissionState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedDay by rememberSaveable { mutableStateOf<Int?>(null) }
    var sheet by remember { mutableStateOf<SheetTarget?>(null) }
    var conflictDraft by remember { mutableStateOf<ScheduleDraft?>(null) }
    var deleteTarget by remember { mutableStateOf<ScheduleItem?>(null) }

    val requestPermission = rememberNotificationPermissionRequester(viewModel::onPermissionResult)
    // Pengguna bisa mengubah izin di pengaturan sistem lalu kembali ke aplikasi.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshPermission() }

    LaunchedEffect(viewModel, strings) {
        viewModel.events.collect { event ->
            when (event) {
                is ScheduleEvent.Saved -> {
                    sheet = null
                    conflictDraft = null
                    val days = event.days.joinToString(", ") { strings.dayName(it) }
                    // Snackbar menunggu 4 detik; diluncurkan terpisah agar peristiwa berikutnya tidak tertahan.
                    launch { snackbarHostState.showActiva(strings.format(StringKey.ALERT_SCHEDULE_SAVED, days, formatClock(event.timeMinutes))) }
                }
                is ScheduleEvent.Conflict -> conflictDraft = event.draft
                ScheduleEvent.Deleted -> {
                    sheet = null
                    launch { snackbarHostState.showActiva(strings[StringKey.ALERT_SCHEDULE_DELETED]) }
                }
                is ScheduleEvent.Invalid ->
                    launch { snackbarHostState.showActiva(strings[event.message], AlertType.WARNING) }
                ScheduleEvent.RequestPermission -> requestPermission()
                is ScheduleEvent.Failed -> launch {
                    val retry = snackbarHostState.showActiva(
                        strings[StringKey.ALERT_SAVE_FAILED], AlertType.ERROR, strings[StringKey.ACTION_RETRY],
                    )
                    if (retry) event.retry()
                }
            }
        }
    }

    val actions = ScheduleActions(
        onSelectDay = { selectedDay = it },
        onAdd = { sheet = SheetTarget.New },
        onEdit = { sheet = SheetTarget.Edit(it) },
        onToggleReminder = { item, enabled -> viewModel.toggleReminder(item.id, enabled) },
        onOpenSettings = viewModel::openSettings,
        onReload = viewModel::reload,
    )
    ScheduleContent(state, permissionState, selectedDay, actions, snackbarHostState, modifier)

    val data = (state as? ScheduleUiState.Ready)?.data
    val target = sheet
    if (data != null && target != null) {
        ScheduleSheet(
            target = target,
            data = data,
            selectedDay = selectedDay ?: data.today.dayOfWeek.isoDayNumber,
            onSave = { viewModel.save(it) },
            onRequestDelete = { deleteTarget = it },
            onCreateRoutine = { sheet = null; onCreateRoutine() },
            onDismiss = { sheet = null },
        )
    }

    conflictDraft?.let { draft ->
        // Menutup dengan menyentuh luar dialog membatalkan penyimpanan; Ganti dan Tambah sama-sama menyimpan.
        Dialog(onDismissRequest = { conflictDraft = null }) {
            ActivaDialogCard(
                title = strings[StringKey.SCHEDULE_CONFLICT_TITLE],
                message = strings[StringKey.ALERT_SCHEDULE_CONFLICT],
                confirmLabel = strings[StringKey.SCHEDULE_CONFLICT_REPLACE],
                onConfirm = { conflictDraft = null; viewModel.save(draft, ConflictPolicy.REPLACE) },
                dismissLabel = strings[StringKey.SCHEDULE_CONFLICT_ADD],
                onDismiss = { conflictDraft = null; viewModel.save(draft, ConflictPolicy.KEEP_BOTH) },
            )
        }
    }

    deleteTarget?.let { item ->
        ActivaDialog(
            title = strings[StringKey.ALERT_DELETE_SCHEDULE_TITLE],
            message = strings[StringKey.ALERT_DELETE_SCHEDULE_BODY],
            confirmLabel = strings[StringKey.ACTION_DELETE],
            onConfirm = { deleteTarget = null; viewModel.delete(item.id) },
            dismissLabel = strings[StringKey.ACTION_CANCEL],
            onDismiss = { deleteTarget = null },
            confirmIcon = ActivaIconKind.TRASH,
        )
    }
}

@Composable
fun ScheduleContent(
    state: ScheduleUiState,
    permission: PermissionState,
    selectedDay: Int?,
    actions: ScheduleActions,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize().background(ActivaPlanTheme.colors.bg).statusBarsPadding()) {
        when (state) {
            ScheduleUiState.Loading -> Unit
            ScheduleUiState.Error -> LoadError(actions.onReload)
            is ScheduleUiState.Ready -> {
                ReadyContent(state.data, permission, selectedDay ?: state.data.today.dayOfWeek.isoDayNumber, actions)
                AddButton(actions.onAdd, Modifier.align(Alignment.BottomEnd).padding(ActivaDimens.screenPadding))
            }
        }
        ActivaSnackbarHost(
            snackbarHostState,
            Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = SNACKBAR_ABOVE_FAB),
        )
    }
}

/** Tinggi FAB 56 + jarak 20 + celah 12, agar snackbar tidak menutupi FAB. */
private val SNACKBAR_ABOVE_FAB = 88.dp

@Composable
private fun ReadyContent(data: ScheduleData, permission: PermissionState, selectedDay: Int, actions: ScheduleActions) {
    val c = ActivaPlanTheme.colors
    val strings = ActivaPlanTheme.strings
    val day = data.week.first { it.dayOfWeek == selectedDay }
    val items = data.items.filter { it.dayOfWeek == selectedDay }
    Column(Modifier.fillMaxSize().padding(horizontal = ActivaDimens.screenPadding)) {
        Spacer(Modifier.height(16.dp))
        Text(strings[StringKey.SCHEDULE_TITLE], style = ActivaPlanTheme.type.screenTitle, color = c.text)
        if (permission == PermissionState.DENIED) {
            Spacer(Modifier.height(16.dp))
            PermissionBanner(actions.onOpenSettings)
        }
        Spacer(Modifier.height(16.dp))
        WeekPicker(data.week, selectedDay, actions.onSelectDay)
        Spacer(Modifier.height(20.dp))
        Text(dateHeader(strings, day.date), style = ActivaPlanTheme.type.cardTitle, color = c.text)
        Spacer(Modifier.height(12.dp))
        if (items.isEmpty()) {
            EmptyDay(actions.onAdd)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 96.dp)) {
                items(items, key = { it.id }) { item ->
                    ScheduleCard(item, highlighted = day.isToday && item.id == data.nextTodayId, actions)
                }
            }
        }
    }
}

/** "Minggu, 25 Oktober". */
internal fun dateHeader(strings: Strings, date: LocalDate): String =
    strings.format(StringKey.SCHEDULE_DATE_HEADER, strings.dayName(date.dayOfWeek.isoDayNumber), date.day, strings.monthName(date.month.ordinal + 1))

@Composable
private fun PermissionBanner(onOpenSettings: () -> Unit) {
    val c = ActivaPlanTheme.colors
    val strings = ActivaPlanTheme.strings
    Row(Modifier.fillMaxWidth().background(c.errorSoft, ActivaShapes.card).padding(16.dp)) {
        ActivaIcon(ActivaIconKind.BELL_OFF, c.error, size = 22.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                strings[StringKey.ALERT_REMINDER_OFF_TITLE],
                style = ActivaPlanTheme.type.cardTitle.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                color = c.text,
            )
            Spacer(Modifier.height(2.dp))
            Text(strings[StringKey.ALERT_REMINDER_OFF_BODY], style = ActivaPlanTheme.type.body.copy(fontSize = 13.sp), color = c.textMuted)
            Spacer(Modifier.height(10.dp))
            ActivaButton(strings[StringKey.ACTION_OPEN_SETTINGS], onOpenSettings, style = ButtonStyle.DANGER, compact = true)
        }
    }
}

@Composable
private fun WeekPicker(week: List<ScheduleWeekDay>, selectedDay: Int, onSelect: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        week.forEach { day -> DayCell(day, day.dayOfWeek == selectedDay, { onSelect(day.dayOfWeek) }, Modifier.weight(1f)) }
    }
}

@Composable
private fun DayCell(day: ScheduleWeekDay, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val c = ActivaPlanTheme.colors
    val strings = ActivaPlanTheme.strings
    val shape = RoundedCornerShape(16.dp)
    val content = if (selected) c.onPrimary else c.text
    val description = strings.format(
        if (day.hasSchedule) StringKey.SCHEDULE_DAY_WITH_ITEMS else StringKey.SCHEDULE_DAY_PLAIN,
        strings.dayName(day.dayOfWeek), day.date.day,
    )
    Column(
        modifier.height(64.dp)
            .clip(shape)
            .background(if (selected) c.primary else c.surface)
            .border(1.dp, if (selected) c.primary else c.border, shape)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .semantics { contentDescription = description },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            strings.dayShort(day.dayOfWeek),
            style = ActivaPlanTheme.type.labelSmall.copy(fontSize = 12.sp),
            color = if (selected) c.onPrimary else if (day.isToday) c.primary else c.textMuted,
        )
        Text(day.date.day.toString(), style = ActivaPlanTheme.type.cardTitle.copy(fontWeight = FontWeight.Bold), color = content)
        Spacer(
            Modifier.padding(top = 3.dp).size(5.dp)
                .background(if (!day.hasSchedule) Color.Transparent else if (selected) c.onPrimary else c.primary, CircleShape),
        )
    }
}

@Composable
private fun ScheduleCard(item: ScheduleItem, highlighted: Boolean, actions: ScheduleActions) {
    val c = ActivaPlanTheme.colors
    val strings = ActivaPlanTheme.strings
    val reminderLabel = strings.format(StringKey.SCHEDULE_REMINDER_FOR, item.routineName)
    Surface(
        color = c.surface,
        contentColor = c.text,
        shape = ActivaShapes.card,
        border = BorderStroke(if (highlighted) 1.5.dp else 1.dp, if (highlighted) c.primary else c.border),
        modifier = Modifier.fillMaxWidth().clickable(role = Role.Button) { actions.onEdit(item) },
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.width(60.dp)) {
                Text(
                    formatClock(item.timeMinutes),
                    style = ActivaPlanTheme.type.cardTitle.copy(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold),
                    color = c.text,
                )
                Text(strings.format(StringKey.SCHEDULE_MINUTES, item.estimatedMinutes), style = metaStyle(), color = c.textMuted)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(item.routineName, style = ActivaPlanTheme.type.cardTitle, color = c.text, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item.level?.let { LevelBadge(it) }
                    Text(exerciseCountText(strings, item.exerciseCount), style = metaStyle(), color = c.textMuted)
                }
            }
            Spacer(Modifier.width(8.dp))
            Switch(
                checked = item.reminder,
                onCheckedChange = { actions.onToggleReminder(item, it) },
                modifier = Modifier.semantics { contentDescription = reminderLabel },
                colors = reminderSwitchColors(),
            )
        }
    }
}

@Composable
internal fun reminderSwitchColors() = with(ActivaPlanTheme.colors) {
    SwitchDefaults.colors(
        checkedThumbColor = surface, checkedTrackColor = success, checkedBorderColor = success,
        uncheckedThumbColor = surface, uncheckedTrackColor = surfaceVar, uncheckedBorderColor = border,
    )
}

@Composable
private fun AddButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = ActivaPlanTheme.colors
    val label = ActivaPlanTheme.strings[StringKey.SCHEDULE_ADD]
    Box(
        modifier.size(56.dp)
            .shadow(6.dp, CircleShape)
            .background(c.primary, CircleShape)
            .semantics { contentDescription = label }
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        ActivaIcon(ActivaIconKind.PLUS, c.onPrimary, size = 24.dp)
    }
}

@Composable
private fun EmptyDay(onAdd: () -> Unit) {
    val c = ActivaPlanTheme.colors
    val strings = ActivaPlanTheme.strings
    Column(Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        ActivaIcon(ActivaIconKind.CALENDAR, c.textMuted, size = 32.dp)
        Spacer(Modifier.height(12.dp))
        Text(strings[StringKey.SCHEDULE_EMPTY_TITLE], style = ActivaPlanTheme.type.cardTitle, color = c.text, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        ActivaButton(strings[StringKey.SCHEDULE_ADD], onAdd, icon = ActivaIconKind.PLUS)
    }
}

@Composable
private fun LoadError(onReload: () -> Unit) {
    val c = ActivaPlanTheme.colors
    val strings = ActivaPlanTheme.strings
    Column(
        Modifier.fillMaxSize().padding(ActivaDimens.screenPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ActivaIcon(ActivaIconKind.WARNING, c.textMuted, size = 32.dp)
        Spacer(Modifier.height(12.dp))
        Text(strings[StringKey.ALERT_LOAD_FAILED], style = ActivaPlanTheme.type.body, color = c.textMuted, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        ActivaButton(strings[StringKey.ACTION_RELOAD], onReload, style = ButtonStyle.SOFT)
    }
}

@Composable
private fun metaStyle() = ActivaPlanTheme.type.body.copy(fontSize = 13.sp, lineHeight = 18.sp)

private fun exerciseCountText(strings: Strings, count: Int): String =
    strings.format(if (count == 1) StringKey.ROUTINE_ITEM_ONE else StringKey.ROUTINE_ITEMS, count)

// region Sheet Tambah/Ubah

@Composable
private fun ScheduleSheet(
    target: SheetTarget,
    data: ScheduleData,
    selectedDay: Int,
    onSave: (ScheduleDraft) -> Unit,
    onRequestDelete: (ScheduleItem) -> Unit,
    onCreateRoutine: () -> Unit,
    onDismiss: () -> Unit,
) {
    val c = ActivaPlanTheme.colors
    val strings = ActivaPlanTheme.strings
    var draft by remember(target) {
        mutableStateOf(
            when (target) {
                SheetTarget.New ->
                    ScheduleDraft(null, setOf(selectedDay), data.defaultTimeMinutes, data.routines.singleOrNull()?.id, reminder = true)
                is SheetTarget.Edit -> ScheduleDraft.from(target.item)
            },
        )
    }
    var pickingTime by remember { mutableStateOf(false) }
    val editing = (target as? SheetTarget.Edit)?.item

    ActivaBottomSheet(onDismiss) {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                strings[if (editing == null) StringKey.SCHEDULE_SHEET_NEW else StringKey.SCHEDULE_SHEET_EDIT],
                style = ActivaPlanTheme.type.screenTitle,
                color = c.text,
            )
            SectionLabel(strings[StringKey.SCHEDULE_DAYS])
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                (1..7).forEach { day ->
                    SheetDayChip(day, day in draft.days, { draft = draft.toggleDay(day) }, Modifier.weight(1f))
                }
            }
            if (draft.days.isEmpty()) Hint(strings[StringKey.SCHEDULE_NEED_DAY])

            SectionLabel(strings[StringKey.SCHEDULE_TIME])
            TimeField(draft.timeMinutes) { pickingTime = true }

            SectionLabel(strings[StringKey.SCHEDULE_ROUTINE])
            if (data.routines.isEmpty()) {
                Text(strings[StringKey.ROUTINE_PICK_EMPTY], style = ActivaPlanTheme.type.body, color = c.textMuted)
                ActivaButton(strings[StringKey.SCHEDULE_CREATE_ROUTINE], onCreateRoutine, style = ButtonStyle.SOFT, icon = ActivaIconKind.PLUS)
            } else {
                data.routines.forEach { routine ->
                    RoutineChoice(routine, routine.id == draft.routineId) { draft = draft.copy(routineId = routine.id) }
                }
                if (draft.routineId == null) Hint(strings[StringKey.SCHEDULE_NEED_ROUTINE])
            }

            Row(
                Modifier.fillMaxWidth().defaultMinSize(minHeight = ActivaDimens.minTouchTarget),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(strings[StringKey.SCHEDULE_REMINDER], style = ActivaPlanTheme.type.cardTitle, color = c.text, modifier = Modifier.weight(1f))
                Switch(
                    checked = draft.reminder,
                    onCheckedChange = { draft = draft.copy(reminder = it) },
                    modifier = Modifier.semantics { contentDescription = strings[StringKey.SCHEDULE_REMINDER] },
                    colors = reminderSwitchColors(),
                )
            }

            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (editing != null) {
                    ActivaButton(
                        strings[StringKey.ACTION_DELETE], { onRequestDelete(editing) }, Modifier.weight(1f),
                        style = ButtonStyle.SECONDARY, icon = ActivaIconKind.TRASH,
                    )
                }
                ActivaButton(strings[StringKey.SCHEDULE_SAVE], { onSave(draft) }, Modifier.weight(1f), enabled = draft.isComplete)
            }
        }
    }

    if (pickingTime) {
        TimePickerDialog(
            initialMinutes = draft.timeMinutes,
            onConfirm = { draft = draft.copy(timeMinutes = it); pickingTime = false },
            onDismiss = { pickingTime = false },
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = ActivaPlanTheme.type.label, color = ActivaPlanTheme.colors.textMuted, modifier = Modifier.padding(top = 8.dp))
}

@Composable
private fun Hint(text: String) {
    Text(text, style = ActivaPlanTheme.type.body.copy(fontSize = 13.sp), color = ActivaPlanTheme.colors.error)
}

@Composable
private fun SheetDayChip(day: Int, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val c = ActivaPlanTheme.colors
    val strings = ActivaPlanTheme.strings
    val shape = RoundedCornerShape(14.dp)
    val name = strings.dayName(day)
    Box(
        modifier.height(ActivaDimens.minTouchTarget)
            .clip(shape)
            .background(if (selected) c.primary else c.surface)
            .border(1.dp, if (selected) c.primary else c.border, shape)
            .selectable(selected = selected, role = Role.Checkbox, onClick = onClick)
            .semantics { contentDescription = name },
        contentAlignment = Alignment.Center,
    ) {
        Text(strings.dayShort(day), style = ActivaPlanTheme.type.label, color = if (selected) c.onPrimary else c.text, maxLines = 1)
    }
}

@Composable
private fun TimeField(timeMinutes: Int, onClick: () -> Unit) {
    val c = ActivaPlanTheme.colors
    val label = ActivaPlanTheme.strings[StringKey.SCHEDULE_TIME_PICKER_TITLE]
    Row(
        Modifier.fillMaxWidth().defaultMinSize(minHeight = ActivaDimens.minTouchTarget)
            .clip(ActivaShapes.button)
            .background(c.surfaceVar)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = "$label: ${formatClock(timeMinutes)}" }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ActivaIcon(ActivaIconKind.CLOCK, c.textMuted, size = 20.dp)
        Spacer(Modifier.width(12.dp))
        Text(formatClock(timeMinutes), style = ActivaPlanTheme.type.cardTitle.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold), color = c.text)
    }
}

@Composable
private fun RoutineChoice(routine: RoutineOption, selected: Boolean, onClick: () -> Unit) {
    val c = ActivaPlanTheme.colors
    val strings = ActivaPlanTheme.strings
    val shape = ActivaShapes.button
    Row(
        Modifier.fillMaxWidth()
            .clip(shape)
            .background(if (selected) c.primarySoft else c.surface)
            .border(if (selected) 2.dp else 1.dp, if (selected) c.primary else c.border, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(routine.name, style = ActivaPlanTheme.type.cardTitle, color = c.text, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                routine.level?.let { LevelBadge(it) }
                Text(
                    exerciseCountText(strings, routine.exerciseCount) + " · " + strings.format(StringKey.SCHEDULE_MINUTES, routine.estimatedMinutes),
                    style = metaStyle(),
                    color = c.textMuted,
                )
            }
        }
        if (selected) {
            Spacer(Modifier.width(8.dp))
            ActivaIcon(ActivaIconKind.CHECK, c.primary, size = 22.dp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TimePickerDialog(initialMinutes: Int, onConfirm: (Int) -> Unit, onDismiss: () -> Unit) {
    val c = ActivaPlanTheme.colors
    val strings = ActivaPlanTheme.strings
    val state = rememberTimePickerState(initialHour = initialMinutes / 60, initialMinute = initialMinutes % 60, is24Hour = true)
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            color = c.surface,
            contentColor = c.text,
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 8.dp,
            modifier = Modifier.width(IntrinsicSize.Min),
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(strings[StringKey.SCHEDULE_TIME_PICKER_TITLE], style = ActivaPlanTheme.type.cardTitle, color = c.text)
                Spacer(Modifier.height(16.dp))
                TimePicker(state)
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ActivaButton(strings[StringKey.ACTION_CANCEL], onDismiss, Modifier.weight(1f), ButtonStyle.SECONDARY, compact = true)
                    ActivaButton(
                        strings[StringKey.SCHEDULE_TIME_OK], { onConfirm(state.hour * 60 + state.minute) }, Modifier.weight(1f),
                        compact = true,
                    )
                }
            }
        }
    }
}

// endregion

// region Pratinjau

private val previewToday = LocalDate(2026, 10, 25)

private val previewWeek = (19..25).map { day ->
    ScheduleWeekDay(LocalDate(2026, 10, day), day - 18, hasSchedule = day in setOf(19, 21, 23, 25), isToday = day == 25)
}

private val previewData = ScheduleData(
    today = previewToday,
    week = previewWeek,
    items = listOf(
        ScheduleItem("s1", 7, 990, "r1", "Board Pemula Full Body", Difficulty.EASY, 5, 42, reminder = true),
        ScheduleItem("s2", 7, 1170, "r2", "Tali Tubuh Atas", Difficulty.MEDIUM, 4, 38, reminder = false),
    ),
    nextTodayId = "s1",
    routines = listOf(
        RoutineOption("r1", "Board Pemula Full Body", Difficulty.EASY, 5, 42),
        RoutineOption("r2", "Tali Tubuh Atas", Difficulty.MEDIUM, 4, 38),
    ),
    defaultTimeMinutes = 990,
)

private val previewActions = ScheduleActions({}, {}, {}, { _, _ -> }, {}, {})

@Composable
private fun SchedulePreview(darkTheme: Boolean, data: ScheduleData, permission: PermissionState) = ActivaPlanTheme(darkTheme = darkTheme) {
    ScheduleContent(ScheduleUiState.Ready(data), permission, null, previewActions, remember { SnackbarHostState() })
}

@Preview(widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun ScheduleDeniedLightPreview() = SchedulePreview(false, previewData, PermissionState.DENIED)

@Preview(widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun ScheduleDeniedDarkPreview() = SchedulePreview(true, previewData, PermissionState.DENIED)

@Preview(widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun ScheduleGrantedLightPreview() = SchedulePreview(false, previewData, PermissionState.GRANTED)

@Preview(widthDp = 390, heightDp = 844, showBackground = true)
@Composable
private fun ScheduleEmptyDarkPreview() =
    SchedulePreview(true, previewData.copy(items = emptyList(), nextTodayId = null), PermissionState.GRANTED)

// endregion
