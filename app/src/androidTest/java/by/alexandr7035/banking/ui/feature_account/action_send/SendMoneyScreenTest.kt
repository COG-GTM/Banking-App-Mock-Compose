package by.alexandr7035.banking.ui.feature_account.action_send

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import by.alexandr7035.banking.R
import by.alexandr7035.banking.domain.features.account.model.MoneyAmount
import by.alexandr7035.banking.ui.components.ScreenPreview
import by.alexandr7035.banking.ui.core.resources.UiText
import by.alexandr7035.banking.ui.feature_account.AmountPickersState
import by.alexandr7035.banking.ui.feature_account.CardPickerState
import by.alexandr7035.banking.ui.feature_account.ContactPickerState
import by.alexandr7035.banking.ui.feature_cards.model.CardUi
import by.alexandr7035.banking.ui.feature_contacts.model.ContactUi
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SendMoneyScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private fun str(id: Int) = context.getString(id)

    private val readyState = SendMoneyScreenState(
        cardPickerState = CardPickerState(selectedCard = CardUi.mock()),
        contactPickerState = ContactPickerState(selectedContact = ContactUi.mock()),
        amountState = AmountPickersState(
            selectedAmount = MoneyAmount(100f),
            proposedValues = setOf(MoneyAmount(100f), MoneyAmount(200f)),
            maxAmount = MoneyAmount(500f),
        )
    )

    private fun setScreen(state: SendMoneyScreenState, onIntent: (SendMoneyScreenIntent) -> Unit = {}) {
        composeRule.setContent {
            ScreenPreview {
                SendMoneyScreen_Ui(state = state, onIntent = onIntent)
            }
        }
    }

    @Test
    fun proceedIsEnabledWhenCardContactAndAmountAreSelected() {
        val intents = mutableListOf<SendMoneyScreenIntent>()
        setScreen(readyState) { intents += it }

        composeRule.onNodeWithText(str(R.string.proceed)).assertIsDisplayed().assertIsEnabled().performClick()

        assertEquals(listOf<SendMoneyScreenIntent>(SendMoneyScreenIntent.ProceedClick), intents)
    }

    @Test
    fun proceedIsDisabledWithoutContact() {
        setScreen(readyState.copy(contactPickerState = ContactPickerState(selectedContact = null)))

        composeRule.onNodeWithText(str(R.string.proceed)).assertIsDisplayed().assertIsNotEnabled()
    }

    @Test
    fun proceedIsDisabledWhenAmountIsZero() {
        setScreen(readyState.copy(amountState = readyState.amountState.copy(selectedAmount = MoneyAmount(0f))))

        composeRule.onNodeWithText(str(R.string.proceed)).assertIsNotEnabled()
    }

    @Test
    fun proceedIsDisabledWhileCardIsLoading() {
        setScreen(readyState.copy(cardPickerState = readyState.cardPickerState.copy(isLoading = true)))

        composeRule.onNodeWithText(str(R.string.proceed)).assertIsNotEnabled()
    }

    @Test
    fun successDialogIsShownAndDismissEmitsIntent() {
        val intents = mutableListOf<SendMoneyScreenIntent>()
        setScreen(readyState.copy(showSuccessDialog = true)) { intents += it }

        composeRule.onNodeWithText(str(R.string.transaction_submitted)).assertIsDisplayed()
        composeRule.onNodeWithText(str(R.string.transaction_explanation)).assertIsDisplayed()
    }

    @Test
    fun successDialogIsHiddenByDefault() {
        setScreen(readyState)

        composeRule.onNodeWithText(str(R.string.transaction_submitted)).assertDoesNotExist()
    }

    @Test
    fun amountErrorIsRendered() {
        setScreen(
            readyState.copy(
                amountState = readyState.amountState.copy(
                    pickersEnabled = false,
                    error = UiText.StringResource(R.string.insufficient_card_balance)
                )
            )
        )

        composeRule.onNodeWithText(str(R.string.insufficient_card_balance)).assertIsDisplayed()
    }

    @Test
    fun cancelInvokesOnBack() {
        var backCalls = 0
        composeRule.setContent {
            ScreenPreview {
                SendMoneyScreen_Ui(state = readyState, onBack = { backCalls++ })
            }
        }

        composeRule.onNodeWithText(str(R.string.cancel)).performClick()

        assertEquals(1, backCalls)
    }
}
