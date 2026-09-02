package by.alexandr7035.banking.data.cards

import androidx.test.ext.junit.runners.AndroidJUnit4
import by.alexandr7035.banking.data.DbTestUtils
import by.alexandr7035.banking.data.DbTestUtils.cardEntity
import by.alexandr7035.banking.data.db.CacheDatabase
import by.alexandr7035.banking.domain.core.AppError
import by.alexandr7035.banking.domain.core.ErrorType
import by.alexandr7035.banking.domain.features.cards.model.AddCardPayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CardsRepositoryMockTest {
    private lateinit var db: CacheDatabase
    private lateinit var repository: CardsRepositoryMock

    private val payload = AddCardPayload(
        cardNumber = "4111111111111111",
        cardHolder = "John Doe",
        addressFirstLine = "1 Main St",
        addressSecondLine = "",
        cvvCode = "123",
        expirationDate = System.currentTimeMillis() + 60_000L
    )

    @Before
    fun setUp() {
        db = DbTestUtils.inMemoryDb()
        repository = CardsRepositoryMock(db.getCardsDao(), Dispatchers.IO)
    }

    @After
    fun tearDown() = db.close()

    private suspend fun expectAppError(expected: ErrorType, block: suspend () -> Unit) {
        try {
            block()
            fail("Expected AppError($expected)")
        } catch (e: AppError) {
            assertEquals(expected, e.errorType)
        }
    }

    @Test
    fun addCardStoresNonPrimaryCardWithZeroBalance() = runTest {
        repository.addCard(payload)

        val card = repository.getCardById(payload.cardNumber)
        assertEquals(payload.cardNumber, card.cardId)
        assertEquals(payload.cardHolder, card.cardHolder)
        assertEquals(0f, card.recentBalance.value)
        assertFalse(card.isPrimary)
    }

    @Test
    fun addCardTwiceThrowsCardAlreadyAdded() = runTest {
        repository.addCard(payload)

        expectAppError(ErrorType.CARD_ALREADY_ADDED) { repository.addCard(payload) }
        assertEquals(1, repository.getCards().size)
    }

    @Test
    fun getCardByIdThrowsCardNotFoundWhenMissing() = runTest {
        expectAppError(ErrorType.CARD_NOT_FOUND) { repository.getCardById("0000000000000000") }
    }

    @Test
    fun deleteCardByIdRemovesCardAndThrowsWhenMissing() = runTest {
        db.getCardsDao().addCard(cardEntity(number = "1"))

        repository.deleteCardById("1")

        assertNull(db.getCardsDao().getCardByNumber("1"))
        expectAppError(ErrorType.CARD_NOT_FOUND) { repository.deleteCardById("1") }
    }

    @Test
    fun markCardAsPrimaryTogglesFlag() = runTest {
        db.getCardsDao().addCard(cardEntity(number = "1", isPrimary = true))
        db.getCardsDao().addCard(cardEntity(number = "2"))

        repository.markCardAsPrimary("2", isPrimary = true)
        assertEquals(listOf(false, true), repository.getCards().map { it.isPrimary })

        repository.markCardAsPrimary("2", isPrimary = false)
        assertEquals(listOf(false, false), repository.getCards().map { it.isPrimary })
    }
}
