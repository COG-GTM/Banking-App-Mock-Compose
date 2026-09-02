package by.alexandr7035.banking.ui.feature_login

import by.alexandr7035.banking.R
import by.alexandr7035.banking.domain.core.AppError
import by.alexandr7035.banking.domain.core.ErrorType
import by.alexandr7035.banking.domain.core.OperationResult
import by.alexandr7035.banking.domain.features.login.LoginWithEmailUseCase
import by.alexandr7035.banking.domain.features.validation.ValidateEmailUseCase
import by.alexandr7035.banking.domain.features.validation.ValidatePasswordUseCase
import by.alexandr7035.banking.domain.features.validation.model.ValidationResult
import by.alexandr7035.banking.testutils.MainDispatcherRule
import by.alexandr7035.banking.ui.core.resources.UiText
import by.alexandr7035.banking.ui.feature_cards.screen_add_card.UiField
import de.palm.composestateevents.StateEventWithContentConsumed
import de.palm.composestateevents.StateEventWithContentTriggered
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val loginWithEmail: LoginWithEmailUseCase = mockk()
    private val validateEmail: ValidateEmailUseCase = mockk()
    private val validatePassword = ValidatePasswordUseCase()

    private val vm = LoginViewModel(loginWithEmail, validatePassword, validateEmail)

    private fun advance() = mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

    private fun UiField.errorRes(): Int? = (error as? UiText.StringResource)?.resId

    private fun stubEmailValidation() {
        every { validateEmail.execute(any()) } answers {
            val email = firstArg<String>()
            when {
                email.isBlank() -> ValidationResult(false, ErrorType.FIELD_IS_EMPTY)
                "@" !in email -> ValidationResult(false, ErrorType.INVALID_EMAIL_FIELD)
                else -> ValidationResult(true)
            }
        }
    }

    private fun setFields(email: String, password: String) {
        vm.emitIntent(LoginIntent.LoginFieldChanged(LoginFieldType.EMAIL, email))
        vm.emitIntent(LoginIntent.LoginFieldChanged(LoginFieldType.PASSWORD, password))
    }

    @Test
    fun `LoginFieldChanged updates fields`() {
        setFields("a@b.com", "secret12")

        assertEquals(UiField("a@b.com"), vm.loginState.value.formFields.loginField)
        assertEquals(UiField("secret12"), vm.loginState.value.formFields.passwordField)
    }

    @Test
    fun `SubmitForm with invalid fields sets per-field errors and validation failure event`() = runTest {
        stubEmailValidation()
        setFields("not-an-email", "short")

        vm.emitIntent(LoginIntent.SubmitForm)

        val state = vm.loginState.value
        assertEquals(R.string.invalid_email_field, state.formFields.loginField.errorRes())
        assertEquals(R.string.invalid_password_field, state.formFields.passwordField.errorRes())
        assertFalse(state.isLoading)
        val event = state.loginEvent as StateEventWithContentTriggered<OperationResult<Unit>>
        assertEquals(OperationResult.Failure(AppError(ErrorType.GENERIC_VALIDATION_ERROR)), event.content)
        coVerify(exactly = 0) { loginWithEmail.execute(any(), any()) }
    }

    @Test
    fun `SubmitForm with empty fields reports FIELD_IS_EMPTY`() = runTest {
        stubEmailValidation()
        setFields("", "")

        vm.emitIntent(LoginIntent.SubmitForm)

        assertEquals(R.string.field_is_empty, vm.loginState.value.formFields.loginField.errorRes())
        assertEquals(R.string.field_is_empty, vm.loginState.value.formFields.passwordField.errorRes())
    }

    @Test
    fun `SubmitForm with valid fields logs in and emits success`() = runTest {
        stubEmailValidation()
        coEvery { loginWithEmail.execute("example@mail.com", "1234567Ab") } returns Unit
        setFields("example@mail.com", "1234567Ab")

        vm.emitIntent(LoginIntent.SubmitForm)
        assertTrue(vm.loginState.value.isLoading)
        advance()

        val state = vm.loginState.value
        assertFalse(state.isLoading)
        assertNull(state.formFields.loginField.error)
        assertNull(state.formFields.passwordField.error)
        val event = state.loginEvent as StateEventWithContentTriggered<OperationResult<Unit>>
        assertEquals(OperationResult.Success(Unit), event.content)

        vm.onLoginEventConsumed()
        assertTrue(vm.loginState.value.loginEvent is StateEventWithContentConsumed)
    }

    @Test
    fun `SubmitForm propagates login failure`() = runTest {
        stubEmailValidation()
        coEvery { loginWithEmail.execute(any(), any()) } throws AppError(ErrorType.WRONG_PASSWORD)
        setFields("example@mail.com", "1234567Ab")

        vm.emitIntent(LoginIntent.SubmitForm)
        advance()

        val event = vm.loginState.value.loginEvent as StateEventWithContentTriggered<OperationResult<Unit>>
        assertEquals(OperationResult.Failure(AppError(ErrorType.WRONG_PASSWORD)), event.content)
        assertFalse(vm.loginState.value.isLoading)
    }
}
