package by.alexandr7035.banking.ui.feature_cards.screen_add_card

import by.alexandr7035.banking.R
import by.alexandr7035.banking.domain.core.AppError
import by.alexandr7035.banking.domain.core.ErrorType
import by.alexandr7035.banking.domain.core.OperationResult
import by.alexandr7035.banking.domain.features.cards.AddCardUseCase
import by.alexandr7035.banking.domain.features.cards.model.AddCardPayload
import by.alexandr7035.banking.domain.features.validation.ValidateBillingAddressUseCase
import by.alexandr7035.banking.domain.features.validation.ValidateCardExpirationUseCase
import by.alexandr7035.banking.domain.features.validation.ValidateCardHolderUseCase
import by.alexandr7035.banking.domain.features.validation.ValidateCardNumberUseCase
import by.alexandr7035.banking.domain.features.validation.ValidateCvvCodeUseCase
import by.alexandr7035.banking.testutils.MainDispatcherRule
import by.alexandr7035.banking.testutils.TestData
import by.alexandr7035.banking.ui.core.resources.UiText
import de.palm.composestateevents.StateEventWithContentConsumed
import de.palm.composestateevents.StateEventWithContentTriggered
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddCardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val addCard: AddCardUseCase = mockk()

    private val vm = AddCardViewModel(
        validateCardNumberUseCase = ValidateCardNumberUseCase(),
        validateCvvCodeUseCase = ValidateCvvCodeUseCase(),
        validateCardExpirationUseCase = ValidateCardExpirationUseCase(),
        validateCardHolderUseCase = ValidateCardHolderUseCase(),
        validateBillingAddressUseCase = ValidateBillingAddressUseCase(),
        addCardUseCase = addCard,
    )

    private val futureExpiration = System.currentTimeMillis() + 60_000L

    private fun fillValidForm() {
        vm.emitIntent(AddCardIntent.StringFieldChanged(AddCardFieldType.CARD_NUMBER, TestData.VALID_CARD_NUMBER))
        vm.emitIntent(AddCardIntent.StringFieldChanged(AddCardFieldType.CVV_CODE, "123"))
        vm.emitIntent(AddCardIntent.StringFieldChanged(AddCardFieldType.CARD_HOLDER, "John Doe"))
        vm.emitIntent(AddCardIntent.StringFieldChanged(AddCardFieldType.ADDRESS_LINE_1, "1 Main St"))
        vm.emitIntent(AddCardIntent.StringFieldChanged(AddCardFieldType.ADDRESS_LINE_2, "Apt 2"))
        vm.emitIntent(AddCardIntent.ExpirationPickerSet(futureExpiration))
    }

    private fun UiField.errorRes(): Int? = (error as? UiText.StringResource)?.resId

    @Test
    fun `StringFieldChanged updates the corresponding field`() {
        vm.emitIntent(AddCardIntent.StringFieldChanged(AddCardFieldType.CARD_HOLDER, "Jane"))

        assertEquals(UiField("Jane"), vm.state.value.formFields.cardHolder)
        assertEquals(UiField(""), vm.state.value.formFields.cardNumber)
    }

    @Test
    fun `ExpirationPickerSet stores timestamp and formatted value`() {
        vm.emitIntent(AddCardIntent.ExpirationPickerSet(futureExpiration))
        assertEquals(futureExpiration, vm.state.value.formFields.expirationDateTimestamp)
        assertTrue(vm.state.value.formFields.expirationDate.value.isNotBlank())

        vm.emitIntent(AddCardIntent.ExpirationPickerSet(null))
        assertNull(vm.state.value.formFields.expirationDateTimestamp)
        assertEquals("-", vm.state.value.formFields.expirationDate.value)
    }

    @Test
    fun `ToggleDatePicker toggles flag`() {
        vm.emitIntent(AddCardIntent.ToggleDatePicker(true))
        assertTrue(vm.state.value.showDatePicker)
        vm.emitIntent(AddCardIntent.ToggleDatePicker(false))
        assertFalse(vm.state.value.showDatePicker)
    }

    @Test
    fun `SaveCard with empty form maps every field error and emits validation failure`() = runTest {
        vm.emitIntent(AddCardIntent.SaveCard)

        val state = vm.state.value
        val fields = state.formFields
        assertFalse(state.isLoading)
        assertEquals(R.string.field_is_empty, fields.cardNumber.errorRes())
        assertEquals(R.string.field_is_empty, fields.cvvCode.errorRes())
        assertEquals(R.string.date_not_specified, fields.expirationDate.errorRes())
        assertEquals(R.string.field_is_empty, fields.cardHolder.errorRes())
        assertEquals(R.string.field_is_empty, fields.addressFirstLine.errorRes())
        assertNull(fields.addressSecondLine.error)

        val event = state.cardSavedEvent as StateEventWithContentTriggered<OperationResult<Unit>>
        assertEquals(OperationResult.Failure(AppError(ErrorType.GENERIC_VALIDATION_ERROR)), event.content)
        coVerify(exactly = 0) { addCard.execute(any()) }
    }

    @Test
    fun `SaveCard validates whole chain, mapping specific errors per field`() = runTest {
        fillValidForm()
        vm.emitIntent(AddCardIntent.StringFieldChanged(AddCardFieldType.CARD_NUMBER, "1234567890123456"))
        vm.emitIntent(AddCardIntent.StringFieldChanged(AddCardFieldType.CVV_CODE, "12"))
        vm.emitIntent(AddCardIntent.ExpirationPickerSet(System.currentTimeMillis() - 1_000L))

        vm.emitIntent(AddCardIntent.SaveCard)

        val fields = vm.state.value.formFields
        assertEquals(R.string.invalid_card_number, fields.cardNumber.errorRes())
        assertEquals(R.string.invalid_cvv, fields.cvvCode.errorRes())
        assertEquals(R.string.card_has_expired, fields.expirationDate.errorRes())
        assertNull(fields.cardHolder.error)
        assertNull(fields.addressFirstLine.error)
        coVerify(exactly = 0) { addCard.execute(any()) }
    }

    @Test
    fun `editing a field clears its previous error`() = runTest {
        vm.emitIntent(AddCardIntent.SaveCard)
        assertEquals(R.string.field_is_empty, vm.state.value.formFields.cardHolder.errorRes())

        vm.emitIntent(AddCardIntent.StringFieldChanged(AddCardFieldType.CARD_HOLDER, "John"))

        assertNull(vm.state.value.formFields.cardHolder.error)
    }

    @Test
    fun `SaveCard with valid form forwards payload and emits success`() = runTest {
        val payload = slot<AddCardPayload>()
        coEvery { addCard.execute(capture(payload)) } returns Unit
        fillValidForm()

        vm.emitIntent(AddCardIntent.SaveCard)

        assertEquals(
            AddCardPayload(
                cardNumber = TestData.VALID_CARD_NUMBER,
                cardHolder = "John Doe",
                expirationDate = futureExpiration,
                addressFirstLine = "1 Main St",
                addressSecondLine = "Apt 2",
                cvvCode = "123"
            ),
            payload.captured
        )
        val state = vm.state.value
        assertFalse(state.isLoading)
        val event = state.cardSavedEvent as StateEventWithContentTriggered<OperationResult<Unit>>
        assertEquals(OperationResult.Success(Unit), event.content)

        vm.consumeSaveCardEvent()
        assertTrue(vm.state.value.cardSavedEvent is StateEventWithContentConsumed)
    }

    @Test
    fun `SaveCard with valid form propagates repository failure`() = runTest {
        coEvery { addCard.execute(any()) } throws AppError(ErrorType.CARD_ALREADY_ADDED)
        fillValidForm()

        vm.emitIntent(AddCardIntent.SaveCard)

        val event = vm.state.value.cardSavedEvent as StateEventWithContentTriggered<OperationResult<Unit>>
        assertEquals(OperationResult.Failure(AppError(ErrorType.CARD_ALREADY_ADDED)), event.content)
        assertFalse(vm.state.value.isLoading)
    }
}
