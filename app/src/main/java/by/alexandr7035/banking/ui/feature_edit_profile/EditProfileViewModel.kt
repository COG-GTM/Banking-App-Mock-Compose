package by.alexandr7035.banking.ui.feature_edit_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.alexandr7035.banking.domain.core.ErrorType
import by.alexandr7035.banking.domain.core.OperationResult
import by.alexandr7035.banking.domain.features.profile.GetCompactProfileUseCase
import by.alexandr7035.banking.domain.features.profile.UpdateProfileUseCase
import by.alexandr7035.banking.ui.core.error.asUiTextError
import by.alexandr7035.banking.ui.feature_cards.screen_add_card.UiField
import de.palm.composestateevents.consumed
import de.palm.composestateevents.triggered
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val getCompactProfileUseCase: GetCompactProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileScreenState())
    val state = _state.asStateFlow()

    private val errorHandler = CoroutineExceptionHandler { _, e ->
        _state.update {
            it.copy(
                isLoading = false,
                isSaving = false,
                error = ErrorType.fromThrowable(e).asUiTextError()
            )
        }
    }

    fun emitIntent(intent: EditProfileScreenIntent) {
        when (intent) {
            is EditProfileScreenIntent.EnterScreen -> loadProfile()
            is EditProfileScreenIntent.FirstNameChanged -> {
                _state.update { it.copy(firstName = UiField(value = intent.value)) }
            }
            is EditProfileScreenIntent.LastNameChanged -> {
                _state.update { it.copy(lastName = UiField(value = intent.value)) }
            }
            is EditProfileScreenIntent.NickNameChanged -> {
                _state.update { it.copy(nickName = UiField(value = intent.value)) }
            }
            is EditProfileScreenIntent.EmailChanged -> {
                _state.update { it.copy(email = UiField(value = intent.value)) }
            }
            is EditProfileScreenIntent.SaveProfile -> saveProfile()
        }
    }

    private fun loadProfile() {
        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch(errorHandler) {
            val profile = getCompactProfileUseCase.execute()
            _state.update {
                it.copy(
                    isLoading = false,
                    firstName = UiField(value = profile.firstName),
                    lastName = UiField(value = profile.lastName),
                    nickName = UiField(value = profile.nickName),
                    email = UiField(value = profile.email),
                    profilePicUrl = profile.profilePicUrl,
                    profileId = profile.id,
                )
            }
        }
    }

    private fun saveProfile() {
        val current = _state.value

        if (current.firstName.value.isBlank() || current.lastName.value.isBlank() || current.email.value.isBlank()) {
            _state.update {
                it.copy(
                    firstName = if (current.firstName.value.isBlank())
                        current.firstName.copy(error = by.alexandr7035.banking.ui.core.resources.UiText.DynamicString("Required"))
                    else current.firstName,
                    lastName = if (current.lastName.value.isBlank())
                        current.lastName.copy(error = by.alexandr7035.banking.ui.core.resources.UiText.DynamicString("Required"))
                    else current.lastName,
                    email = if (current.email.value.isBlank())
                        current.email.copy(error = by.alexandr7035.banking.ui.core.resources.UiText.DynamicString("Required"))
                    else current.email,
                )
            }
            return
        }

        _state.update { it.copy(isSaving = true) }

        viewModelScope.launch(errorHandler) {
            val profile = getCompactProfileUseCase.execute()
            val updatedProfile = profile.copy(
                firstName = current.firstName.value.trim(),
                lastName = current.lastName.value.trim(),
                nickName = current.nickName.value.trim(),
                email = current.email.value.trim(),
            )

            val result = OperationResult.runWrapped {
                updateProfileUseCase.execute(updatedProfile)
            }

            _state.update {
                it.copy(
                    isSaving = false,
                    saveEvent = triggered(result),
                )
            }
        }
    }

    fun consumeSaveEvent() {
        _state.update {
            it.copy(saveEvent = consumed())
        }
    }
}
