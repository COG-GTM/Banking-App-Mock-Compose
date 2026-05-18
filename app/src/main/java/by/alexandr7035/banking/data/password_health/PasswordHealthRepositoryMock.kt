package by.alexandr7035.banking.data.password_health

import by.alexandr7035.banking.domain.features.password_health.LoginItem
import by.alexandr7035.banking.domain.features.password_health.PasswordHealthRepository
import by.alexandr7035.banking.domain.features.password_health.ReusedPasswordGroup
import kotlinx.coroutines.delay
import java.security.MessageDigest

class PasswordHealthRepositoryMock : PasswordHealthRepository {

    override suspend fun getReusedPasswordGroups(): List<ReusedPasswordGroup> {
        delay(MOCK_DELAY)

        val logins = mockLogins()
        val grouped = logins
            .filter { it.second.isNotEmpty() }
            .groupBy { sha256(it.second) }
            .filter { it.value.size >= 2 }
            .map { (hash, entries) ->
                ReusedPasswordGroup(
                    id = hash,
                    items = entries.map { it.first }.sortedBy { it.name }
                )
            }
            .sortedByDescending { it.items.size }

        return grouped
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    private fun mockLogins(): List<Pair<LoginItem, String>> {
        return listOf(
            LoginItem("1", "Gmail", "user@gmail.com") to "Password123!",
            LoginItem("2", "Yahoo Mail", "user@yahoo.com") to "Password123!",
            LoginItem("3", "Outlook", "user@outlook.com") to "Password123!",
            LoginItem("4", "GitHub", "devuser") to "GitSecure#2024",
            LoginItem("5", "GitLab", "devuser") to "GitSecure#2024",
            LoginItem("6", "Netflix", "user@gmail.com") to "StreamPass99",
            LoginItem("7", "Disney+", "user@gmail.com") to "StreamPass99",
            LoginItem("8", "Amazon", "user@gmail.com") to "ShopSecure1!",
            LoginItem("9", "Twitter", "user_handle") to "Tw1tterP@ss",
            LoginItem("10", "LinkedIn", "user@gmail.com") to "L1nked!nPro",
        )
    }

    companion object {
        private const val MOCK_DELAY = 800L
    }
}
