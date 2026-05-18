package by.alexandr7035.banking.ui.feature_password_health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.alexandr7035.banking.domain.features.password_health.GetReusedPasswordGroupsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PasswordHealthViewModel(
    private val getReusedPasswordGroupsUseCase: GetReusedPasswordGroupsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PasswordHealthState())
    val state = _state.asStateFlow()

    fun emitIntent(intent: PasswordHealthIntent) {
        when (intent) {
            is PasswordHealthIntent.LoadData -> loadData()
        }
    }

    private fun loadData() {
        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val groups = getReusedPasswordGroupsUseCase.execute()
                _state.update {
                    it.copy(isLoading = false, groups = groups)
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(isLoading = false, groups = emptyList())
                }
            }
        }
    }
}
