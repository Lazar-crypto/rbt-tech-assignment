package com.razal.rbtticketbooking.payment.service.impl

import com.razal.rbtticketbooking.payment.service.PaymentClient
import com.razal.rbtticketbooking.shared.config.AppProperties
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class WireMockPaymentClient(
    private val restTemplate: RestTemplate,
    private val appProps: AppProperties
): PaymentClient {

    override fun isPaymentSuccessful(paymentRef: String): Boolean {
        val uri =
            if (paymentRef == "will-fail") "${appProps.paymentApi.baseUrl}/v1/payment_intents/will-fail"
            else "${appProps.paymentApi.baseUrl}/v1/payment_intents/will-succeed"
        val resp = restTemplate.getForEntity(uri, Map::class.java, paymentRef)
        val status = (resp.body?.get("status") as? String)?.lowercase()
        return status == "succeeded"
    }
}