package com.razal.rbtticketbooking.security.functional

import com.nimbusds.jwt.JWTClaimsSet
import com.razal.rbtticketbooking.security.jwt.JwtAuthFilter
import com.razal.rbtticketbooking.security.jwt.JwtService
import junit.framework.TestCase.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import java.time.Instant
import java.util.Date
import kotlin.test.Test
import kotlin.test.assertEquals

class JwtAuthFilterTest {
    private val jwt = mock<JwtService>()
    private val filter = JwtAuthFilter(jwt)

    @BeforeEach
    fun clearCtx() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `valid token sets authentication`() {
        val claims = JWTClaimsSet.Builder()
            .subject("emilys")
            .claim("roles", listOf("USER"))
            .issuer("rbt-ticket-booking")
            .expirationTime(Date.from(Instant.now().plusSeconds(3600)))
            .build()

        whenever(jwt.claimsIfValid("good")).thenReturn(claims)

        val req = MockHttpServletRequest().apply {
            addHeader(HttpHeaders.AUTHORIZATION, "Bearer good")
        }
        val res = MockHttpServletResponse()
        val chain = MockFilterChain()

        filter.doFilter(req, res, chain)

        val auth = SecurityContextHolder.getContext().authentication
        assertNotNull(auth)
        assertEquals("emilys", auth.name)
        assertTrue(auth.authorities.map { it.authority }.contains("ROLE_USER"))
    }

    @Test
    fun `invalid token leaves context unauthenticated`() {
        whenever(jwt.claimsIfValid("bad")).thenReturn(null)

        val req = MockHttpServletRequest().apply {
            addHeader(HttpHeaders.AUTHORIZATION, "Bearer bad")
        }
        val res = MockHttpServletResponse()
        val chain = MockFilterChain()

        filter.doFilter(req, res, chain)

        assertNull(SecurityContextHolder.getContext().authentication)
    }
}