package by.alexandr7035.banking.domain.features.password_health

interface PasswordHealthRepository {
    suspend fun getReusedPasswordGroups(): List<ReusedPasswordGroup>
}
