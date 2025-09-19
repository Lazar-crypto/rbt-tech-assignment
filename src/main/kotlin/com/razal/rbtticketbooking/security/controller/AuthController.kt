package com.razal.rbtticketbooking.security.controller

import com.razal.rbtticketbooking.security.dto.LoginRequest
import com.razal.rbtticketbooking.security.service.AuthService
import com.razal.rbtticketbooking.shared.dto.ResponseDto
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/auth"], produces = [MediaType.APPLICATION_JSON_VALUE])
class AuthController(private val authService: AuthService) {

    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest) = ResponseEntity.ok(
        ResponseDto(
            data = authService.login(req.username, req.password),
            msg = "Login successful"
            )
        )
}