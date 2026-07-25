package `in`.mato.cryptodetail.presentation.cryptodetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.mato.cryptodetail.domain.model.CryptoDetail
import `in`.mato.cryptodetail.domain.model.HistoryPoint
import `in`.mato.cryptodetail.domain.repository.CryptoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ChartRange(val days: Int, val label: String) {
    SevenDays(days = 7, label = "7D"),
    ThirtyDays(days = 30, label = "30D"),
}

data class CryptoDetailUiState(
    val isLoading: Boolean = false,
    val detail: CryptoDetail? = null,
    val history: List<HistoryPoint> = emptyList(),
    val selectedRange: ChartRange = ChartRange.SevenDays,
    val isHistoryLoading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class CryptoDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: CryptoRepository,
) : ViewModel() {
    private val coinId: String = checkNotNull(savedStateHandle["coinId"])
    private val _uiState = MutableStateFlow(CryptoDetailUiState())
    val uiState: StateFlow<CryptoDetailUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val (detailResult, historyResult) = coroutineScope {
                val detail = async { repository.getCryptoDetail(coinId) }
                val history = async { repository.getPriceHistory(coinId, days = _uiState.value.selectedRange.days) }
                detail.await() to history.await()
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    detail = detailResult.getOrNull(),
                    history = historyResult.getOrDefault(emptyList()),
                    isHistoryLoading = false,
                    errorMessage = detailResult.exceptionOrNull()?.message
                        ?: historyResult.exceptionOrNull()?.message,
                )
            }
        }
    }

    fun selectRange(range: ChartRange) {
        if (range == _uiState.value.selectedRange || _uiState.value.isHistoryLoading) return
        _uiState.update {
            it.copy(
                selectedRange = range,
                history = emptyList(),
                isHistoryLoading = true,
                errorMessage = null,
            )
        }
        viewModelScope.launch {
            repository.getPriceHistory(coinId, days = range.days).fold(
                onSuccess = { history ->
                    _uiState.update { it.copy(history = history, isHistoryLoading = false) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isHistoryLoading = false,
                            errorMessage = error.message ?: "Unable to load chart data.",
                        )
                    }
                },
            )
        }
    }
}
