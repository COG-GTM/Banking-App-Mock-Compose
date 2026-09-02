package by.alexandr7035.banking.ui.feature_cards.screen_add_card

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import by.alexandr7035.banking.R
import by.alexandr7035.banking.ui.components.ScreenPreview
import by.alexandr7035.banking.ui.core.resources.UiText
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddCardScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private fun str(id: Int) = context.getString(id)

    @Test
    fun fieldValidationErrorsAreDisplayed() {
        val state = AddCardState(
            formFields = AddCardFormFields(
                cardNumber = UiField("123", UiText.StringResource(R.string.invalid_card_number)),
                cvvCode = UiField("1", UiText.StringResource(R.string.invalid_cvv)),
                cardHolder = UiField("", UiText.StringResource(R.string.field_is_empty)),
            )
        )
        composeRule.setContent {
            ScreenPreview { AddCardScreen_Ui(state = state) }
        }

        composeRule.onNodeWithText(str(R.string.invalid_card_number)).assertIsDisplayed()
        composeRule.onNodeWithText(str(R.string.invalid_cvv)).assertIsDisplayed()
        composeRule.onNodeWithText(str(R.string.field_is_empty)).assertIsDisplayed()
    }

    @Test
    fun noErrorsDisplayedForCleanForm() {
        composeRule.setContent {
            ScreenPreview { AddCardScreen_Ui(state = AddCardState()) }
        }

        composeRule.onAllNodesWithText(str(R.string.field_is_empty)).assertCountEquals(0)
        composeRule.onAllNodesWithText(str(R.string.invalid_card_number)).assertCountEquals(0)
    }

    @Test
    fun saveButtonEmitsSaveCardIntent() {
        val intents = mutableListOf<AddCardIntent>()
        composeRule.setContent {
            ScreenPreview { AddCardScreen_Ui(state = AddCardState(), onIntent = { intents += it }) }
        }

        composeRule.onNodeWithText(str(R.string.save_card)).performClick()

        assertEquals(listOf<AddCardIntent>(AddCardIntent.SaveCard), intents)
    }
}
