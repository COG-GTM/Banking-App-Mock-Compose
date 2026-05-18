package by.alexandr7035.banking.data.profile

import by.alexandr7035.banking.domain.features.profile.model.CompactProfile
import by.alexandr7035.banking.domain.features.profile.ProfileRepository
import by.alexandr7035.banking.domain.features.profile.model.ProfileTier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class ProfileRepositoryMock(
    private val dispatcher: CoroutineDispatcher
) : ProfileRepository {

    private var cachedProfile: CompactProfile = CompactProfile(
        id = "089621027821",
        firstName = "Alexander",
        lastName = "Michael",
        nickName = "@alexandermichael",
        email = "test@example.com",
        profilePicUrl = "https://api.dicebear.com/7.x/open-peeps/svg?seed=Bailey",
        tier = ProfileTier.BASIC,
    )

    override suspend fun getCompactProfile(): CompactProfile = withContext(dispatcher) {
        delay(MOCK_DELAY)
        return@withContext cachedProfile
    }

    override suspend fun updateProfile(profile: CompactProfile) = withContext(dispatcher) {
        delay(MOCK_DELAY)
        cachedProfile = profile
    }

    companion object {
        private const val MOCK_DELAY = 300L
    }
}