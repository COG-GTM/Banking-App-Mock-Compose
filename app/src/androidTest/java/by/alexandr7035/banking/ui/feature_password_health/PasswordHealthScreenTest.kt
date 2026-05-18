package by.alexandr7035.banking.ui.feature_password_health

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import by.alexandr7035.banking.domain.features.password_health.LoginItem
import by.alexandr7035.banking.domain.features.password_health.ReusedPasswordGroup
import by.alexandr7035.banking.ui.components.ScreenPreview
import org.junit.Rule
import org.junit.Test

class PasswordHealthScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // --- Empty state tests ---

    @Test
    fun emptyState_showsNoReusedPasswordsView() {
        composeTestRule.setContent {
            ScreenPreview {
                PasswordHealthScreenContent(
                    state = PasswordHealthState(
                        isLoading = false,
                        groups = emptyList()
                    )
                )
            }
        }

        composeTestRule.onNodeWithTag("NoReusedPasswordsView")
            .assertIsDisplayed()
    }

    @Test
    fun emptyState_showsNoReusedPasswordsText() {
        composeTestRule.setContent {
            ScreenPreview {
                PasswordHealthScreenContent(
                    state = PasswordHealthState(
                        isLoading = false,
                        groups = emptyList()
                    )
                )
            }
        }

        composeTestRule.onNodeWithText("No reused passwords detected.")
            .assertIsDisplayed()
    }

    @Test
    fun emptyState_showsReusedPasswordsTitle() {
        composeTestRule.setContent {
            ScreenPreview {
                PasswordHealthScreenContent(
                    state = PasswordHealthState(
                        isLoading = false,
                        groups = emptyList()
                    )
                )
            }
        }

        composeTestRule.onNodeWithText("Reused Passwords")
            .assertIsDisplayed()
    }

    // --- Loading state tests ---

    @Test
    fun loadingState_doesNotShowEmptyView() {
        composeTestRule.setContent {
            ScreenPreview {
                PasswordHealthScreenContent(
                    state = PasswordHealthState(
                        isLoading = true,
                        groups = emptyList()
                    )
                )
            }
        }

        composeTestRule.onNodeWithTag("NoReusedPasswordsView")
            .assertDoesNotExist()
    }

    @Test
    fun loadingState_doesNotShowList() {
        composeTestRule.setContent {
            ScreenPreview {
                PasswordHealthScreenContent(
                    state = PasswordHealthState(
                        isLoading = true,
                        groups = emptyList()
                    )
                )
            }
        }

        composeTestRule.onNodeWithTag("ReusedPasswordsList")
            .assertDoesNotExist()
    }

    // --- Grouped list tests ---

    @Test
    fun groupedList_showsReusedPasswordsList() {
        composeTestRule.setContent {
            ScreenPreview {
                PasswordHealthScreenContent(
                    state = PasswordHealthState(
                        isLoading = false,
                        groups = createMockGroups()
                    )
                )
            }
        }

        composeTestRule.onNodeWithTag("ReusedPasswordsList")
            .assertIsDisplayed()
    }

    @Test
    fun groupedList_displaysCorrectNumberOfGroups() {
        composeTestRule.setContent {
            ScreenPreview {
                PasswordHealthScreenContent(
                    state = PasswordHealthState(
                        isLoading = false,
                        groups = createMockGroups()
                    )
                )
            }
        }

        composeTestRule.onAllNodesWithTag("ReusedPasswordGroup")
            .assertCountEquals(3)
    }

    @Test
    fun groupedList_displaysThreeAccountsHeader() {
        composeTestRule.setContent {
            ScreenPreview {
                PasswordHealthScreenContent(
                    state = PasswordHealthState(
                        isLoading = false,
                        groups = createMockGroups()
                    )
                )
            }
        }

        composeTestRule.onNodeWithText("3 accounts use this password")
            .assertIsDisplayed()
    }

    @Test
    fun groupedList_displaysTwoAccountsHeaders() {
        composeTestRule.setContent {
            ScreenPreview {
                PasswordHealthScreenContent(
                    state = PasswordHealthState(
                        isLoading = false,
                        groups = createMockGroups()
                    )
                )
            }
        }

        composeTestRule.onAllNodesWithText("2 accounts use this password")
            .assertCountEquals(2)
    }

    @Test
    fun groupedList_displaysLoginItemNames() {
        composeTestRule.setContent {
            ScreenPreview {
                PasswordHealthScreenContent(
                    state = PasswordHealthState(
                        isLoading = false,
                        groups = createMockGroups()
                    )
                )
            }
        }

        composeTestRule.onNodeWithText("Gmail").assertIsDisplayed()
        composeTestRule.onNodeWithText("Outlook").assertIsDisplayed()
        composeTestRule.onNodeWithText("Yahoo Mail").assertIsDisplayed()
        composeTestRule.onNodeWithText("GitHub").assertIsDisplayed()
    }

    @Test
    fun groupedList_displaysLoginItemUsernames() {
        composeTestRule.setContent {
            ScreenPreview {
                PasswordHealthScreenContent(
                    state = PasswordHealthState(
                        isLoading = false,
                        groups = createMockGroups()
                    )
                )
            }
        }

        composeTestRule.onNodeWithText("user@gmail.com").assertIsDisplayed()
        composeTestRule.onNodeWithText("devuser").assertIsDisplayed()
    }

    @Test
    fun groupedList_displaysCorrectTotalCipherItems() {
        composeTestRule.setContent {
            ScreenPreview {
                PasswordHealthScreenContent(
                    state = PasswordHealthState(
                        isLoading = false,
                        groups = createMockGroups()
                    )
                )
            }
        }

        // 3 + 2 + 2 = 7 total items
        composeTestRule.onAllNodesWithTag("ReusedPasswordCipherItem")
            .assertCountEquals(7)
    }

    @Test
    fun groupedList_showsPasswordHealthToolbarTitle() {
        composeTestRule.setContent {
            ScreenPreview {
                PasswordHealthScreenContent(
                    state = PasswordHealthState(
                        isLoading = false,
                        groups = createMockGroups()
                    )
                )
            }
        }

        composeTestRule.onNodeWithText("Password Health")
            .assertIsDisplayed()
    }

    // --- Single group edge case ---

    @Test
    fun singleGroup_displaysOneGroupCard() {
        val singleGroup = listOf(
            ReusedPasswordGroup(
                id = "single",
                items = listOf(
                    LoginItem("1", "Service A", "user1"),
                    LoginItem("2", "Service B", "user2"),
                )
            )
        )

        composeTestRule.setContent {
            ScreenPreview {
                PasswordHealthScreenContent(
                    state = PasswordHealthState(
                        isLoading = false,
                        groups = singleGroup
                    )
                )
            }
        }

        composeTestRule.onAllNodesWithTag("ReusedPasswordGroup")
            .assertCountEquals(1)
        composeTestRule.onNodeWithText("2 accounts use this password")
            .assertIsDisplayed()
    }

    private fun createMockGroups(): List<ReusedPasswordGroup> {
        return listOf(
            ReusedPasswordGroup(
                id = "hash1",
                items = listOf(
                    LoginItem("1", "Gmail", "user@gmail.com"),
                    LoginItem("2", "Outlook", "user@outlook.com"),
                    LoginItem("3", "Yahoo Mail", "user@yahoo.com"),
                )
            ),
            ReusedPasswordGroup(
                id = "hash2",
                items = listOf(
                    LoginItem("4", "GitHub", "devuser"),
                    LoginItem("5", "GitLab", "devuser"),
                )
            ),
            ReusedPasswordGroup(
                id = "hash3",
                items = listOf(
                    LoginItem("6", "Disney+", "user@gmail.com"),
                    LoginItem("7", "Netflix", "user@gmail.com"),
                )
            )
        )
    }
}
