package com.asfoundation.adyen.validator

import org.springframework.stereotype.Component

@Component
class CreditCardValidator {

    fun validateCreditCardFields(
            encryptedCardNumber: String?,
            encryptedExpiryMonth: String?,
            encryptedExpiryYear: String?,
            encryptedSecurityCode: String?
    ) {
        if (encryptedCardNumber.isNullOrBlank()) {
            throw IllegalArgumentException("encryptedCardNumber can't be null")
        }
        if (encryptedExpiryMonth.isNullOrBlank()) {
            throw IllegalArgumentException("encryptedExpiryMonth can't be null")
        }
        if (encryptedExpiryYear.isNullOrBlank()) {
            throw IllegalArgumentException("encryptedExpiryYear can't be null")
        }
        if (encryptedSecurityCode.isNullOrBlank()) {
            throw IllegalArgumentException("encryptedSecurityCode can't be null")
        }
    }

}