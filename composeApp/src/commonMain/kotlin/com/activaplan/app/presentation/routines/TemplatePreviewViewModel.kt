package com.activaplan.app.presentation.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.activaplan.app.domain.i18n.Language
import com.activaplan.app.domain.model.TemplateDetail
import com.activaplan.app.domain.usecase.CreateRoutineFromTemplateUseCase
import com.activaplan.app.domain.usecase.GetTemplateDetailUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed interface TemplatePreviewUiState {
    data object Loading : TemplatePreviewUiState

    /** Katalog latihan gagal dimuat; layar menawarkan Muat ulang. */
    data object Error : TemplatePreviewUiState

    /** ID template tidak ada atau tak punya latihan yang dikenal. */
    data object NotFound : TemplatePreviewUiState

    data class Ready(val detail: TemplateDetail) : TemplatePreviewUiState
}

/** Hasil menekan Gunakan: [Copied] membawa ID rutinitas salinan; [Failed.retry] mengulang. */
sealed interface TemplateUseEvent {
    data class Copied(val routineId: String) : TemplateUseEvent
    data object Unavailable : TemplateUseEvent
    data class Failed(val retry: () -> Unit) : TemplateUseEvent
}

class TemplatePreviewViewModel(
    private val id: String,
    private val language: Language,
    private val getDetail: GetTemplateDetailUseCase,
    private val createFromTemplate: CreateRoutineFromTemplateUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<TemplatePreviewUiState>(TemplatePreviewUiState.Loading)
    val uiState: StateFlow<TemplatePreviewUiState> = _uiState.asStateFlow()

    private val _events = Channel<TemplateUseEvent>(Channel.BUFFERED)
    val events: Flow<TemplateUseEvent> = _events.receiveAsFlow()

    init {
        reload()
    }

    fun reload() {
        viewModelScope.launch {
            _uiState.value = TemplatePreviewUiState.Loading
            _uiState.value = try {
                getDetail(id, language)?.let { TemplatePreviewUiState.Ready(it) } ?: TemplatePreviewUiState.NotFound
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                TemplatePreviewUiState.Error
            }
        }
    }

    fun use() {
        viewModelScope.launch {
            val event = try {
                createFromTemplate(id, language)?.let { TemplateUseEvent.Copied(it.id) } ?: TemplateUseEvent.Unavailable
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                TemplateUseEvent.Failed(retry = ::use)
            }
            _events.send(event)
        }
    }
}
