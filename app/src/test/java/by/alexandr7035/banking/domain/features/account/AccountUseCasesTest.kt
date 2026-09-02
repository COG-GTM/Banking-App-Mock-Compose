package by.alexandr7035.banking.domain.features.account

import by.alexandr7035.banking.domain.features.account.account_topup.GetSuggestedTopUpValuesUseCase
import by.alexandr7035.banking.domain.features.account.account_topup.TopUpAccountUseCase
import by.alexandr7035.banking.domain.features.account.model.BalanceCurrency
import by.alexandr7035.banking.domain.features.account.model.MoneyAmount
import by.alexandr7035.banking.domain.features.account.send_money.GetSuggestedSendValuesForCardBalance
import by.alexandr7035.banking.domain.features.account.send_money.SendMoneyUseCase
import by.alexandr7035.banking.domain.features.transactions.TransactionRepository
import by.alexandr7035.banking.domain.features.transactions.model.TransactionRowPayload
import by.alexandr7035.banking.domain.features.transactions.model.TransactionType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetSuggestedTopUpValuesUseCaseTest {
    @Test
    fun `returns deterministic set of USD values`() {
        val expected = listOf(0.25f, 1f, 50f, 150f, 250f, 350f, 450f, 550f, 1000f)
            .map { MoneyAmount(it, BalanceCurrency.USD) }
            .toSet()

        assertEquals(expected, GetSuggestedTopUpValuesUseCase().execute())
    }
}

class GetSuggestedSendValuesForCardBalanceTest {
    private val useCase = GetSuggestedSendValuesForCardBalance()

    @Test
    fun `zero balance yields no suggestions`() {
        assertEquals(emptySet<MoneyAmount>(), useCase.execute(MoneyAmount(0f)))
    }

    @Test
    fun `sub unit balance yields min, half and full balance`() {
        assertEquals(
            setOf(MoneyAmount(0.01f), MoneyAmount(0.4f), MoneyAmount(0.8f)),
            useCase.execute(MoneyAmount(0.8f))
        )
    }

    @Test
    fun `balance up to 100 yields steps of 5 plus 1 and balance`() {
        assertEquals(
            setOf(1f, 5f, 10f, 15f, 20f, 23f).map { MoneyAmount(it) }.toSet(),
            useCase.execute(MoneyAmount(23f))
        )
    }

    @Test
    fun `balance up to 10000 yields at most 7 steps of 50 plus 1 and balance`() {
        val result = useCase.execute(MoneyAmount(1000f))

        assertEquals(
            setOf(1f, 50f, 100f, 150f, 200f, 250f, 300f, 350f, 1000f).map { MoneyAmount(it) }.toSet(),
            result
        )
    }

    @Test
    fun `large balance never suggests amounts above balance`() {
        val balance = MoneyAmount(50_000f)
        val result = useCase.execute(balance)

        assertTrue(result.contains(MoneyAmount(1f)))
        assertTrue(result.all { it.value <= balance.value })
        assertEquals(9, result.size)
    }
}

class SendMoneyUseCaseTest {
    private val repository: TransactionRepository = mockk(relaxed = true)
    private val useCase = SendMoneyUseCase(repository)

    @Test
    fun `forwards SEND payload with card and contact`() = runTest {
        val payload = slot<TransactionRowPayload>()
        coEvery { repository.submitTransaction(capture(payload)) } returns Unit

        useCase.execute(amount = MoneyAmount(25f), fromCardId = "card-1", contactId = 7L)

        assertEquals(
            TransactionRowPayload(
                type = TransactionType.SEND,
                amount = MoneyAmount(25f),
                cardId = "card-1",
                contactId = 7L
            ),
            payload.captured
        )
        coVerify(exactly = 1) { repository.submitTransaction(any()) }
    }
}

class TopUpAccountUseCaseTest {
    private val repository: TransactionRepository = mockk(relaxed = true)
    private val useCase = TopUpAccountUseCase(repository)

    @Test
    fun `forwards TOP_UP payload without contact`() = runTest {
        val payload = slot<TransactionRowPayload>()
        coEvery { repository.submitTransaction(capture(payload)) } returns Unit

        useCase.execute(cardId = "card-2", amount = MoneyAmount(150f))

        assertEquals(
            TransactionRowPayload(
                type = TransactionType.TOP_UP,
                amount = MoneyAmount(150f),
                cardId = "card-2",
                contactId = null
            ),
            payload.captured
        )
        coVerify(exactly = 1) { repository.submitTransaction(any()) }
    }
}
