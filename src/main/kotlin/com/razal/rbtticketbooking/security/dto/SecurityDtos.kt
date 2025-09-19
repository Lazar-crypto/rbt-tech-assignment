package com.razal.rbtticketbooking.security.dto

import java.time.Instant

data class LoginRequest(val username: String, val password: String)
data class LoginResponse(
    val token: String,
    val tokenType: String = "Bearer",
    val expiresAt: Instant? = null,
)

data class DummyLoginResponse(val accessToken: String)
data class DummyMeResponse(val id: Int, val username: String)