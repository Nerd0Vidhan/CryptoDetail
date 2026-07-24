package `in`.mato.cryptodetail.presentation.cryptolist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.mato.cryptodetail.domain.model.Crypto
import `in`.mato.cryptodetail.domain.repository.CryptoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CryptoListUiState(
    val coins: List<Crypto> = emptyList(),
    val searchQuery: String = "",
    val isInitialLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class CryptoListViewModel @Inject constructor(
    private val repository: CryptoRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CryptoListUiState())
    val uiState: StateFlow<CryptoListUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun refresh() {
        if (_uiState.value.isInitialLoading || _uiState.value.isRefreshing) return

        val hasCachedCoins = _uiState.value.coins.isNotEmpty()
        _uiState.update {
            it.copy(
                isInitialLoading = !hasCachedCoins,
                isRefreshing = hasCachedCoins,
                errorMessage = null,
            )
        }
        viewModelScope.launch {
            repository.getTopCryptocurrencies(limit = COINS_PER_PAGE).fold(
                onSuccess = { coins ->
                    _uiState.update {
                        it.copy(
                            coins = coins,
                            isInitialLoading = false,
                            isRefreshing = false,
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isInitialLoading = false,
                            isRefreshing = false,
                            errorMessage = error.message ?: "Unable to load cryptocurrency prices.",
                        )
                    }
                },
            )
        }
    }

    companion object {
        private const val COINS_PER_PAGE = 50
    }
}
