package com.activaplan.app.presentation.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.activaplan.app.domain.i18n.Language
import com.activaplan.app.domain.model.RoutinesData
import com.activaplan.app.domain.usecase.CreateRoutineFromTemplateUseCase
import com.activaplan.app.domain.usecase.DeleteRoutineUseCase
import com.activaplan.app.domain.usecase.ObserveRoutinesUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface RoutinesUiState {
    data object Loading : RoutinesUiState

    /** Katalog latihan gagal dimuat; layar menawarkan Muat ulang. */
    data object Error : RoutinesUiState

    data class Ready(val data: RoutinesData) : RoutinesUiState
}

/** Hasil aksi di daftar; [Failed.retry] mengulang aksi yang sama. */
sealed interface RoutinesEvent {
    data object Deleted : RoutinesEvent

    /** Template sudah disalin menjadi rutinitas [routineId]; layar membuka Builder untuknya. */
    data class TemplateCopied(val routineId: String) : RoutinesEvent

    /** Template tidak ada atau kosong. */
    data object TemplateUnavailable : RoutinesEvent

    data class Failed(val retry: () -> Unit) : RoutinesEvent
}

class RoutinesViewModel(
    private val language: Language,
    observeRoutines: ObserveRoutinesUseCase,
    private val deleteRoutine: DeleteRoutineUseCase,
    private val createFromTemplate: CreateRoutineFromTemplateUseCase,
) : ViewModel() {

    private val reloads = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<RoutinesUiState> = reloads.flatMapLatest {
        observeRoutines()
            .map<RoutinesData, RoutinesUiState> { RoutinesUiState.Ready(it) }
            .onStart { emit(RoutinesUiState.Loading) }
            .catch { emit(RoutinesUiState.Error) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), RoutinesUiState.Loading)

    private val _events = Channel<RoutinesEvent>(Channel.BUFFERED)
    val events: Flow<RoutinesEvent> = _events.receiveAsFlow()

    fun reload() {
        reloads.value += 1
    }

    fun delete(routineId: String): Unit = perform({ delete(routineId) }) {
        deleteRoutine(routineId)
        RoutinesEvent.Deleted
    }

    fun useTemplate(templateId: String): Unit = perform({ useTemplate(templateId) }) {
        createFromTemplate(templateId, language)
            ?.let { RoutinesEvent.TemplateCopied(it.id) }
            ?: RoutinesEvent.TemplateUnavailable
    }

    private fun perform(retry: () -> Unit, action: suspend () -> RoutinesEvent) {
        viewModelScope.launch {
            val event = try {
                action()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                RoutinesEvent.Failed(retry)
            }
            _events.send(event)
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
