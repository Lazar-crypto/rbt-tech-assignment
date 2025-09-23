package com.razal.rbtticketbooking.shared.repository

import com.razal.rbtticketbooking.shared.domain.Ticket
import com.razal.rbtticketbooking.shared.domain.TicketStatus
import org.springframework.data.jpa.repository.JpaRepository

interface TicketRepo: JpaRepository<Ticket, Long> {

    fun countByEventIdAndStatus(eventId: Long, status: TicketStatus): Long
}