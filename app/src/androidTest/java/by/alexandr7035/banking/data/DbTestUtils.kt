package by.alexandr7035.banking.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import by.alexandr7035.banking.data.cards.cache.CardEntity
import by.alexandr7035.banking.data.db.CacheDatabase
import by.alexandr7035.banking.data.db.convertors.MoneyAmountConvertor
import by.alexandr7035.banking.data.transactions.db.TransactionEntity
import by.alexandr7035.banking.domain.features.account.model.MoneyAmount
import by.alexandr7035.banking.domain.features.cards.model.CardType
import by.alexandr7035.banking.domain.features.transactions.model.TransactionStatus
import by.alexandr7035.banking.domain.features.transactions.model.TransactionType

object DbTestUtils {
    fun inMemoryDb(): CacheDatabase = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext<Context>(),
        CacheDatabase::class.java
    ).addTypeConverter(MoneyAmountConvertor()).allowMainThreadQueries().build()

    fun cardEntity(
        number: String = "4111111111111111",
        isPrimary: Boolean = false,
        balance: Float = 100f,
        addedDate: Long = 1_000L,
    ) = CardEntity(
        number = number,
        isPrimary = isPrimary,
        cardType = CardType.DEBIT,
        recentBalance = balance,
        cardHolder = "John Doe",
        expiration = System.currentTimeMillis() + 31_556_926_000L,
        addressFirstLine = "1 Main St",
        addressSecondLine = "",
        addedDate = addedDate
    )

    fun transactionEntity(
        type: TransactionType = TransactionType.TOP_UP,
        amount: Float = 50f,
        cardId: String = "4111111111111111",
        contactId: Long? = null,
        status: TransactionStatus = TransactionStatus.PENDING,
    ) = TransactionEntity(
        type = type,
        value = MoneyAmount(amount),
        recentStatus = status,
        cardId = cardId,
        linkedContactId = contactId,
        createdDate = 1_000L,
        updatedStatusDate = 1_000L
    )
}
