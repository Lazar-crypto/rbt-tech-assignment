package com.razal.rbtticketbooking.event

import com.fasterxml.jackson.databind.ObjectMapper
import com.razal.rbtticketbooking.event.dto.CreateEventRequest
import com.razal.rbtticketbooking.event.repository.PerformerRepo
import com.razal.rbtticketbooking.event.repository.VenueRepo
import com.razal.rbtticketbooking.security.jwt.JwtService
import com.razal.rbtticketbooking.util.BaseIntegrationTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional


import java.math.BigDecimal
import java.time.OffsetDateTime
import kotlin.test.Test

@AutoConfigureMockMvc
@Transactional
class EventE2ETest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val jwtService: JwtService,
    @Autowired val objectMapper: ObjectMapper,
    @Autowired val venueRepo: VenueRepo,
    @Autowired val performerRepo: PerformerRepo

): BaseIntegrationTest(){

    @Test
    fun `ADMIN user can create event - happy flow`() {
        val adminToken = jwtService.issue("testadmin", listOf("ADMIN"))
        val createEventRequest = CreateEventRequest(
            name = "Test Concert",
            description = "Test concert description",
            venueId = 1L,
            performerId = 1L,
            startTime = OffsetDateTime.now().plusDays(30),
            totalTickets = 100,
            maxPerRequest = 4,
            ticketPrice = BigDecimal("50.00")
        )
        mockMvc.post("/api/events") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(createEventRequest)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.code") { value(201) }
            jsonPath("$.data") { exists() }
            jsonPath("$.msg") { value("Event created and published successfully") }
        }
    }

    @Test
    fun `ADMIN user with invalid request should return 400 Bad Request`() {
        val adminToken = jwtService.issue("testadmin", listOf("ADMIN"))

        val createEventRequest = CreateEventRequest(
            name = "Test Concert",
            description = "Test concert description",
            venueId = 1L,
            performerId = 1L,
            startTime = OffsetDateTime.now().plusDays(30),
            totalTickets = 99999999,
            maxPerRequest = 4,
            ticketPrice = BigDecimal("50.00")
        )

        mockMvc.post("/api/events") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(createEventRequest)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.status") { value("BAD_REQUEST") }
            jsonPath("$.message") { value("Total tickets must be less than or equal to venue capacity") }
        }
    }

    @Test
    fun `USER role cannot create event - should return 403 Forbidden`() {
        val userToken = jwtService.issue("regularuser", listOf("USER"))

        val createEventRequest = CreateEventRequest(
            name = "Test Concert",
            description = "Test concert description",
            venueId = 1L,
            performerId = 1L,
            startTime = OffsetDateTime.now().plusDays(30),
            totalTickets = 100,
            maxPerRequest = 4,
            ticketPrice = BigDecimal("50.00")
        )
        mockMvc.post("/api/events") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $userToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(createEventRequest)
        }.andExpect {
            status { isForbidden() }
        }
    }

}