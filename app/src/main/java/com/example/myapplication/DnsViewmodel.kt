package com.example.myapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DnsViewmodel @Inject constructor(private val repo: DnsRepository) : ViewModel() {


    private val _state = MutableStateFlow(DnsUiState())
    val state: StateFlow<DnsUiState> = _state

    fun startDiscovery() {
        viewModelScope.launch {
            _state.value =
                _state.value.copy(isLoading = true, isTimeout = false, error = null, success = "")
            repo.getDns().collect { result ->
                when (result) {
                    is DnsResult.Found -> _state.value =
                        _state.value.copy(success = result.url, isLoading = false)

                    is DnsResult.TimedOut -> _state.value =
                        _state.value.copy(isTimeout = true, isLoading = false)
                }
            }

        }
    }

}

data class DnsUiState(
    val success: String = "",
    val isLoading: Boolean = false,
    val isTimeout: Boolean = false,
    val error: String? = null,
)

data class DnsData(
    val hostName: String,
    val port: String,
)