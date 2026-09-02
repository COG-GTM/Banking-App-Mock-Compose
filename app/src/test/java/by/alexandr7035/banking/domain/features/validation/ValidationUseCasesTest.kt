package by.alexandr7035.banking.domain.features.validation

import by.alexandr7035.banking.domain.core.ErrorType
import by.alexandr7035.banking.domain.features.validation.model.ValidationResult
import org.junit.Assert.assertEquals
import org.junit.Test

class ValidateCardNumberUseCaseTest {
    private val useCase = ValidateCardNumberUseCase()

    @Test
    fun `valid Luhn numbers pass`() {
        listOf("4111111111111111", "5555555555554444", "378282246310005", "4242424242424242").forEach {
            assertEquals(it, ValidationResult(true), useCase.execute(it))
        }
    }

    @Test
    fun `number failing Luhn check returns INVALID_CARD_NUMBER`() {
        assertEquals(
            ValidationResult(false, ErrorType.INVALID_CARD_NUMBER),
            useCase.execute("4111111111111112")
        )
    }

    @Test
    fun `non numeric or formatted input returns INVALID_CARD_NUMBER`() {
        listOf("abcd", "4242 4242 4242 4242").forEach {
            assertEquals(it, ValidationResult(false, ErrorType.INVALID_CARD_NUMBER), useCase.execute(it))
        }
    }

    @Test
    fun `blank number returns FIELD_IS_EMPTY`() {
        assertEquals(ValidationResult(false, ErrorType.FIELD_IS_EMPTY), useCase.execute(""))
        assertEquals(ValidationResult(false, ErrorType.FIELD_IS_EMPTY), useCase.execute("   "))
    }
}

class ValidateCvvCodeUseCaseTest {
    private val useCase = ValidateCvvCodeUseCase()

    @Test
    fun `three and four digit codes are valid`() {
        assertEquals(ValidationResult(true), useCase.execute("123"))
        assertEquals(ValidationResult(true), useCase.execute("1234"))
    }

    @Test
    fun `blank code returns FIELD_IS_EMPTY`() {
        assertEquals(ValidationResult(false, ErrorType.FIELD_IS_EMPTY), useCase.execute(""))
    }

    @Test
    fun `wrong length or non digits return INVALID_CVV`() {
        listOf("12", "12345", "12a", "abc").forEach {
            assertEquals(it, ValidationResult(false, ErrorType.INVALID_CVV), useCase.execute(it))
        }
    }
}

class ValidateCardExpirationUseCaseTest {
    private val useCase = ValidateCardExpirationUseCase()

    @Test
    fun `null expiration returns DATE_UNSPECIFIED`() {
        assertEquals(ValidationResult(false, ErrorType.DATE_UNSPECIFIED), useCase.execute(null))
    }

    @Test
    fun `past expiration returns CARD_EXPIRED`() {
        assertEquals(
            ValidationResult(false, ErrorType.CARD_EXPIRED),
            useCase.execute(System.currentTimeMillis() - 1_000L)
        )
    }

    @Test
    fun `future expiration is valid`() {
        assertEquals(ValidationResult(true), useCase.execute(System.currentTimeMillis() + 60_000L))
    }
}

class ValidateCardHolderUseCaseTest {
    private val useCase = ValidateCardHolderUseCase()

    @Test
    fun `blank holder returns FIELD_IS_EMPTY`() {
        assertEquals(ValidationResult(false, ErrorType.FIELD_IS_EMPTY), useCase.execute(""))
        assertEquals(ValidationResult(false, ErrorType.FIELD_IS_EMPTY), useCase.execute("  "))
    }

    @Test
    fun `non blank holder is valid`() {
        assertEquals(ValidationResult(true), useCase.execute("John Doe"))
    }
}

class ValidateBillingAddressUseCaseTest {
    private val useCase = ValidateBillingAddressUseCase()

    @Test
    fun `blank first line returns FIELD_IS_EMPTY regardless of second line`() {
        assertEquals(
            ValidationResult(false, ErrorType.FIELD_IS_EMPTY),
            useCase.execute(addressFirstLine = "", addressSecondLine = "Apt 2")
        )
    }

    @Test
    fun `non blank first line is valid even with empty second line`() {
        assertEquals(
            ValidationResult(true),
            useCase.execute(addressFirstLine = "1 Main St", addressSecondLine = "")
        )
    }
}

class ValidatePasswordUseCaseTest {
    private val useCase = ValidatePasswordUseCase()

    @Test
    fun `password with 8+ chars, digit and letter is valid`() {
        assertEquals(ValidationResult(true), useCase.execute("1234567Ab"))
        assertEquals(ValidationResult(true), useCase.execute("abcdefg1"))
    }

    @Test
    fun `blank password returns FIELD_IS_EMPTY`() {
        assertEquals(ValidationResult(false, ErrorType.FIELD_IS_EMPTY), useCase.execute(""))
    }

    @Test
    fun `too short password returns INVALID_PASSWORD_FIELD`() {
        assertEquals(ValidationResult(false, ErrorType.INVALID_PASSWORD_FIELD), useCase.execute("abc1"))
    }

    @Test
    fun `password without digit returns INVALID_PASSWORD_FIELD`() {
        assertEquals(ValidationResult(false, ErrorType.INVALID_PASSWORD_FIELD), useCase.execute("abcdefghij"))
    }

    @Test
    fun `password without letter returns INVALID_PASSWORD_FIELD`() {
        assertEquals(ValidationResult(false, ErrorType.INVALID_PASSWORD_FIELD), useCase.execute("1234567890"))
    }
}
