package com.razal.rbtticketbooking.security.service.impl

import com.razal.rbtticketbooking.security.dto.DummyLoginResponse
import com.razal.rbtticketbooking.security.dto.DummyMeResponse
import com.razal.rbtticketbooking.security.dto.LoginRequest
import com.razal.rbtticketbooking.security.service.DummyJsonService
import com.razal.rbtticketbooking.shared.config.AppProperties
import com.razal.rbtticketbooking.shared.exceptions.DummyJsonException
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class DummyServiceImpl(
    builder: RestClient.Builder,
    props: AppProperties
): DummyJsonService {

    private val client = builder.baseUrl(props.dummyJson.baseUrl).build()

    override fun login(username: String, password: String): String {
        val resp = client.post()
            .uri("/user/login")
            .contentType(MediaType.APPLICATION_JSON)
            .body(LoginRequest(username, password))
            .retrieve()
            .body(DummyLoginResponse::class.java) ?: throw DummyJsonException("Empty login response")
        return resp.accessToken
    }

    override fun me(accessToken: String): DummyMeResponse =
        client.get()
            .uri("/user/me")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            .retrieve()
            .body(DummyMeResponse::class.java) ?: throw DummyJsonException("Empty me response")
}