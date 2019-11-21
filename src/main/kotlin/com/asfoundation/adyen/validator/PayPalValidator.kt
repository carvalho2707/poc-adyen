package com.asfoundation.adyen.validator

import org.springframework.stereotype.Component

@Component
class PayPalValidator {

    fun validatePayPalFields(redirectUrl: String?) {
        if (redirectUrl.isNullOrBlank()) {
            throw IllegalArgumentException("redirectUrl can't be null")
        }
    }

}