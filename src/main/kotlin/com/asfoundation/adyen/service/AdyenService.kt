package com.asfoundation.adyen.service

import com.adyen.Client
import com.adyen.enums.Environment
import com.adyen.model.Amount
import com.adyen.model.checkout.*
import com.adyen.service.Checkout
import com.asfoundation.adyen.config.AppProperties
import com.asfoundation.adyen.model.PayPalData
import com.asfoundation.adyen.model.PaymentMethodType
import com.asfoundation.adyen.model.PaymentResult
import com.asfoundation.adyen.validator.CreditCardValidator
import com.asfoundation.adyen.validator.PayPalValidator
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal


@Service
class AdyenService {

    @Autowired
    lateinit var appProperties: AppProperties

    @Autowired
    lateinit var cardValidator: CreditCardValidator

    @Autowired
    lateinit var payPalValidator: PayPalValidator

    val objectMapper = ObjectMapper()

    fun getPaymentMethods(value: BigDecimal, currency: String): PaymentMethodsResponse {
        val client = Client(appProperties.apiKey, Environment.TEST)

        val checkout = Checkout(client)
        val paymentMethodsRequest = PaymentMethodsRequest()
        paymentMethodsRequest.merchantAccount = appProperties.apiMerchant
        paymentMethodsRequest.countryCode = "PT"
        val amount = Amount()
        amount.currency = currency
        amount.value = value.multiply(100.toBigDecimal()).toLong()
        paymentMethodsRequest.amount = amount
        paymentMethodsRequest.channel = PaymentMethodsRequest.ChannelEnum.ANDROID
        return checkout.paymentMethods(paymentMethodsRequest)
    }

    fun makePayment(
            value: BigDecimal,
            currency: String,
            encryptedCardNumber: String?,
            encryptedExpiryMonth: String?,
            encryptedExpiryYear: String?,
            encryptedSecurityCode: String?,
            paymentMethodType: PaymentMethodType,
            reference: String,
            redirectUrl: String?,
            storeDetails: Boolean?
    ): PaymentResult {
        return when (paymentMethodType) {
            PaymentMethodType.CREDIT_CARD -> createCreditCardPayment(value, currency, encryptedCardNumber, encryptedExpiryMonth, encryptedExpiryYear, encryptedSecurityCode, reference, storeDetails)
            PaymentMethodType.PAYPAL -> createPayPalPayment(value, currency, reference, redirectUrl)
        }
    }

    private fun createCreditCardPayment(
            value: BigDecimal,
            currency: String,
            encryptedCardNumber: String?,
            encryptedExpiryMonth: String?,
            encryptedExpiryYear: String?,
            encryptedSecurityCode: String?,
            reference: String,
            storeDetails: Boolean?
    ): PaymentResult {
        cardValidator.validateCreditCardFields(encryptedCardNumber, encryptedExpiryMonth, encryptedExpiryYear, encryptedSecurityCode)

        val client = Client(appProperties.apiKey, Environment.TEST)
        val checkout = Checkout(client)

        val paymentsRequest = PaymentsRequest()
        paymentsRequest.merchantAccount = appProperties.apiMerchant

        val amount = Amount()
        amount.currency = currency
        amount.value = value.multiply(100.toBigDecimal()).toLong()

        paymentsRequest.amount = amount
        paymentsRequest.reference = reference
        paymentsRequest.addEncryptedCardData(encryptedCardNumber, encryptedExpiryMonth, encryptedExpiryYear, encryptedSecurityCode, HOLDER_NAME, storeDetails
                ?: false)

        val paymentsResponse = checkout.payments(paymentsRequest)
        return createCreditCardResponse(paymentsResponse)
    }

    private fun createCreditCardResponse(paymentsResponse: PaymentsResponse): PaymentResult {
        return PaymentResult(paymentsResponse.resultCode.name, paymentsResponse.refusalReason, paymentsResponse.refusalReasonCode)
    }

    private fun createPayPalResponse(paymentsResponse: PaymentsResponse): PaymentResult {
        return PaymentResult(paymentsResponse.resultCode.name, paymentsResponse.refusalReason, paymentsResponse.refusalReasonCode, paymentsResponse.action)
    }

    private fun createPayPalPayment(value: BigDecimal, currency: String, reference: String, redirectUrl: String?): PaymentResult {
        payPalValidator.validatePayPalFields(redirectUrl)

        val client = Client(appProperties.apiKey, Environment.TEST)
        val checkout = Checkout(client)

        val paymentsRequest = PaymentsRequest()
        paymentsRequest.merchantAccount = appProperties.apiMerchant

        val amount = Amount()
        amount.currency = currency
        amount.value = value.multiply(100.toBigDecimal()).toLong()

        paymentsRequest.amount = amount
        paymentsRequest.reference = reference

        val paymentMethodDetails = DefaultPaymentMethodDetails()
        paymentMethodDetails.type = "paypal"
        paymentsRequest.paymentMethod = paymentMethodDetails

        paymentsRequest.returnUrl = redirectUrl
        val paymentsResponse = checkout.payments(paymentsRequest)
        return createPayPalResponse(paymentsResponse)
    }

    fun updatePayment(payload: String, paymentData: String): PaymentResult {
        val client = Client(appProperties.apiKey, Environment.TEST)
        val checkout = Checkout(client)

        val payPalResponse = objectMapper.readValue(payload, PayPalData::class.java)

        val details = HashMap<String, String>()
        details["payload"] = payPalResponse.payload

        val paymentsDetailsRequest = PaymentsDetailsRequest()
        paymentsDetailsRequest.details = details
        paymentsDetailsRequest.paymentData = paymentData

        val paymentsResponse = checkout.paymentsDetails(paymentsDetailsRequest)
        return createPayPalResponse(paymentsResponse)
    }

    companion object {
        const val HOLDER_NAME = "Checkout Shopper Placeholder"
    }

}