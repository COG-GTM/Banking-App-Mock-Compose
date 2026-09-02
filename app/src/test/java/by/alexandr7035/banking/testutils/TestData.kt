package by.alexandr7035.banking.testutils

import by.alexandr7035.banking.domain.features.account.model.MoneyAmount
import by.alexandr7035.banking.domain.features.cards.model.CardType
import by.alexandr7035.banking.domain.features.cards.model.PaymentCard
import by.alexandr7035.banking.domain.features.contacts.Contact
import by.alexandr7035.banking.domain.features.savings.model.Saving

object TestData {
    const val VALID_CARD_NUMBER = "4111111111111111"

    fun paymentCard(
        cardId: String = VALID_CARD_NUMBER,
        isPrimary: Boolean = false,
        addedDate: Long = 1_000L,
        balance: Float = 100f,
    ) = PaymentCard(
        cardId = cardId,
        isPrimary = isPrimary,
        cardNumber = cardId,
        cardType = CardType.DEBIT,
        cardHolder = "John Doe",
        expiration = System.currentTimeMillis() + 31_556_926_000L,
        recentBalance = MoneyAmount(balance),
        addressFirstLine = "1 Main St",
        addressSecondLine = "",
        addedDate = addedDate
    )

    fun contact(id: Long = 1L) = Contact(
        id = id,
        name = "Jane Doe",
        profilePic = "",
        linkedCardNumber = "5555555555554444"
    )

    fun saving(id: Long, completed: Float) = Saving(
        id = id,
        title = "Saving $id",
        description = "",
        completedPercentage = completed,
        iconUrl = ""
    )
}
