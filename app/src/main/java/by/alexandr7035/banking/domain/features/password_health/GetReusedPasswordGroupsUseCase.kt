package by.alexandr7035.banking.domain.features.password_health

class GetReusedPasswordGroupsUseCase(
    private val repository: PasswordHealthRepository
) {
    suspend fun execute(): List<ReusedPasswordGroup> {
        return repository.getReusedPasswordGroups()
    }
}
