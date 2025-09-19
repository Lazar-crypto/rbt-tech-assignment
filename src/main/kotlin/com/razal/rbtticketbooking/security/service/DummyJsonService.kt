package com.razal.rbtticketbooking.security.service

import com.razal.rbtticketbooking.security.dto.DummyMeResponse

interface DummyJsonService {

    fun login(username: String, password: String): String

    fun me(accessToken: String): DummyMeResponse
}