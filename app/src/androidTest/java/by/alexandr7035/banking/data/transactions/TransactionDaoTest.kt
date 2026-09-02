package by.alexandr7035.banking.data.transactions

import androidx.test.ext.junit.runners.AndroidJUnit4
import by.alexandr7035.banking.data.DbTestUtils
import by.alexandr7035.banking.data.DbTestUtils.transactionEntity
import by.alexandr7035.banking.data.db.CacheDatabase
import by.alexandr7035.banking.data.transactions.db.TransactionDao
import by.alexandr7035.banking.domain.features.account.model.MoneyAmount
import by.alexandr7035.banking.domain.features.transactions.model.TransactionStatus
import by.alexandr7035.banking.domain.features.transactions.model.TransactionType
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransactionDaoTest {
    private lateinit var db: CacheDatabase
    private lateinit var dao: TransactionDao

    @Before
    fun setUp() {
        db = DbTestUtils.inMemoryDb()
        dao = db.getTransactionsDao()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun insertReturnsGeneratedIdAndRoundTripsMoneyAmount() = runTest {
        val id = dao.addTransaction(transactionEntity(amount = 12.5f))

        assertTrue(id > 0)
        val stored = dao.getTransaction(id)!!
        assertEquals(id, stored.id)
        assertEquals(MoneyAmount(12.5f), stored.value)
        assertEquals(TransactionStatus.PENDING, stored.recentStatus)
    }

    @Test
    fun missingTransactionReturnsNull() = runTest {
        assertNull(dao.getTransaction(42L))
    }

    @Test
    fun listIsNewestFirstAndPaged() = runTest {
        val ids = (1..5).map { dao.addTransaction(transactionEntity(amount = it.toFloat())) }

        val firstPage = dao.getTransactionList(startPosition = 0, loadSize = 2)
        val secondPage = dao.getTransactionList(startPosition = 2, loadSize = 2)

        assertEquals(listOf(ids[4], ids[3]), firstPage.map { it.id })
        assertEquals(listOf(ids[2], ids[1]), secondPage.map { it.id })
    }

    @Test
    fun listFiltersByType() = runTest {
        dao.addTransaction(transactionEntity(type = TransactionType.TOP_UP))
        val sendId = dao.addTransaction(transactionEntity(type = TransactionType.SEND, contactId = 3L))
        dao.addTransaction(transactionEntity(type = TransactionType.TOP_UP))

        val sends = dao.getTransactionList(TransactionType.SEND, startPosition = 0, loadSize = 10)

        assertEquals(listOf(sendId), sends.map { it.id })
        assertEquals(3L, sends.single().linkedContactId)
    }

    @Test
    fun updateChangesStatus() = runTest {
        val id = dao.addTransaction(transactionEntity())
        val stored = dao.getTransaction(id)!!

        dao.updateTransaction(stored.copy(recentStatus = TransactionStatus.COMPLETED, updatedStatusDate = 2_000L))

        val updated = dao.getTransaction(id)!!
        assertEquals(TransactionStatus.COMPLETED, updated.recentStatus)
        assertEquals(2_000L, updated.updatedStatusDate)
    }
}
