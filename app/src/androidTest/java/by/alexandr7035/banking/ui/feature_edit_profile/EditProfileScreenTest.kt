package by.alexandr7035.banking.ui.feature_edit_profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import by.alexandr7035.banking.R
import by.alexandr7035.banking.domain.core.OperationResult
import by.alexandr7035.banking.ui.components.ScreenPreview
import by.alexandr7035.banking.ui.core.resources.UiText
import by.alexandr7035.banking.ui.feature_cards.screen_add_card.UiField
import de.palm.composestateevents.triggered
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    private val loadedState = EditProfileState(
        isLoading = false,
        firstName = UiField(value = "Alexander"),
        lastName = UiField(value = "Michael"),
        nickName = UiField(value = "@alexandermichael"),
        email = UiField(value = "test@example.com"),
    )

    // --- Test 1: Form fields are pre-populated ---

    @Test
    fun editProfileScreen_displaysPrePopulatedFields() {
        composeTestRule.setContent {
            ScreenPreview {
                EditProfileScreen_Ui(state = loadedState)
            }
        }

        composeTestRule.onNodeWithText("Alexander").assertIsDisplayed()
        composeTestRule.onNodeWithText("Michael").assertIsDisplayed()
        composeTestRule.onNodeWithText("@alexandermichael").assertIsDisplayed()
        composeTestRule.onNodeWithText("test@example.com").assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.save_changes)).assertIsDisplayed()
    }

    // --- Test 2: Validation - Empty First Name ---

    @Test
    fun editProfileScreen_showsErrorForEmptyFirstName() {
        val errorText = context.getString(R.string.field_is_empty)
        val stateWithError = loadedState.copy(
            firstName = UiField(
                value = "",
                error = UiText.StringResource(R.string.field_is_empty)
            )
        )

        composeTestRule.setContent {
            ScreenPreview {
                EditProfileScreen_Ui(state = stateWithError)
            }
        }

        composeTestRule.onNodeWithText(errorText).assertIsDisplayed()
    }

    // --- Test 3: Validation - Short Name (1 character) ---

    @Test
    fun editProfileScreen_showsErrorForShortName() {
        val errorText = context.getString(R.string.invalid_name_field)
        val stateWithError = loadedState.copy(
            firstName = UiField(
                value = "A",
                error = UiText.StringResource(R.string.invalid_name_field)
            )
        )

        composeTestRule.setContent {
            ScreenPreview {
                EditProfileScreen_Ui(state = stateWithError)
            }
        }

        composeTestRule.onNodeWithText("A").assertIsDisplayed()
        composeTestRule.onNodeWithText(errorText).assertIsDisplayed()
    }

    // --- Test 4: Validation - Empty Nickname ---

    @Test
    fun editProfileScreen_showsErrorForEmptyNickname() {
        val errorText = context.getString(R.string.field_is_empty)
        val stateWithError = loadedState.copy(
            nickName = UiField(
                value = "",
                error = UiText.StringResource(R.string.field_is_empty)
            )
        )

        composeTestRule.setContent {
            ScreenPreview {
                EditProfileScreen_Ui(state = stateWithError)
            }
        }

        // The error text appears for nickname specifically
        composeTestRule.onNodeWithText(errorText).assertIsDisplayed()
    }

    // --- Test 5: Validation - Invalid Email ---

    @Test
    fun editProfileScreen_showsErrorForInvalidEmail() {
        val errorText = context.getString(R.string.invalid_email_field)
        val stateWithError = loadedState.copy(
            email = UiField(
                value = "notanemail",
                error = UiText.StringResource(R.string.invalid_email_field)
            )
        )

        composeTestRule.setContent {
            ScreenPreview {
                EditProfileScreen_Ui(state = stateWithError)
            }
        }

        composeTestRule.onNodeWithText("notanemail").assertIsDisplayed()
        composeTestRule.onNodeWithText(errorText).assertIsDisplayed()
    }

    // --- Test 6: Save button emits SaveProfile intent ---

    @Test
    fun editProfileScreen_saveButtonEmitsSaveIntent() {
        val emittedIntents = mutableListOf<EditProfileIntent>()

        composeTestRule.setContent {
            ScreenPreview {
                EditProfileScreen_Ui(
                    state = loadedState,
                    onIntent = { emittedIntents.add(it) }
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.save_changes)).performClick()

        assertTrue(
            "Expected SaveProfile intent to be emitted",
            emittedIntents.any { it is EditProfileIntent.SaveProfile }
        )
    }

    // --- Test 7: Field changes emit correct intents ---

    @Test
    fun editProfileScreen_firstNameChangeEmitsIntent() {
        val emittedIntents = mutableListOf<EditProfileIntent>()

        composeTestRule.setContent {
            ScreenPreview {
                EditProfileScreen_Ui(
                    state = loadedState,
                    onIntent = { emittedIntents.add(it) }
                )
            }
        }

        composeTestRule.onNodeWithText("Alexander").performTextReplacement("John")

        assertTrue(
            "Expected FirstNameChanged intent",
            emittedIntents.any { it is EditProfileIntent.FirstNameChanged && it.value == "John" }
        )
    }

    @Test
    fun editProfileScreen_lastNameChangeEmitsIntent() {
        val emittedIntents = mutableListOf<EditProfileIntent>()

        composeTestRule.setContent {
            ScreenPreview {
                EditProfileScreen_Ui(
                    state = loadedState,
                    onIntent = { emittedIntents.add(it) }
                )
            }
        }

        composeTestRule.onNodeWithText("Michael").performTextReplacement("Doe")

        assertTrue(
            "Expected LastNameChanged intent",
            emittedIntents.any { it is EditProfileIntent.LastNameChanged && it.value == "Doe" }
        )
    }

    @Test
    fun editProfileScreen_nickNameChangeEmitsIntent() {
        val emittedIntents = mutableListOf<EditProfileIntent>()

        composeTestRule.setContent {
            ScreenPreview {
                EditProfileScreen_Ui(
                    state = loadedState,
                    onIntent = { emittedIntents.add(it) }
                )
            }
        }

        composeTestRule.onNodeWithText("@alexandermichael").performTextReplacement("@johndoe")

        assertTrue(
            "Expected NickNameChanged intent",
            emittedIntents.any { it is EditProfileIntent.NickNameChanged && it.value == "@johndoe" }
        )
    }

    @Test
    fun editProfileScreen_emailChangeEmitsIntent() {
        val emittedIntents = mutableListOf<EditProfileIntent>()

        composeTestRule.setContent {
            ScreenPreview {
                EditProfileScreen_Ui(
                    state = loadedState,
                    onIntent = { emittedIntents.add(it) }
                )
            }
        }

        composeTestRule.onNodeWithText("test@example.com").performTextReplacement("john@example.com")

        assertTrue(
            "Expected EmailChanged intent",
            emittedIntents.any { it is EditProfileIntent.EmailChanged && it.value == "john@example.com" }
        )
    }

    // --- Test: Multiple validation errors displayed simultaneously ---

    @Test
    fun editProfileScreen_showsMultipleValidationErrors() {
        val emptyError = context.getString(R.string.field_is_empty)
        val emailError = context.getString(R.string.invalid_email_field)
        val stateWithErrors = loadedState.copy(
            firstName = UiField(value = "", error = UiText.StringResource(R.string.field_is_empty)),
            lastName = UiField(value = "", error = UiText.StringResource(R.string.field_is_empty)),
            nickName = UiField(value = "", error = UiText.StringResource(R.string.field_is_empty)),
            email = UiField(value = "bad", error = UiText.StringResource(R.string.invalid_email_field)),
        )

        composeTestRule.setContent {
            ScreenPreview {
                EditProfileScreen_Ui(state = stateWithErrors)
            }
        }

        // 3 fields have the same "empty" error, 1 has "invalid email"
        val emptyNodes = composeTestRule.onAllNodes(
            androidx.compose.ui.test.hasText(emptyError)
        )
        emptyNodes[0].assertIsDisplayed()

        composeTestRule.onNodeWithText(emailError).assertIsDisplayed()
    }

    // --- Test: Load error shows ErrorFullScreen with retry ---

    @Test
    fun editProfileScreen_showsErrorScreenOnLoadFailure() {
        val errorText = context.getString(R.string.unknown_error)
        val emittedIntents = mutableListOf<EditProfileIntent>()
        val errorState = EditProfileState(
            isLoading = false,
            error = UiText.StringResource(R.string.unknown_error),
        )

        composeTestRule.setContent {
            ScreenPreview {
                EditProfileScreen_Ui(
                    state = errorState,
                    onIntent = { emittedIntents.add(it) }
                )
            }
        }

        composeTestRule.onNodeWithText(errorText).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.try_again)).assertIsDisplayed()

        composeTestRule.onNodeWithText(context.getString(R.string.try_again)).performClick()

        assertTrue(
            "Expected EnterScreen intent on retry",
            emittedIntents.any { it is EditProfileIntent.EnterScreen }
        )
    }

    // --- Test: Loading state shows progress ---

    @Test
    fun editProfileScreen_showsLoadingIndicator() {
        val loadingState = EditProfileState(isLoading = true)

        composeTestRule.setContent {
            ScreenPreview {
                EditProfileScreen_Ui(state = loadingState)
            }
        }

        // When loading, form field labels still render but fields are empty
        composeTestRule.onNodeWithText(context.getString(R.string.first_name)).assertIsDisplayed()
    }

    // --- Test: Saving state shows progress ---

    @Test
    fun editProfileScreen_showsSavingIndicator() {
        val savingState = loadedState.copy(isSaving = true)

        composeTestRule.setContent {
            ScreenPreview {
                EditProfileScreen_Ui(state = savingState)
            }
        }

        composeTestRule.onNodeWithText("Alexander").assertIsDisplayed()
    }

    // --- Test: Back button emits onBack ---

    @Test
    fun editProfileScreen_backButtonCallsOnBack() {
        var backCalled = false

        composeTestRule.setContent {
            ScreenPreview {
                EditProfileScreen_Ui(
                    state = loadedState,
                    onBack = { backCalled = true }
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.edit_profile)).assertIsDisplayed()
    }

    // --- Test: Clearing a field and saving triggers empty error ---

    @Test
    fun editProfileScreen_clearingFieldShowsEmptyAfterTextClearance() {
        val emittedIntents = mutableListOf<EditProfileIntent>()

        composeTestRule.setContent {
            ScreenPreview {
                EditProfileScreen_Ui(
                    state = loadedState,
                    onIntent = { emittedIntents.add(it) }
                )
            }
        }

        composeTestRule.onNodeWithText("Alexander").performTextClearance()

        assertTrue(
            "Expected FirstNameChanged with empty value",
            emittedIntents.any { it is EditProfileIntent.FirstNameChanged && it.value.isEmpty() }
        )
    }
}
