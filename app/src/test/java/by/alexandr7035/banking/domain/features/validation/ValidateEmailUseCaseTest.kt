package by.alexandr7035.banking.domain.features.validation

import android.app.Application
import by.alexandr7035.banking.domain.core.ErrorType
import by.alexandr7035.banking.domain.features.validation.model.ValidationResult
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class ValidateEmailUseCaseTest {
    private val useCase = ValidateEmailUseCase()

    @Test
    fun `well formed email is valid`() {
        assertEquals(ValidationResult(true), useCase.execute("example@mail.com"))
        assertEquals(ValidationResult(true), useCase.execute("first.last+tag@sub.domain.org"))
    }

    @Test
    fun `blank email returns FIELD_IS_EMPTY`() {
        assertEquals(ValidationResult(false, ErrorType.FIELD_IS_EMPTY), useCase.execute(""))
        assertEquals(ValidationResult(false, ErrorType.FIELD_IS_EMPTY), useCase.execute("   "))
    }

    @Test
    fun `malformed email returns INVALID_EMAIL_FIELD`() {
        listOf("example", "example@", "@mail.com", "example@mail", "ex ample@mail.com").forEach {
            assertEquals(it, ValidationResult(false, ErrorType.INVALID_EMAIL_FIELD), useCase.execute(it))
        }
    }
}
