package by.alexandr7035.banking.data.transactions

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import by.alexandr7035.banking.data.DbTestUtils
import by.alexandr7035.banking.data.db.CacheDatabase
import by.alexandr7035.banking.domain.features.account.model.MoneyAmount
import by.alexandr7035.banking.domain.features.contacts.ContactsRepository
import by.alexandr7035.banking.domain.features.transactions.model.TransactionRowPayload
import by.alexandr7035.banking.domain.features.transactions.model.TransactionStatus
import by.alexandr7035.banking.domain.features.transactions.model.TransactionType
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransactionRepositoryMockTest {
    private lateinit var db: CacheDatabase
    private lateinit var workManager: WorkManager
    private lateinit var repository: TransactionRepositoryMock

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        workManager = WorkManager.getInstance(context)

        db = DbTestUtils.inMemoryDb()
        repository = TransactionRepositoryMock(
            workManager = workManager,
            transactionDao = db.getTransactionsDao(),
            coroutineDispatcher = Dispatchers.IO,
            contactsRepository = mockk<ContactsRepository>()
        )
    }

    @After
    fun tearDown() {
        workManager.cancelAllWork()
        db.close()
    }

    @Test
    fun submitTransactionInsertsPendingEntityAndEnqueuesWorker() = runTest {
        repository.submitTransaction(
            TransactionRowPayload(
                type = TransactionType.SEND,
                amount = MoneyAmount(75f),
                cardId = "4111111111111111",
                contactId = 5L
            )
        )

        val stored = db.getTransactionsDao().getTransactionList(0, 10).single()
        assertEquals(TransactionStatus.PENDING, stored.recentStatus)
        assertEquals(TransactionType.SEND, stored.type)
        assertEquals(MoneyAmount(75f), stored.value)
        assertEquals("4111111111111111", stored.cardId)
        assertEquals(5L, stored.linkedContactId)

        val workInfos = workManager.getWorkInfos(
            androidx.work.WorkQuery.fromStates(WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING, WorkInfo.State.BLOCKED)
        ).get()
        assertEquals(1, workInfos.size)
        assertTrue(workInfos.single().tags.contains(TransactionWorker::class.java.name))
    }

    @Test
    fun getTransactionStatusFlowEmitsCurrentStatus() = runBlocking {
        val id = db.getTransactionsDao().addTransaction(
            DbTestUtils.transactionEntity(status = TransactionStatus.COMPLETED)
        )

        val first = kotlinx.coroutines.withTimeout(5_000L) {
            repository.getTransactionStatusFlow(id).first()
        }
        assertEquals(TransactionStatus.COMPLETED, first)
    }
}
