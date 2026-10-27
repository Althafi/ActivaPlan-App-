package com.activaplan.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.activaplan.app.domain.model.HomeData
import com.activaplan.app.domain.usecase.ObserveHomeUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

sealed interface HomeUiState {
    data object Loading : HomeUiState

    /** Katalog latihan gagal dimuat; layar menawarkan Muat ulang. */
    data object Error : HomeUiState

    data class Ready(val data: HomeData) : HomeUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(observeHome: ObserveHomeUseCase) : ViewModel() {

    private val reloads = MutableStateFlow(0)

    val uiState: StateFlow<HomeUiState> = reloads
        .flatMapLatest {
            observeHome()
                .map<HomeData, HomeUiState> { HomeUiState.Ready(it) }
                .onStart { emit(HomeUiState.Loading) }
                .catch { emit(HomeUiState.Error) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), HomeUiState.Loading)

    fun reload() = reloads.update { it + 1 }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
