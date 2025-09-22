package com.razal.rbtticketbooking.event.repository

import com.razal.rbtticketbooking.shared.domain.EventStatus
import com.razal.rbtticketbooking.shared.domain.Ticket
import com.razal.rbtticketbooking.shared.domain.TicketStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface TicketRepo: JpaRepository<Ticket, Long> {


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Ticket t
        set t.status = :expired
        where t.status = :available and t.event.status = :closed
    """)
    fun bulkExpireTickets(
        @Param("closed") closed: EventStatus = EventStatus.CLOSED,
        @Param("expired") expired: TicketStatus = TicketStatus.EXPIRED,
        @Param("available") available: TicketStatus = TicketStatus.AVAILABLE
    ): Int

}