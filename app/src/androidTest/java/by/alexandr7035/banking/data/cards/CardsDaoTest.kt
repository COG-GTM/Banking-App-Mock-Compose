package by.alexandr7035.banking.data.cards

import androidx.test.ext.junit.runners.AndroidJUnit4
import by.alexandr7035.banking.data.DbTestUtils
import by.alexandr7035.banking.data.DbTestUtils.cardEntity
import by.alexandr7035.banking.data.cards.cache.CardsDao
import by.alexandr7035.banking.data.db.CacheDatabase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CardsDaoTest {
    private lateinit var db: CacheDatabase
    private lateinit var dao: CardsDao

    @Before
    fun setUp() {
        db = DbTestUtils.inMemoryDb()
        dao = db.getCardsDao()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun insertAndQueryByNumber() = runTest {
        val card = cardEntity(number = "4111111111111111")
        dao.addCard(card)

        assertEquals(card, dao.getCardByNumber("4111111111111111"))
        assertNull(dao.getCardByNumber("0000000000000000"))
    }

    @Test
    fun getCardsIsOrderedByNumber() = runTest {
        dao.addCard(cardEntity(number = "5555555555554444"))
        dao.addCard(cardEntity(number = "4111111111111111"))

        assertEquals(listOf("4111111111111111", "5555555555554444"), dao.getCards().map { it.number })
    }

    @Test(expected = android.database.sqlite.SQLiteConstraintException::class)
    fun insertingDuplicateNumberAborts() = runTest {
        dao.addCard(cardEntity(number = "4111111111111111"))
        dao.addCard(cardEntity(number = "4111111111111111", balance = 999f))
    }

    @Test
    fun updateCardPersistsChanges() = runTest {
        val card = cardEntity(balance = 10f)
        dao.addCard(card)

        dao.updateCard(card.copy(recentBalance = 250f, cardHolder = "Jane"))

        val updated = dao.getCardByNumber(card.number)!!
        assertEquals(250f, updated.recentBalance)
        assertEquals("Jane", updated.cardHolder)
    }

    @Test
    fun deleteCardRemovesRow() = runTest {
        val card = cardEntity()
        dao.addCard(card)

        dao.deleteCard(card)

        assertNull(dao.getCardByNumber(card.number))
        assertTrue(dao.getCards().isEmpty())
    }

    @Test
    fun markCardAsPrimaryMakesItTheOnlyPrimary() = runTest {
        dao.addCard(cardEntity(number = "1", isPrimary = true))
        dao.addCard(cardEntity(number = "2", isPrimary = false))

        dao.markCardAsPrimary("2")

        val byNumber = dao.getCards().associate { it.number to it.isPrimary }
        assertEquals(mapOf("1" to false, "2" to true), byNumber)
    }

    @Test
    fun unmarkCardAsPrimaryOnlyAffectsGivenCard() = runTest {
        dao.addCard(cardEntity(number = "1", isPrimary = true))
        dao.addCard(cardEntity(number = "2", isPrimary = true))

        dao.unmarkCardAsPrimary("1")

        val byNumber = dao.getCards().associate { it.number to it.isPrimary }
        assertEquals(mapOf("1" to false, "2" to true), byNumber)
    }
}
