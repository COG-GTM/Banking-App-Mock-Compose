package by.alexandr7035.banking.domain.features.profile

class UpdateProfileUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend fun execute(firstName: String, lastName: String, nickName: String, email: String) {
        profileRepository.updateProfile(firstName, lastName, nickName, email)
    }
}
