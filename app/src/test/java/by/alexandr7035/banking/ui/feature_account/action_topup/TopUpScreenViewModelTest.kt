package by.alexandr7035.banking.ui.feature_account.action_topup

import app.cash.turbine.test
import by.alexandr7035.banking.R
import by.alexandr7035.banking.domain.core.AppError
import by.alexandr7035.banking.domain.core.ErrorType
import by.alexandr7035.banking.domain.features.account.account_topup.GetSuggestedTopUpValuesUseCase
import by.alexandr7035.banking.domain.features.account.account_topup.TopUpAccountUseCase
import by.alexandr7035.banking.domain.features.account.model.MoneyAmount
import by.alexandr7035.banking.domain.features.cards.GetCardByIdUseCase
import by.alexandr7035.banking.domain.features.cards.GetDefaultCardUseCase
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
class TopUpScreenViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val getCardById: GetCardByIdUseCase = mockk()
    private val getDefaultCard: GetDefaultCardUseCase = mockk()
    private val topUp: TopUpAccountUseCase = mockk()

    private fun createViewModel() = TopUpScreenViewModel(
        getSuggestedTopUpValuesUseCase = GetSuggestedTopUpValuesUseCase(),
        getCardByIdUseCase = getCardById,
        getDefaultCardUseCase = getDefaultCard,
        topUpAccountUseCase = topUp,
    )

    private fun advance() = mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

    @Test
    fun `initial state proposes top up values and preselects the first one`() {
        val vm = createViewModel()

        val expected = GetSuggestedTopUpValuesUseCase().execute()
        assertEquals(expected, vm.state.value.amountState.proposedValues)
        assertEquals(expected.first(), vm.state.value.amountState.selectedAmount)
        assertFalse(vm.state.value.proceedButtonEnabled)
    }

    @Test
    fun `EnterScreen with card id loads that card and enables proceed`() = runTest {
        coEvery { getCardById.execute("c1") } returns TestData.paymentCard(cardId = "c1")
        val vm = createViewModel()

        vm.emitIntent(TopUpScreenIntent.EnterScreen("c1"))
        advance()

        assertEquals("c1", vm.state.value.cardPickerState.selectedCard?.id)
        assertTrue(vm.state.value.proceedButtonEnabled)
        coVerify(exactly = 0) { getDefaultCard.execute() }
    }

    @Test
    fun `EnterScreen without card id falls back to default card`() = runTest {
        coEvery { getDefaultCard.execute() } returns TestData.paymentCard(cardId = "default")
        val vm = createViewModel()

        vm.emitIntent(TopUpScreenIntent.EnterScreen(null))
        advance()

        assertEquals("default", vm.state.value.cardPickerState.selectedCard?.id)
    }

    @Test
    fun `ChooseCard failure triggers error event`() = runTest {
        coEvery { getCardById.execute("bad") } throws AppError(ErrorType.CARD_NOT_FOUND)
        val vm = createViewModel()

        vm.emitIntent(TopUpScreenIntent.ChooseCard("bad"))
        advance()

        val picker = vm.state.value.cardPickerState
        assertNull(picker.selectedCard)
        assertEquals(
            ErrorType.CARD_NOT_FOUND,
            (picker.cardSelectErrorEvent as StateEventWithContentTriggered<ErrorType>).content
        )

        vm.consumeLoadCardErrorEvent()
        assertFalse(vm.state.value.cardPickerState.cardSelectErrorEvent is StateEventWithContentTriggered<*>)
    }

    @Test
    fun `ProceedClick success shows dialog and forwards selected card and amount`() = runTest {
        coEvery { getCardById.execute("c1") } returns TestData.paymentCard(cardId = "c1")
        coEvery { topUp.execute(any(), any()) } returns Unit
        val vm = createViewModel()
        vm.emitIntent(TopUpScreenIntent.EnterScreen("c1"))
        advance()
        vm.emitIntent(TopUpScreenIntent.UpdateSelectedValue(MoneyAmount(150f)))

        vm.state.test {
            awaitItem()
            vm.emitIntent(TopUpScreenIntent.ProceedClick)
            assertTrue(awaitItem().isLoading)
            advance()
            val done = awaitItem()
            assertFalse(done.isLoading)
            assertTrue(done.showSuccessDialog)
        }

        coVerify(exactly = 1) { topUp.execute(cardId = "c1", amount = MoneyAmount(150f)) }
    }

    @Test
    fun `ProceedClick failure maps error`() = runTest {
        coEvery { getCardById.execute("c1") } returns TestData.paymentCard(cardId = "c1")
        coEvery { topUp.execute(any(), any()) } throws AppError(ErrorType.CARD_NOT_FOUND)
        val vm = createViewModel()
        vm.emitIntent(TopUpScreenIntent.EnterScreen("c1"))
        advance()

        vm.emitIntent(TopUpScreenIntent.ProceedClick)
        advance()

        assertFalse(vm.state.value.isLoading)
        assertFalse(vm.state.value.showSuccessDialog)
        assertEquals(R.string.card_not_found, (vm.state.value.error as UiText.StringResource).resId)
    }
}
