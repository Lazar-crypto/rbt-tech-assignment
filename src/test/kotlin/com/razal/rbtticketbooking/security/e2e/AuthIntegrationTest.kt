package com.razal.rbtticketbooking.security.e2e

import com.razal.rbtticketbooking.security.controller.AuthController
import com.razal.rbtticketbooking.security.dto.LoginResponse
import com.razal.rbtticketbooking.security.service.AuthService
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.Instant
import kotlin.test.Test
import org.mockito.Mockito.mock

class AuthIntegrationTest {
    
    private lateinit var mockMvc: MockMvc
    private lateinit var authService: AuthService

    @BeforeEach
    fun setup() {
        authService = mock(AuthService::class.java)
        val authController = AuthController(authService)
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build()
    }

    @Test
    fun `login with valid credentials returns token`() {
        val mockToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.test.signature"
        val expiresAt = Instant.now().plusSeconds(3600)
        val loginResponse = LoginResponse(mockToken, expiresAt = expiresAt)
        
        whenever(authService.login("emilys", "pass")).thenReturn(loginResponse)

        mockMvc.post("/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"username":"emilys","password":"pass"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.token") { value(mockToken) }
            jsonPath("$.msg") { value("Login successful") }
        }
    }

    @Test
    fun `login with invalid request format returns 400`() {
        mockMvc.post("/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"invalid": "json"}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }
}
