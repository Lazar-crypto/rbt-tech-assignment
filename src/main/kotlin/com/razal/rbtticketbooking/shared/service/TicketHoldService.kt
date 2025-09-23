package com.razal.rbtticketbooking.shared.service

interface TicketHoldService {

    fun tryHold(
        eventId: Long,
        ticketId: Long,
        userId: String,
        ttlSeconds: Long): Boolean

    fun release(eventId: Long, ticketId: Long, userId: String)

    fun calculateRemainingTickets(eventId: Long): Long

    fun activeHeldTickets(eventId: Long): List<Long>
}