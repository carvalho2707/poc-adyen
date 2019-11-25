package com.asfoundation.adyen.model

import com.adyen.model.checkout.CheckoutPaymentsAction

data class PaymentResult(
        val resultCode: String,
        val refusalReason: String? = null,
        val refusalReasonCode: String? = null,
        val action: CheckoutPaymentsAction? = null,
        val token: String? = null,
        val p2pReference: String? = null
)