package com.razal.rbtticketbooking.security.functional

import com.razal.rbtticketbooking.security.dto.DummyMeResponse
import com.razal.rbtticketbooking.security.jwt.JwtService
import com.razal.rbtticketbooking.security.service.DummyJsonService
import com.razal.rbtticketbooking.security.service.RoleService
import com.razal.rbtticketbooking.security.service.impl.AuthServiceImpl
import com.razal.rbtticketbooking.shared.config.AppProperties
import junit.framework.TestCase.assertTrue
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthServiceImplTest {

    private val dummy = mock<DummyJsonService>()
    private val roles = mock<RoleService>()
    private val jwt = mock<JwtService>()

    private val props = AppProperties(
        jwt = AppProperties.Jwt(
            secret = "x".repeat(64),
            issuer = "rbt-ticket-booking",
            expirationMin = 60
        ),
        dummyJson = AppProperties.DummyJson(baseUrl = "https://dummyjson.com"),
        adminUsers = listOf("emilys")
    )
    private val service = AuthServiceImpl(props, dummy, jwt, roles)

    @Test
    fun `login calls dummy, assigns roles, issues our jwt`() {
        whenever(dummy.login("emilys", "pass")).thenReturn("ext-token")
        whenever(dummy.me("ext-token")).thenReturn(DummyMeResponse(id = 1, username = "emilys"))
        whenever(roles.rolesFor("emilys")).thenReturn(listOf("ADMIN", "USER"))
        whenever(jwt.issue("emilys", listOf("ADMIN","USER"))).thenReturn("our-jwt")

        val res = service.login("emilys", "pass")

        assertEquals("our-jwt", res.token)
        assertTrue(res.expiresAt!!.isAfter(Instant.now()))
        verify(dummy).login("emilys", "pass")
        verify(dummy).me("ext-token")
        verify(roles).rolesFor("emilys")
        verify(jwt).issue("emilys", listOf("ADMIN","USER"))
    }
}