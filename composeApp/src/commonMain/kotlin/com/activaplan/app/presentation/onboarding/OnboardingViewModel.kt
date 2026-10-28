package com.activaplan.app.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.activaplan.app.domain.model.Difficulty
import com.activaplan.app.domain.model.Mode
import com.activaplan.app.domain.model.OnboardingChoices
import com.activaplan.app.domain.model.Settings
import com.activaplan.app.domain.reminder.NotificationPermission
import com.activaplan.app.domain.reminder.PermissionState
import com.activaplan.app.domain.usecase.CompleteOnboardingUseCase
import com.activaplan.app.domain.usecase.ObserveSettingsUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** [saving] true sejak tombol ditekan sampai tersimpan, agar tidak tersimpan dua kali. */
data class OnboardingUiState(val choices: OnboardingChoices, val saving: Boolean = false)

/** Hasil aksi di Onboarding; [Failed.retry] mengulang aksi yang gagal. */
sealed interface OnboardingEvent {
    /** Izin notifikasi belum pernah ditanyakan; layar menampilkan dialog sistem. */
    data object RequestPermission : OnboardingEvent

    /** Izin ditolak; toggle sudah dikembalikan ke mati. */
    data object PermissionDenied : OnboardingEvent

    data class Failed(val retry: () -> Unit) : OnboardingEvent
}

/**
 * Menyimpan pilihan di memori sampai pengguna menekan salah satu tombol akhir; menutup aplikasi di tengah jalan
 * tidak menyimpan apa pun. Setelah tersimpan, `Settings.onboardingDone` berubah dan `App` berpindah ke Beranda sendiri.
 */
class OnboardingViewModel(
    observeSettings: ObserveSettingsUseCase,
    private val complete: CompleteOnboardingUseCase,
    private val permission: NotificationPermission,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState(OnboardingChoices.initial(Settings.DEFAULT.defaultTimeMinutes)))
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    private val _events = Channel<OnboardingEvent>(Channel.BUFFERED)
    val events: Flow<OnboardingEvent> = _events.receiveAsFlow()

    init {
        // Jam pengingat dimulai dari waktu default yang tersimpan.
        viewModelScope.launch {
            val defaultTime = observeSettings().first().defaultTimeMinutes
            _state.update { it.copy(choices = it.choices.copy(reminderTimeMinutes = defaultTime)) }
        }
    }

    fun onLevel(level: Difficulty) = edit { it.copy(startLevel = level) }

    /** Mengetuk kartu alat yang terpilih melepasnya; boleh sampai kosong, tombol utama yang menolak. */
    fun onToggleMode(mode: Mode) = edit {
        it.copy(ownedModes = if (mode in it.ownedModes) it.ownedModes - mode else it.ownedModes + mode)
    }

    fun onReminderTime(timeMinutes: Int) = edit { it.copy(reminderTimeMinutes = timeMinutes) }

    fun onReminders(on: Boolean) {
        if (!on) {
            edit { it.copy(remindersOn = false) }
            return
        }
        viewModelScope.launch {
            when (permission.status()) {
                PermissionState.GRANTED -> edit { it.copy(remindersOn = true) }
                PermissionState.UNDETERMINED -> {
                    edit { it.copy(remindersOn = true) }
                    _events.send(OnboardingEvent.RequestPermission)
                }
                PermissionState.DENIED -> {
                    edit { it.copy(remindersOn = false) }
                    _events.send(OnboardingEvent.PermissionDenied)
                }
            }
        }
    }

    /** Dipanggil setelah dialog izin dijawab; bila tidak diizinkan, toggle kembali mati. */
    fun onPermissionResult() {
        viewModelScope.launch {
            if (permission.status() != PermissionState.GRANTED && _state.value.choices.remindersOn) {
                edit { it.copy(remindersOn = false) }
                _events.send(OnboardingEvent.PermissionDenied)
            }
        }
    }

    /** Tombol "Mulai Latihan". */
    fun onStart() {
        val choices = _state.value.choices
        if (choices.isValid) finish(choices)
    }

    /** Tautan "Nanti saja": menandai selesai tanpa mengubah setelan lain. */
    fun onLater() = finish(null)

    private fun finish(choices: OnboardingChoices?) {
        if (_state.value.saving) return
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            try {
                complete(choices)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _state.update { it.copy(saving = false) }
                _events.send(OnboardingEvent.Failed { finish(choices) })
            }
        }
    }

    private fun edit(transform: (OnboardingChoices) -> OnboardingChoices) =
        _state.update { it.copy(choices = transform(it.choices)) }
}
