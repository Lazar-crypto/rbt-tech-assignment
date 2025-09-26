package com.razal.rbtticketbooking.booking.dto

data class ReserveRequest(
    val eventId: Long,
    val quantity: Int
)
data class ReserveRequestKafka(
    val user: String,
    val eventId: Long,
    val quantity: Int
)

data class ReserveTicketResponse(
    val eventId: Long,
    val ticketIds: List<Long>,
    val expiresAtEpochSeconds: Long
)

data class FinalizeRequest(
    val eventId: Long,
    val ticketIds: List<Long>,
    val confirmed: Boolean,
    val paymentRef: String, // will-succeed / will-fail - values for mock testing
    val idempotencyKey: String
)
data class FinalizeRequestKafka(
    val user: String,
    val req: FinalizeRequest
)



