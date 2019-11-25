package com.asfoundation.adyen.service

import com.adyen.Client
import com.adyen.enums.Environment
import com.adyen.model.Amount
import com.adyen.model.checkout.*
import com.adyen.service.Checkout
import com.asfoundation.adyen.config.AppProperties
import com.asfoundation.adyen.model.PaymentMethodType
import com.asfoundation.adyen.model.PaymentResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.util.MultiValueMap
import java.math.BigDecimal


@Service
class AdyenService {

    @Autowired
    lateinit var appProperties: AppProperties

    fun getPaymentMethods(value: BigDecimal, currency: String, walletAddress: String?): PaymentMethodsResponse {
        val client = Client(appProperties.apiKey, Environment.TEST)
        val checkout = Checkout(client)

        val paymentMethodsRequest = PaymentMethodsRequest()
        paymentMethodsRequest.merchantAccount = appProperties.apiMerchant
        paymentMethodsRequest.countryCode = "PT"
        paymentMethodsRequest.channel = PaymentMethodsRequest.ChannelEnum.ANDROID

        walletAddress?.let {
            paymentMethodsRequest.shopperReference = it
        }

        val amount = Amount()
        amount.currency = currency
        amount.value = value.multiply(100.toBigDecimal()).toLong()
        paymentMethodsRequest.amount = amount

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
            walletAddress: String?,
            token: String?
    ): PaymentResult {
        return when (paymentMethodType) {
            PaymentMethodType.CREDIT_CARD -> createCreditCardPayment(value, currency, encryptedCardNumber, encryptedExpiryMonth, encryptedExpiryYear, encryptedSecurityCode, reference, walletAddress, token)
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
            walletAddress: String?,
            recurringDetailReference: String?
    ): PaymentResult {
        val client = Client(appProperties.apiKey, Environment.TEST)
        val checkout = Checkout(client)

        val paymentsRequest = PaymentsRequest()
        paymentsRequest.merchantAccount = appProperties.apiMerchant
        paymentsRequest.reference = reference
        paymentsRequest.addEncryptedCardData(
                encryptedCardNumber,
                encryptedExpiryMonth,
                encryptedExpiryYear,
                encryptedSecurityCode,
                HOLDER_NAME,
                walletAddress != null
        )

        paymentsRequest.shopperReference = walletAddress

        if (walletAddress != null && recurringDetailReference != null) {
            paymentsRequest.shopperInteraction = PaymentsRequest.ShopperInteractionEnum.CONTAUTH
            paymentsRequest.recurringProcessingModel = PaymentsRequest.RecurringProcessingModelEnum.CARD_ON_FILE
            (paymentsRequest.paymentMethod as DefaultPaymentMethodDetails).recurringDetailReference = recurringDetailReference
        }

        val amount = Amount()
        amount.currency = currency
        amount.value = value.multiply(100.toBigDecimal()).toLong()
        paymentsRequest.amount = amount

        val paymentsResponse = checkout.payments(paymentsRequest)
        return createCreditCardResponse(paymentsResponse)
    }

    private fun createCreditCardResponse(paymentsResponse: PaymentsResponse): PaymentResult {
        return PaymentResult(
                resultCode = paymentsResponse.resultCode.name,
                refusalReason = paymentsResponse.refusalReason,
                refusalReasonCode = paymentsResponse.refusalReasonCode,
                p2pReference = paymentsResponse.pspReference
        )
    }

    private fun createPayPalResponse(paymentsResponse: PaymentsResponse): PaymentResult {
        return PaymentResult(
                resultCode = paymentsResponse.resultCode.name,
                refusalReason = paymentsResponse.refusalReason,
                refusalReasonCode = paymentsResponse.refusalReasonCode,
                action = paymentsResponse.action,
                p2pReference = paymentsResponse.pspReference
        )
    }

    private fun createPayPalPayment(value: BigDecimal, currency: String, reference: String, redirectUrl: String?): PaymentResult {
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

    fun updatePayment(body: MultiValueMap<String, String>): PaymentResult {
        val client = Client(appProperties.apiKey, Environment.TEST)
        val checkout = Checkout(client)

        val details = HashMap<String, String>()
        details["payload"] = body.getFirst("payload") as String

        val paymentsDetailsRequest = PaymentsDetailsRequest()
        paymentsDetailsRequest.details = details
        paymentsDetailsRequest.paymentData = body.getFirst("payment_data")

        val paymentsResponse = checkout.paymentsDetails(paymentsDetailsRequest)
        return createPayPalResponse(paymentsResponse)
    }

    companion object {
        const val HOLDER_NAME = "Checkout Shopper Placeholder"
    }

}