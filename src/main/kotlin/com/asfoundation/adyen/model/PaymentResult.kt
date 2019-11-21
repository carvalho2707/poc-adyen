package com.asfoundation.adyen.model

data class PaymentResult(
        val resultCode: String,
        val refusalReason: String? = null,
        val refusalReasonCode: String? = null,
        val action: PaymentAction? = null
)