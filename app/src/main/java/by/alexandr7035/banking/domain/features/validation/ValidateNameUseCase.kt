package by.alexandr7035.banking.domain.features.validation

import by.alexandr7035.banking.domain.core.ErrorType
import by.alexandr7035.banking.domain.features.validation.model.ValidationResult

class ValidateNameUseCase {
    fun execute(name: String): ValidationResult {
        return if (name.isBlank()) {
            ValidationResult(isValid = false, validationError = ErrorType.FIELD_IS_EMPTY)
        } else if (name.length < 2) {
            ValidationResult(isValid = false, validationError = ErrorType.INVALID_NAME_FIELD)
        } else {
            ValidationResult(isValid = true, validationError = null)
        }
    }
}
