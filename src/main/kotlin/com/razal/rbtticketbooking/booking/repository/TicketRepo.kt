package com.razal.rbtticketbooking.booking.repository

import com.razal.rbtticketbooking.shared.domain.Ticket
import com.razal.rbtticketbooking.shared.domain.TicketStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository("ticketBookingRepo")
interface TicketRepo: JpaRepository<Ticket, Long> {

    @Query("""
        select t.id
        from Ticket t
        where t.event.id = :eventId
          and t.status = :status
          and t.id not in :heldIds
        order by t.id
    """)
    fun findAvailableExcludingIds(
        @Param("eventId") eventId: Long,
        @Param("heldIds") heldIds: Collection<Long>,
        @Param("status") status: TicketStatus = TicketStatus.AVAILABLE,
        pageable: Pageable
    ): List<Long>

    @Query("""
        select t.id
        from Ticket t
        where t.event.id = :eventId
          and t.status = :status
        order by t.id
    """)
    fun findAvailableIds(
        @Param("eventId") eventId: Long,
        @Param("status") status: TicketStatus = TicketStatus.AVAILABLE,
        pageable: Pageable
    ): List<Long>

    @Modifying
    @Query("""
        update Ticket t
           set t.status = :booked,
               t.bookedAt = :now,
               t.bookedByRef = :user
         where t.id in :ids and t.status = :available
    """)
    fun bulkBook(
        @Param("ids") ids: List<Long>,
        @Param("user") user: String,
        @Param("booked") booked: TicketStatus = TicketStatus.BOOKED,
        @Param("available") available: TicketStatus = TicketStatus.AVAILABLE,
        @Param("now") now: OffsetDateTime = OffsetDateTime.now()
    ): Int
}