package com.asfoundation.adyen.validator

import org.springframework.stereotype.Component
import org.springframework.util.MultiValueMap

@Component
class PayPalValidator {

    fun validatePayPalFields(redirectUrl: String?) {
        if (redirectUrl.isNullOrBlank()) {
            throw IllegalArgumentException("redirectUrl can't be null")
        }
    }

    fun validateUpdatePayment(body: MultiValueMap<String, String>) {
        if (body["payload"]?.size ?: 0 != 1) {
            throw IllegalArgumentException("payload can't be null and can only have 1 value")
        }
        if (body["payment_data"]?.size ?: 0 != 1) {
            throw IllegalArgumentException("payment_data can't be null and can only have 1 value")
        }
    }

}