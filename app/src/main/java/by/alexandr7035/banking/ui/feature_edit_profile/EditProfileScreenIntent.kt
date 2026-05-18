package by.alexandr7035.banking.ui.feature_edit_profile

sealed class EditProfileScreenIntent {
    data class FirstNameChanged(val value: String) : EditProfileScreenIntent()
    data class LastNameChanged(val value: String) : EditProfileScreenIntent()
    data class NickNameChanged(val value: String) : EditProfileScreenIntent()
    data class EmailChanged(val value: String) : EditProfileScreenIntent()

    object SaveProfile : EditProfileScreenIntent()
}
