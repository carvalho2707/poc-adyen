package com.asfoundation.adyen.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class AppProperties {

    @Value("\${adyen.apikey}")
    lateinit var apiKey: String

    @Value("\${adyen.merchant}")
    lateinit var apiMerchant: String

}