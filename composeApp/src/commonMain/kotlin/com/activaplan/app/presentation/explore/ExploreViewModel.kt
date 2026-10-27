package com.activaplan.app.presentation.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.activaplan.app.data.repository.ExerciseRepository
import com.activaplan.app.domain.i18n.Language
import com.activaplan.app.domain.model.Category
import com.activaplan.app.domain.model.Difficulty
import com.activaplan.app.domain.model.Exercise
import com.activaplan.app.domain.model.ExploreQuery
import com.activaplan.app.domain.model.Mode
import com.activaplan.app.domain.usecase.FilterExercisesUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ExploreUiState {
    data object Loading : ExploreUiState

    /** Katalog latihan gagal dimuat; layar menawarkan Muat ulang. */
    data object Error : ExploreUiState

    data class Ready(val results: List<Exercise>) : ExploreUiState
}

/** Isi katalog dimuat sekali per [language]; filter dihitung di memori sehingga hasil langsung berubah. */
class ExploreViewModel(
    private val exercises: ExerciseRepository,
    private val filter: FilterExercisesUseCase,
    private val language: Language,
) : ViewModel() {

    private val _query = MutableStateFlow(ExploreQuery(mode = Mode.PUSHUP_BOARD))

    /** Dipisah dari [uiState] agar teks di kolom cari selalu sinkron saat mengetik. */
    val query: StateFlow<ExploreQuery> = _query.asStateFlow()

    private sealed interface Load {
        data object Loading : Load
        data object Failed : Load
        data class Done(val all: List<Exercise>) : Load
    }

    private val load = MutableStateFlow<Load>(Load.Loading)

    val uiState: StateFlow<ExploreUiState> = combine(load, _query) { loaded, query ->
        when (loaded) {
            Load.Loading -> ExploreUiState.Loading
            Load.Failed -> ExploreUiState.Error
            is Load.Done -> ExploreUiState.Ready(filter(loaded.all, query))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), ExploreUiState.Loading)

    init {
        reload()
    }

    fun reload() {
        viewModelScope.launch {
            load.value = Load.Loading
            load.value = try {
                Load.Done(exercises.exercises(language = language))
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                Load.Failed
            }
        }
    }

    fun onMode(mode: Mode) = _query.update { it.copy(mode = mode) }

    fun onText(text: String) = _query.update { it.copy(text = text) }

    fun onCategory(category: Category?) = _query.update { it.copy(category = category) }

    fun onDifficulty(difficulty: Difficulty?) = _query.update { it.copy(difficulty = difficulty) }

    /** Mengosongkan teks, kategori, dan level; mode tetap. */
    fun resetFilters() = _query.update { it.copy(text = "", category = null, difficulty = null) }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
