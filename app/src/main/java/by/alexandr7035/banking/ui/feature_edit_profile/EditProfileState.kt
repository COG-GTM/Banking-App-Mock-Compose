package by.alexandr7035.banking.ui.feature_edit_profile

import by.alexandr7035.banking.domain.core.OperationResult
import by.alexandr7035.banking.ui.feature_cards.screen_add_card.UiField
import de.palm.composestateevents.StateEventWithContent
import de.palm.composestateevents.consumed

data class EditProfileState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val firstName: UiField = UiField(value = ""),
    val lastName: UiField = UiField(value = ""),
    val nickName: UiField = UiField(value = ""),
    val email: UiField = UiField(value = ""),
    val saveEvent: StateEventWithContent<OperationResult<Unit>> = consumed(),
)
