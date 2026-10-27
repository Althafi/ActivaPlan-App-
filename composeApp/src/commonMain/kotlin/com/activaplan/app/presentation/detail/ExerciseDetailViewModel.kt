package com.activaplan.app.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.activaplan.app.domain.i18n.Language
import com.activaplan.app.domain.model.AddToRoutineResult
import com.activaplan.app.domain.model.Exercise
import com.activaplan.app.domain.model.ExerciseDetail
import com.activaplan.app.domain.model.Routine
import com.activaplan.app.domain.usecase.AddExerciseToRoutineUseCase
import com.activaplan.app.domain.usecase.GetExerciseDetailUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ExerciseDetailUiState {
    data object Loading : ExerciseDetailUiState

    /** Katalog latihan gagal dimuat; layar menawarkan Muat ulang. */
    data object Error : ExerciseDetailUiState

    /** ID tidak ada di katalog (tautan usang). */
    data object NotFound : ExerciseDetailUiState

    data class Ready(val detail: ExerciseDetail) : ExerciseDetailUiState
}

/** Hasil satu percobaan menambah latihan ke rutinitas. [Failed.retry] mengulang percobaan yang sama. */
sealed interface AddOutcome {
    data class Done(val result: AddToRoutineResult) : AddOutcome
    data class Failed(val retry: () -> Unit) : AddOutcome
}

class ExerciseDetailViewModel(
    private val id: String,
    private val language: Language,
    private val getDetail: GetExerciseDetailUseCase,
    private val addToRoutine: AddExerciseToRoutineUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExerciseDetailUiState>(ExerciseDetailUiState.Loading)
    val uiState: StateFlow<ExerciseDetailUiState> get() = _uiState

    val routines: StateFlow<List<Routine>> = addToRoutine.routines
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    private val _outcomes = Channel<AddOutcome>(Channel.BUFFERED)
    val outcomes: Flow<AddOutcome> = _outcomes.receiveAsFlow()

    init {
        reload()
    }

    fun reload() {
        viewModelScope.launch {
            _uiState.value = ExerciseDetailUiState.Loading
            _uiState.value = try {
                getDetail(id, language)?.let { ExerciseDetailUiState.Ready(it) } ?: ExerciseDetailUiState.NotFound
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                ExerciseDetailUiState.Error
            }
        }
    }

    fun addToExisting(routineId: String) = add { addToRoutine.toExisting(routineId, it) }

    fun addToNew(name: String) = add { addToRoutine.toNew(name, it) }

    private fun add(action: suspend (Exercise) -> AddToRoutineResult) {
        val exercise = (_uiState.value as? ExerciseDetailUiState.Ready)?.detail?.exercise ?: return
        viewModelScope.launch {
            val outcome = try {
                AddOutcome.Done(action(exercise))
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                AddOutcome.Failed(retry = { add(action) })
            }
            _outcomes.send(outcome)
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
