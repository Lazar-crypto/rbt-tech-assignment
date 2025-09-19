package com.razal.rbtticketbooking.security.service.impl

import com.razal.rbtticketbooking.security.dto.LoginResponse
import com.razal.rbtticketbooking.security.jwt.JwtService
import com.razal.rbtticketbooking.security.service.AuthService
import com.razal.rbtticketbooking.security.service.DummyJsonService
import com.razal.rbtticketbooking.security.service.RoleService
import com.razal.rbtticketbooking.shared.config.AppProperties
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class AuthServiceImpl(
    private val props: AppProperties,
    private val dummy: DummyJsonService,
    private val jwtService: JwtService,
    private val roleService: RoleService
): AuthService {
    override fun login(username: String, password: String): LoginResponse {
        val extAccess = dummy.login(username, password)
        val me = dummy.me(extAccess)
        val userRoles = roleService.rolesFor(me.username)
        val token = jwtService.issue(me.username, userRoles)
        val expiresAt = Instant.now().plus(props.jwt.expirationMin, ChronoUnit.MINUTES)

        return LoginResponse(token, expiresAt = expiresAt)
    }
}