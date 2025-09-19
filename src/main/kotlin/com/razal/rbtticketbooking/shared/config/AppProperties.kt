package com.razal.rbtticketbooking.shared.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class AppProperties(
    val jwt: Jwt = Jwt(),
    val dummyJson: DummyJson,
    val adminUsers: List<String> = emptyList()
) {
    data class Jwt(
        val secret: String = "",
        val issuer: String = "",
        val expirationMin: Long = 0,
    )
    data class DummyJson(
        val baseUrl: String = ""
    )
}