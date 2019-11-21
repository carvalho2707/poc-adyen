package com.asfoundation.adyen

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AdyenApplication

fun main(args: Array<String>) {
    runApplication<AdyenApplication>(*args)
}
