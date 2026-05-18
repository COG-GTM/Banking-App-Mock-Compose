package by.alexandr7035.banking.ui.feature_edit_profile

sealed class EditProfileIntent {
    object EnterScreen : EditProfileIntent()
    data class FirstNameChanged(val value: String) : EditProfileIntent()
    data class LastNameChanged(val value: String) : EditProfileIntent()
    data class NickNameChanged(val value: String) : EditProfileIntent()
    data class EmailChanged(val value: String) : EditProfileIntent()
    object SaveProfile : EditProfileIntent()
}
