package com.razal.rbtticketbooking.security.jwt

import RBT_LOGGER
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(private val jwt: JwtService) : OncePerRequestFilter() {

    override fun doFilterInternal(req: HttpServletRequest, res: HttpServletResponse, chain: FilterChain) {
        val header = req.getHeader(HttpHeaders.AUTHORIZATION)
        if (header?.startsWith("Bearer ") == true) {
            val raw = header.substring(7)

            val claims = jwt.claimsIfValid(raw)
            if (claims != null) {
                val username = claims.subject
                val roles = try {
                    claims.getStringListClaim("roles") ?: emptyList()
                } catch (_: Exception) {
                    emptyList<String>()
                }
                val authorities = roles.map { SimpleGrantedAuthority("ROLE_$it") }
                val auth = UsernamePasswordAuthenticationToken(username, null, authorities)
                SecurityContextHolder.getContext().authentication = auth
                RBT_LOGGER.debug("authenticated user {} with authorities {}", username, authorities)
            }
        }
        chain.doFilter(req, res)
    }
}
