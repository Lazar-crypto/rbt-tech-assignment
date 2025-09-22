package com.razal.rbtticketbooking.event.dto

import java.math.BigDecimal
import java.time.OffsetDateTime

data class CreateEventRequest(
    val venueId: Long,
    val performerId: Long,
    val name: String,
    val description: String?,
    val startTime: OffsetDateTime,
    val totalTickets: Int,
    val maxPerRequest: Int,
    val ticketPrice: BigDecimal,
)
data class GetEventResponse(
    val name: String,
    val startTime: OffsetDateTime,
    val remainingTickets: Long,
    val maxPerRequest: Int
)