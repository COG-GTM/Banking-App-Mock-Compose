package by.alexandr7035.banking.domain.features.savings

import by.alexandr7035.banking.testutils.TestData.saving
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetHomeSavingsUseCaseTest {
    private val repository: SavingsRepository = mockk()
    private val useCase = GetHomeSavingsUseCase(repository)

    @Test
    fun `incomplete savings sorted by completion desc come before completed ones`() = runTest {
        val done = saving(1, 1f)
        val low = saving(2, 0.2f)
        val high = saving(3, 0.9f)
        coEvery { repository.getSavings() } returns listOf(done, low, high)

        assertEquals(listOf(high, low, done), useCase.execute())
    }

    @Test
    fun `returns at most three savings`() = runTest {
        coEvery { repository.getSavings() } returns (1..5).map { saving(it.toLong(), it / 10f) }

        val result = useCase.execute()

        assertEquals(listOf(5L, 4L, 3L), result.map { it.id })
    }
}
