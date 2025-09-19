package com.razal.rbtticketbooking.security.service

import com.razal.rbtticketbooking.security.dto.LoginResponse

interface AuthService {
    fun login(username: String, password: String): LoginResponse
}