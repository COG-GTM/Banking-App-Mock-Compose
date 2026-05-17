package by.alexandr7035.banking.ui.feature_edit_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.alexandr7035.banking.domain.core.AppError
import by.alexandr7035.banking.domain.core.ErrorType
import by.alexandr7035.banking.domain.core.OperationResult
import by.alexandr7035.banking.domain.features.profile.GetCompactProfileUseCase
import by.alexandr7035.banking.domain.features.profile.UpdateProfileUseCase
import by.alexandr7035.banking.domain.features.validation.ValidateEmailUseCase
import by.alexandr7035.banking.domain.features.validation.ValidateNameUseCase
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
    private val validateNameUseCase: ValidateNameUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileState())
    val state = _state.asStateFlow()

    private val loadErrorHandler = CoroutineExceptionHandler { _, e ->
        _state.update {
            it.copy(
                isLoading = false,
                error = ErrorType.fromThrowable(e).asUiTextError()
            )
        }
    }

    private val saveErrorHandler = CoroutineExceptionHandler { _, e ->
        _state.update {
            it.copy(
                isSaving = false,
                saveEvent = triggered(OperationResult.Failure(AppError(ErrorType.fromThrowable(e))))
            )
        }
    }

    fun emitIntent(intent: EditProfileIntent) {
        when (intent) {
            is EditProfileIntent.EnterScreen -> if (_state.value.isLoading) loadProfile()
            is EditProfileIntent.FirstNameChanged -> {
                _state.update { it.copy(firstName = UiField(value = intent.value)) }
            }
            is EditProfileIntent.LastNameChanged -> {
                _state.update { it.copy(lastName = UiField(value = intent.value)) }
            }
            is EditProfileIntent.NickNameChanged -> {
                _state.update { it.copy(nickName = UiField(value = intent.value)) }
            }
            is EditProfileIntent.EmailChanged -> {
                _state.update { it.copy(email = UiField(value = intent.value)) }
            }
            is EditProfileIntent.SaveProfile -> saveProfile()
        }
    }

    private fun loadProfile() {
        viewModelScope.launch(loadErrorHandler) {
            val profile = getCompactProfileUseCase.execute()
            _state.update {
                it.copy(
                    isLoading = false,
                    firstName = UiField(value = profile.firstName),
                    lastName = UiField(value = profile.lastName),
                    nickName = UiField(value = profile.nickName),
                    email = UiField(value = profile.email),
                )
            }
        }
    }

    private fun saveProfile() {
        val currentState = _state.value

        val firstNameResult = validateNameUseCase.execute(currentState.firstName.value)
        val lastNameResult = validateNameUseCase.execute(currentState.lastName.value)
        val nickNameResult = validateNameUseCase.execute(currentState.nickName.value)
        val emailResult = validateEmailUseCase.execute(currentState.email.value)

        val hasErrors = listOf(firstNameResult, lastNameResult, nickNameResult, emailResult).any { !it.isValid }

        _state.update {
            it.copy(
                firstName = it.firstName.copy(
                    error = firstNameResult.validationError?.asUiTextError()
                ),
                lastName = it.lastName.copy(
                    error = lastNameResult.validationError?.asUiTextError()
                ),
                nickName = it.nickName.copy(
                    error = nickNameResult.validationError?.asUiTextError()
                ),
                email = it.email.copy(
                    error = emailResult.validationError?.asUiTextError()
                ),
            )
        }

        if (hasErrors) return

        _state.update { it.copy(isSaving = true) }

        viewModelScope.launch(saveErrorHandler) {
            updateProfileUseCase.execute(
                firstName = currentState.firstName.value,
                lastName = currentState.lastName.value,
                nickName = currentState.nickName.value,
                email = currentState.email.value,
            )

            _state.update {
                it.copy(
                    isSaving = false,
                    saveEvent = triggered(OperationResult.Success(Unit))
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
