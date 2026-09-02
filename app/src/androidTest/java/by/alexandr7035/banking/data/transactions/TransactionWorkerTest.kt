package by.alexandr7035.banking.data.transactions

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import by.alexandr7035.banking.data.DbTestUtils
import by.alexandr7035.banking.data.DbTestUtils.transactionEntity
import by.alexandr7035.banking.data.db.CacheDatabase
import by.alexandr7035.banking.domain.core.AppError
import by.alexandr7035.banking.domain.core.ErrorType
import by.alexandr7035.banking.domain.features.account.AccountRepository
import by.alexandr7035.banking.domain.features.account.model.MoneyAmount
import by.alexandr7035.banking.domain.features.transactions.model.TransactionStatus
import by.alexandr7035.banking.domain.features.transactions.model.TransactionType
import by.alexandr7035.banking.ui.core.notifications.TransactionNotificationHelper
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.After
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Hand-written fake: MockK-android proxies interfaces via java.lang.reflect.Proxy, which wraps
 *  checked exceptions like [AppError] in UndeclaredThrowableException. */
private class FakeAccountRepository : AccountRepository {
    val sendCalls = mutableListOf<Triple<String, MoneyAmount, Long>>()
    val topUpCalls = mutableListOf<Pair<String, MoneyAmount>>()
    var failWith: Throwable? = null

    override fun getBalanceFlow(): Flow<MoneyAmount> = emptyFlow()
    override suspend fun getCardBalanceFlow(cardId: String): Flow<MoneyAmount> = emptyFlow()

    override suspend fun topUpCard(cardId: String, amount: MoneyAmount) {
        topUpCalls += cardId to amount
        failWith?.let { throw it }
    }

    override suspend fun sendFromCard(cardId: String, amount: MoneyAmount, contactId: Long) {
        sendCalls += Triple(cardId, amount, contactId)
        failWith?.let { throw it }
    }
}

@RunWith(AndroidJUnit4::class)
class TransactionWorkerTest {
    private lateinit var context: Context
    private lateinit var db: CacheDatabase
    private val accountRepository = FakeAccountRepository()
    private val notificationHelper: TransactionNotificationHelper = mockk(relaxed = true)

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = DbTestUtils.inMemoryDb()
    }

    @After
    fun tearDown() = db.close()

    private fun buildWorker(transactionId: Long): TransactionWorker =
        TestListenableWorkerBuilder<TransactionWorker>(
            context = context,
            inputData = workDataOf(TransactionWorker.TRANSACTION_ID_KEY to transactionId)
        ).setWorkerFactory(object : androidx.work.WorkerFactory() {
            override fun createWorker(
                appContext: Context,
                workerClassName: String,
                workerParameters: WorkerParameters
            ): ListenableWorker = TransactionWorker(
                appContext,
                workerParameters,
                db.getTransactionsDao(),
                accountRepository,
                notificationHelper
            )
        }).build()

    @Test
    fun sendTransactionCallsSendFromCardAndCompletes() = runBlocking {
        val id = db.getTransactionsDao().addTransaction(
            transactionEntity(type = TransactionType.SEND, amount = 20f, cardId = "card-1", contactId = 7L)
        )

        val result = buildWorker(id).doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assertEquals(listOf(Triple("card-1", MoneyAmount(20f), 7L)), accountRepository.sendCalls)
        assertEquals(TransactionStatus.COMPLETED, db.getTransactionsDao().getTransaction(id)!!.recentStatus)
        verify(exactly = 1) { notificationHelper.successMessage(TransactionType.SEND, MoneyAmount(20f), "card-1") }
        verify(exactly = 1) { notificationHelper.showNotification(any()) }
    }

    @Test
    fun topUpTransactionCallsTopUpCardAndCompletes() = runBlocking {
        val id = db.getTransactionsDao().addTransaction(
            transactionEntity(type = TransactionType.TOP_UP, amount = 300f, cardId = "card-2")
        )

        val result = buildWorker(id).doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assertEquals(listOf("card-2" to MoneyAmount(300f)), accountRepository.topUpCalls)
        assertTrue(accountRepository.sendCalls.isEmpty())
        assertEquals(TransactionStatus.COMPLETED, db.getTransactionsDao().getTransaction(id)!!.recentStatus)
    }

    @Test
    fun failingOperationMarksTransactionFailedAndShowsError() = runBlocking {
        val error = AppError(ErrorType.INSUFFICIENT_CARD_BALANCE)
        accountRepository.failWith = error
        val id = db.getTransactionsDao().addTransaction(
            transactionEntity(type = TransactionType.SEND, cardId = "card-1", contactId = 1L)
        )

        val result = buildWorker(id).doWork()

        assertEquals(ListenableWorker.Result.failure(), result)
        assertEquals(TransactionStatus.FAILED, db.getTransactionsDao().getTransaction(id)!!.recentStatus)
        verify(exactly = 1) { notificationHelper.errorMessage(error) }
        verify(exactly = 0) { notificationHelper.successMessage(any(), any(), any()) }
    }

    @Test
    fun missingTransactionFailsWithoutTouchingAccount() = runBlocking {
        val result = buildWorker(999L).doWork()

        assertEquals(ListenableWorker.Result.failure(), result)
        assertTrue(accountRepository.sendCalls.isEmpty())
        assertTrue(accountRepository.topUpCalls.isEmpty())
        verify(exactly = 0) { notificationHelper.showNotification(any()) }
    }

    @Test
    fun missingInputIdFailsImmediately() = runBlocking {
        val worker = TestListenableWorkerBuilder<TransactionWorker>(context)
            .setWorkerFactory(object : androidx.work.WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker = TransactionWorker(
                    appContext, workerParameters, db.getTransactionsDao(), accountRepository, notificationHelper
                )
            }).build()

        assertEquals(ListenableWorker.Result.failure(), worker.doWork())
    }
}
