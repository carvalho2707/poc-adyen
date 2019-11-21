package com.asfoundation.adyen.controller

import com.adyen.model.checkout.PaymentMethodsResponse
import com.asfoundation.adyen.model.PaymentMethodType
import com.asfoundation.adyen.model.PaymentResult
import com.asfoundation.adyen.service.AdyenService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
class AdyenController {

    @Autowired
    lateinit var adyenService: AdyenService

    @GetMapping("methods")
    fun getPaymentMethods(@RequestParam("value") value: BigDecimal, @RequestParam("currency") currency: String): PaymentMethodsResponse {
        return adyenService.getPaymentMethods(value, currency)
    }

    @PostMapping("payment")
    fun makePayment(
            @RequestParam(value = "value", required = true) value: BigDecimal,
            @RequestParam(value = "currency", required = true) currency: String,
            @RequestParam(value = "encrypted_card_number") encryptedCardNumber: String?,
            @RequestParam(value = "encrypted_expiry_month") encryptedExpiryMonth: String?,
            @RequestParam(value = "encrypted_expiry_year") encryptedExpiryYear: String?,
            @RequestParam(value = "encrypted_security_code") encryptedSecurityCode: String?,
            @RequestParam(value = "reference", required = true) reference: String,
            @RequestParam(value = "type", required = true) type: PaymentMethodType,
            @RequestParam(value = "redirect_url") redirectUrl: String?,
            @RequestParam(value = "store_details") storeDetails: Boolean?
    ): PaymentResult {
        return adyenService.makePayment(
                value,
                currency,
                encryptedCardNumber,
                encryptedExpiryMonth,
                encryptedExpiryYear,
                encryptedSecurityCode,
                type,
                reference,
                redirectUrl,
                storeDetails
        )
    }

    @PostMapping("payment/details")
    fun updatePayment(
            @RequestParam(value = "payload", required = true) payload: String,
            @RequestParam(value = "payment_data", required = true) paymentData: String
    ): PaymentResult {
        return adyenService.updatePayment(payload, paymentData)
    }

}