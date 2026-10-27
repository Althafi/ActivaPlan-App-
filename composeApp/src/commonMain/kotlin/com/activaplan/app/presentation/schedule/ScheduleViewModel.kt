package com.activaplan.app.presentation.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.activaplan.app.domain.i18n.StringKey
import com.activaplan.app.domain.model.ConflictPolicy
import com.activaplan.app.domain.model.SaveScheduleResult
import com.activaplan.app.domain.model.ScheduleData
import com.activaplan.app.domain.model.ScheduleDraft
import com.activaplan.app.domain.reminder.NotificationPermission
import com.activaplan.app.domain.reminder.PermissionState
import com.activaplan.app.domain.reminder.ReminderSynchronizer
import com.activaplan.app.domain.usecase.DeleteScheduleEntryUseCase
import com.activaplan.app.domain.usecase.ObserveScheduleUseCase
import com.activaplan.app.domain.usecase.PruneScheduleUseCase
import com.activaplan.app.domain.usecase.SaveScheduleUseCase
import com.activaplan.app.domain.usecase.SetScheduleReminderUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ScheduleUiState {
    data object Loading : ScheduleUiState

    /** Katalog latihan gagal dimuat; layar menawarkan Muat ulang. */
    data object Error : ScheduleUiState

    data class Ready(val data: ScheduleData) : ScheduleUiState
}

/** Hasil aksi di layar Jadwal; [Failed.retry] mengulang aksi yang sama. */
sealed interface ScheduleEvent {
    data class Saved(val days: List<Int>, val timeMinutes: Int) : ScheduleEvent

    /** Ada jadwal yang bentrok; layar menanyakan Ganti atau Tambah lalu menyimpan ulang [draft]. */
    data class Conflict(val draft: ScheduleDraft) : ScheduleEvent

    data object Deleted : ScheduleEvent

    /** Isian tidak lengkap; [message] menjelaskan yang kurang. */
    data class Invalid(val message: StringKey) : ScheduleEvent

    /** Pengingat dinyalakan tetapi izin notifikasi belum pernah ditanyakan; layar menampilkan dialog sistem. */
    data object RequestPermission : ScheduleEvent

    data class Failed(val retry: () -> Unit) : ScheduleEvent
}

class ScheduleViewModel(
    observeSchedule: ObserveScheduleUseCase,
    private val saveSchedule: SaveScheduleUseCase,
    private val deleteEntry: DeleteScheduleEntryUseCase,
    private val setReminder: SetScheduleReminderUseCase,
    pruneSchedule: PruneScheduleUseCase,
    private val permission: NotificationPermission,
    private val synchronizer: ReminderSynchronizer,
) : ViewModel() {

    private val reloads = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ScheduleUiState> = reloads.flatMapLatest {
        observeSchedule()
            .map<ScheduleData, ScheduleUiState> { ScheduleUiState.Ready(it) }
            .onStart { emit(ScheduleUiState.Loading) }
            .catch { emit(ScheduleUiState.Error) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), ScheduleUiState.Loading)

    private val _permission = MutableStateFlow(PermissionState.UNDETERMINED)

    /** Status izin notifikasi; banner "Pengingat tidak aktif" tampil bila [PermissionState.DENIED]. */
    val permissionState: StateFlow<PermissionState> = _permission.asStateFlow()

    private val _events = Channel<ScheduleEvent>(Channel.BUFFERED)
    val events: Flow<ScheduleEvent> = _events.receiveAsFlow()

    init {
        refreshPermission()
        // Jadwal yang menunjuk rutinitas terhapus dibuang agar tidak menyisakan pengingat yatim.
        viewModelScope.launch {
            try {
                pruneSchedule()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Dicoba lagi saat layar dibuka berikutnya.
            }
        }
    }

    fun reload() {
        reloads.value += 1
    }

    /** Membaca ulang izin, mis. setelah pengguna kembali dari pengaturan sistem. */
    fun refreshPermission() {
        viewModelScope.launch { _permission.value = permission.status() }
    }

    fun openSettings() = permission.openSettings()

    /** Dipanggil setelah dialog izin dijawab: perbarui status dan jadwalkan ulang alarm sesuai izin baru. */
    fun onPermissionResult() {
        viewModelScope.launch {
            _permission.value = permission.status()
            try {
                synchronizer.sync()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Penyelarasan berikutnya (saat aplikasi dibuka atau data berubah) mencoba lagi.
            }
        }
    }

    fun save(draft: ScheduleDraft, policy: ConflictPolicy = ConflictPolicy.ASK): Unit = perform({ save(draft, policy) }) {
        when (val result = saveSchedule(draft, policy)) {
            is SaveScheduleResult.Saved -> {
                _events.send(ScheduleEvent.Saved(result.entries.map { it.dayOfWeek }.sorted(), draft.timeMinutes))
                askPermissionIfNeeded(draft.reminder)
            }
            is SaveScheduleResult.Conflict -> _events.send(ScheduleEvent.Conflict(draft))
            SaveScheduleResult.NoDay -> _events.send(ScheduleEvent.Invalid(StringKey.SCHEDULE_NEED_DAY))
            SaveScheduleResult.NoRoutine -> _events.send(ScheduleEvent.Invalid(StringKey.SCHEDULE_NEED_ROUTINE))
        }
    }

    fun delete(entryId: String): Unit = perform({ delete(entryId) }) {
        deleteEntry(entryId)
        _events.send(ScheduleEvent.Deleted)
    }

    fun toggleReminder(entryId: String, enabled: Boolean): Unit = perform({ toggleReminder(entryId, enabled) }) {
        setReminder(entryId, enabled)
        askPermissionIfNeeded(enabled)
    }

    private suspend fun askPermissionIfNeeded(reminderOn: Boolean) {
        if (reminderOn && permission.status() == PermissionState.UNDETERMINED) _events.send(ScheduleEvent.RequestPermission)
    }

    private fun perform(retry: () -> Unit, action: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                action()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _events.send(ScheduleEvent.Failed(retry))
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
