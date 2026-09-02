package by.alexandr7035.banking.domain.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class OperationResultTest {

    @Test
    fun `runWrapped wraps returned value into Success`() {
        val result = OperationResult.runWrapped { 42 }

        assertEquals(OperationResult.Success(42), result)
        assertTrue(result.isSuccess())
    }

    @Test
    fun `runWrapped maps AppError into Failure preserving error type`() {
        val result = OperationResult.runWrapped<Unit> {
            throw AppError(ErrorType.CARD_NOT_FOUND)
        }

        assertTrue(result is OperationResult.Failure)
        assertEquals(ErrorType.CARD_NOT_FOUND, (result as OperationResult.Failure).error.errorType)
        assertFalse(result.isSuccess())
    }

    @Test
    fun `runWrapped maps unknown exception into Failure with UNKNOWN_ERROR`() {
        val result = OperationResult.runWrapped<Unit> {
            throw IOException("boom")
        }

        assertEquals(OperationResult.Failure(AppError(ErrorType.UNKNOWN_ERROR)), result)
    }

    @Test
    fun `ErrorType fromThrowable maps AppError and generic throwables`() {
        assertEquals(ErrorType.WRONG_PASSWORD, ErrorType.fromThrowable(AppError(ErrorType.WRONG_PASSWORD)))
        assertEquals(ErrorType.UNKNOWN_ERROR, ErrorType.fromThrowable(IllegalStateException()))
    }
}
