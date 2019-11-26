package com.asfoundation.adyen.controller

import com.adyen.model.checkout.PaymentMethodsResponse
import com.asfoundation.adyen.model.PaymentMethodType
import com.asfoundation.adyen.model.PaymentResult
import com.asfoundation.adyen.service.AdyenService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
class AdyenController {

    @Autowired
    lateinit var adyenService: AdyenService

    @GetMapping("methods")
    fun getPaymentMethods(
            @RequestParam("value") value: BigDecimal,
            @RequestParam("currency") currency: String,
            @RequestParam("wallet.address") walletAddress: String
    ): PaymentMethodsResponse? {
        return adyenService.getPaymentMethods(value, currency, walletAddress)
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
            @RequestParam(value = "wallet.address") walletAddress: String,
            @RequestParam(value = "token") token: String?,
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
                walletAddress,
                token,
                storeDetails
        )
    }

    @PostMapping("payment/details", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun updatePayment(@RequestBody body: MultiValueMap<String, String>): PaymentResult {
        return adyenService.updatePayment(body)
    }

    @PostMapping("payment/disable")
    fun disableStoredPayments(@RequestParam(value = "wallet.address") walletAddress: String) {
        return adyenService.disableStoredPayments(walletAddress)
    }

}