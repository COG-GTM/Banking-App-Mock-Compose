package by.alexandr7035.banking.domain.features.cards

import by.alexandr7035.banking.domain.features.cards.model.PaymentCard
import by.alexandr7035.banking.testutils.TestData.paymentCard
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetHomeCardsUseCaseTest {
    private val repository: CardsRepository = mockk()
    private val useCase = GetHomeCardsUseCase(repository)

    @Test
    fun `primary cards come first, each partition sorted by addedDate descending`() = runTest {
        val oldPrimary = paymentCard(cardId = "p-old", isPrimary = true, addedDate = 10)
        val newPrimary = paymentCard(cardId = "p-new", isPrimary = true, addedDate = 20)
        val oldOther = paymentCard(cardId = "o-old", isPrimary = false, addedDate = 5)
        val newOther = paymentCard(cardId = "o-new", isPrimary = false, addedDate = 50)
        coEvery { repository.getCards() } returns listOf(oldOther, oldPrimary, newOther, newPrimary)

        val result = useCase.execute()

        assertEquals(listOf(newPrimary, oldPrimary, newOther), result)
    }

    @Test
    fun `returns at most three cards`() = runTest {
        coEvery { repository.getCards() } returns (1..6).map {
            paymentCard(cardId = "c$it", addedDate = it.toLong())
        }

        val result = useCase.execute()

        assertEquals(3, result.size)
        assertEquals(listOf("c6", "c5", "c4"), result.map { it.cardId })
    }

    @Test
    fun `returns empty list when no cards`() = runTest {
        coEvery { repository.getCards() } returns emptyList()

        assertEquals(emptyList<PaymentCard>(), useCase.execute())
    }
}

class GetDefaultCardUseCaseTest {
    private val repository: CardsRepository = mockk()
    private val useCase = GetDefaultCardUseCase(repository)

    @Test
    fun `primary card is preferred over first card`() = runTest {
        val first = paymentCard(cardId = "first")
        val primary = paymentCard(cardId = "primary", isPrimary = true)
        coEvery { repository.getCards() } returns listOf(first, primary)

        assertEquals(primary, useCase.execute())
    }

    @Test
    fun `falls back to first card when no primary`() = runTest {
        val first = paymentCard(cardId = "first")
        coEvery { repository.getCards() } returns listOf(first, paymentCard(cardId = "second"))

        assertEquals(first, useCase.execute())
    }

    @Test
    fun `returns null when no cards`() = runTest {
        coEvery { repository.getCards() } returns emptyList()

        assertNull(useCase.execute())
    }
}
