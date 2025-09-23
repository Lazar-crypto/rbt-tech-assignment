package com.razal.rbtticketbooking.event.repository

import com.razal.rbtticketbooking.shared.domain.Event
import com.razal.rbtticketbooking.shared.domain.EventStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.OffsetDateTime

interface EventRepo: JpaRepository<Event, Long>{

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Event e
        set e.status = :closed
        where e.status = :published and e.startTime <= :cutoff
    """)
    fun bulkClosePastEvents(
        @Param("cutoff") cutoff: OffsetDateTime,
        @Param("published") published: EventStatus = EventStatus.PUBLISHED,
        @Param("closed") closed: EventStatus = EventStatus.CLOSED
    ): Int

    fun findByStatus(status: EventStatus, page: Pageable): Page<Event>
}
