package by.alexandr7035.banking.ui.feature_password_health

import by.alexandr7035.banking.domain.features.password_health.ReusedPasswordGroup

data class PasswordHealthState(
    val isLoading: Boolean = true,
    val groups: List<ReusedPasswordGroup> = emptyList()
)
