package by.alexandr7035.banking.domain.features.password_health

data class LoginItem(
    val id: String,
    val name: String,
    val username: String
)

data class ReusedPasswordGroup(
    val id: String,
    val items: List<LoginItem>
)
