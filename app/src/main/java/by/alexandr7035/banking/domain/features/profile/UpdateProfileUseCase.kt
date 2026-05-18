package by.alexandr7035.banking.domain.features.profile

import by.alexandr7035.banking.domain.features.profile.model.CompactProfile

class UpdateProfileUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend fun execute(profile: CompactProfile) {
        profileRepository.updateProfile(profile)
    }
}
