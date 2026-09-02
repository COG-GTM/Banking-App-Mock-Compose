package by.alexandr7035.banking.ui.feature_account.action_send

import app.cash.turbine.test
import by.alexandr7035.banking.R
import by.alexandr7035.banking.domain.core.AppError
import by.alexandr7035.banking.domain.core.ErrorType
import by.alexandr7035.banking.domain.features.account.model.MoneyAmount
import by.alexandr7035.banking.domain.features.account.send_money.GetSuggestedSendValuesForCardBalance
import by.alexandr7035.banking.domain.features.account.send_money.SendMoneyUseCase
import by.alexandr7035.banking.domain.features.cards.GetCardByIdUseCase
import by.alexandr7035.banking.domain.features.cards.GetDefaultCardUseCase
import by.alexandr7035.banking.domain.features.contacts.GetContactByIdUseCase
import by.alexandr7035.banking.domain.features.contacts.GetRecentContactUseCase
import by.alexandr7035.banking.testutils.MainDispatcherRule
import by.alexandr7035.banking.testutils.TestData
import by.alexandr7035.banking.ui.core.resources.UiText
import de.palm.composestateevents.StateEventWithContentTriggered
import io.mockk.coEvery
import io.mockk.coVerify
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
class SendMoneyViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val getSuggestedSendValues = GetSuggestedSendValuesForCardBalance()
    private val getCardById: GetCardByIdUseCase = mockk()
    private val getDefaultCard: GetDefaultCardUseCase = mockk()
    private val getRecentContact: GetRecentContactUseCase = mockk()
    private val getContactById: GetContactByIdUseCase = mockk()
    private val sendMoney: SendMoneyUseCase = mockk()

    private fun createViewModel() = SendMoneyViewModel(
        getSuggestedSendValuesForCardBalance = getSuggestedSendValues,
        getCardByIdUseCase = getCardById,
        getDefaultCardUseCase = getDefaultCard,
        getRecentContactUseCase = getRecentContact,
        getContactByIdUseCase = getContactById,
        sendMoneyUseCase = sendMoney,
    )

    private val card = TestData.paymentCard(cardId = "card-1", balance = 200f)
    private val contact = TestData.contact(id = 9L)

    private fun SendMoneyViewModel.enterScreenWithCardAndContact() {
        coEvery { getDefaultCard.execute() } returns card
        coEvery { getRecentContact.execute() } returns contact
        emitIntent(SendMoneyScreenIntent.EnterScreen(selectedCardId = null))
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun `EnterScreen loads default card and recent contact and enables proceed`() = runTest {
        val vm = createViewModel()
        assertFalse(vm.state.value.proceedButtonEnabled)

        vm.enterScreenWithCardAndContact()

        val state = vm.state.value
        assertEquals("card-1", state.cardPickerState.selectedCard?.id)
        assertEquals(9L, state.contactPickerState.selectedContact?.id)
        assertFalse(state.cardPickerState.isLoading)
        assertFalse(state.contactPickerState.isLoading)
        assertTrue(state.amountState.pickersEnabled)
        assertEquals(MoneyAmount(200f), state.amountState.maxAmount)
        assertEquals(getSuggestedSendValues.execute(MoneyAmount(200f)), state.amountState.proposedValues)
        assertEquals(state.amountState.proposedValues.first(), state.amountState.selectedAmount)
        assertTrue(state.proceedButtonEnabled)
    }

    @Test
    fun `card with zero balance disables pickers and shows insufficient balance error`() = runTest {
        coEvery { getDefaultCard.execute() } returns TestData.paymentCard(cardId = "empty", balance = 0f)
        coEvery { getRecentContact.execute() } returns contact
        val vm = createViewModel()

        vm.emitIntent(SendMoneyScreenIntent.EnterScreen(null))
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val amount = vm.state.value.amountState
        assertFalse(amount.pickersEnabled)
        assertNull(amount.maxAmount)
        assertEquals(MoneyAmount(0f), amount.selectedAmount)
        assertEquals(R.string.insufficient_card_balance, (amount.error as UiText.StringResource).resId)
        assertFalse(vm.state.value.proceedButtonEnabled)
    }

    @Test
    fun `ChooseCard failure clears card and triggers error event`() = runTest {
        coEvery { getCardById.execute("missing") } throws AppError(ErrorType.CARD_NOT_FOUND)
        val vm = createViewModel()

        vm.emitIntent(SendMoneyScreenIntent.ChooseCard("missing"))
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val picker = vm.state.value.cardPickerState
        assertNull(picker.selectedCard)
        assertFalse(picker.isLoading)
        val event = picker.cardSelectErrorEvent as StateEventWithContentTriggered<ErrorType>
        assertEquals(ErrorType.CARD_NOT_FOUND, event.content)
    }

    @Test
    fun `ProceedClick success transitions loading then shows success dialog`() = runTest {
        val vm = createViewModel()
        vm.enterScreenWithCardAndContact()
        vm.emitIntent(SendMoneyScreenIntent.UpdateSelectedValue(MoneyAmount(50f)))
        coEvery { sendMoney.execute(any(), any(), any()) } returns Unit

        vm.state.test {
            assertFalse(awaitItem().isLoading)

            vm.emitIntent(SendMoneyScreenIntent.ProceedClick)
            val loading = awaitItem()
            assertTrue(loading.isLoading)
            assertFalse(loading.showSuccessDialog)

            mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
            val done = awaitItem()
            assertFalse(done.isLoading)
            assertTrue(done.showSuccessDialog)
            assertNull(done.error)
        }

        coVerify(exactly = 1) {
            sendMoney.execute(amount = MoneyAmount(50f), fromCardId = "card-1", contactId = 9L)
        }
    }

    @Test
    fun `ProceedClick failure maps error to UiText and hides loading`() = runTest {
        val vm = createViewModel()
        vm.enterScreenWithCardAndContact()
        coEvery { sendMoney.execute(any(), any(), any()) } throws AppError(ErrorType.INSUFFICIENT_CARD_BALANCE)

        vm.state.test {
            awaitItem()
            vm.emitIntent(SendMoneyScreenIntent.ProceedClick)
            assertTrue(awaitItem().isLoading)

            mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
            val failed = awaitItem()
            assertFalse(failed.isLoading)
            assertFalse(failed.showSuccessDialog)
            assertEquals(
                R.string.insufficient_card_balance,
                (failed.error as UiText.StringResource).resId
            )
        }
    }

    @Test
    fun `ProceedClick without selected contact does not call use case`() = runTest {
        coEvery { getDefaultCard.execute() } returns card
        coEvery { getRecentContact.execute() } returns null
        val vm = createViewModel()
        vm.emitIntent(SendMoneyScreenIntent.EnterScreen(null))
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(vm.state.value.proceedButtonEnabled)

        vm.emitIntent(SendMoneyScreenIntent.ProceedClick)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { sendMoney.execute(any(), any(), any()) }
        assertFalse(vm.state.value.showSuccessDialog)
    }

    @Test
    fun `DismissSuccessDialog hides dialog and triggers back navigation`() = runTest {
        val vm = createViewModel()
        vm.enterScreenWithCardAndContact()
        coEvery { sendMoney.execute(any(), any(), any()) } returns Unit
        vm.emitIntent(SendMoneyScreenIntent.ProceedClick)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state.value.showSuccessDialog)

        vm.emitIntent(SendMoneyScreenIntent.DismissSuccessDialog)

        assertFalse(vm.state.value.showSuccessDialog)
        assertEquals(de.palm.composestateevents.triggered, vm.state.value.requiredBackNavEvent)

        vm.consumeBackNavEvent()
        assertEquals(de.palm.composestateevents.consumed, vm.state.value.requiredBackNavEvent)
    }
}
