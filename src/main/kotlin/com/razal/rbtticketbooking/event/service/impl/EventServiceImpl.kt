package com.razal.rbtticketbooking.event.service.impl

import RBT_LOGGER
import com.razal.rbtticketbooking.event.dto.CreateEventRequest
import com.razal.rbtticketbooking.event.dto.GetEventResponse
import com.razal.rbtticketbooking.event.repository.EventRepo
import com.razal.rbtticketbooking.event.repository.PerformerRepo
import com.razal.rbtticketbooking.event.repository.VenueRepo
import com.razal.rbtticketbooking.event.service.EventService
import com.razal.rbtticketbooking.shared.domain.Event
import com.razal.rbtticketbooking.shared.domain.EventStatus
import com.razal.rbtticketbooking.shared.domain.Ticket
import com.razal.rbtticketbooking.shared.domain.TicketStatus
import com.razal.rbtticketbooking.shared.exceptions.ResourceNotFoundException
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class EventServiceImpl(
    private val eventRepo: EventRepo,
    private val venueRepo: VenueRepo,
    private val performerRepo: PerformerRepo,
    @param:Value("\${spring.jpa.properties.hibernate.jdbc.batch_size}") private val batchSize: Int,
    @PersistenceContext private val entityManager: EntityManager
    ): EventService {

    @Transactional
    override fun publish(req: CreateEventRequest): Long {
        val venue = venueRepo.findById(req.venueId).orElseThrow { ResourceNotFoundException("Venue not found") }
        val performer = performerRepo.findById(req.performerId).orElseThrow { ResourceNotFoundException("Performer not found") }

        require(req.totalTickets <= venue.capacity) { "Total tickets must be less than or equal to venue capacity" }
        require(req.maxPerRequest > 0) { "Max per request must be greater than 0" }
        require(req.ticketPrice >= BigDecimal.ZERO) { "Ticket price must greater than 0" }

        val event = eventRepo.save(
            Event(
                venue = venue,
                performer = performer,
                name = req.name,
                description = req.description,
                startTime = req.startTime,
                totalTickets = req.totalTickets,
                maxPerRequest = req.maxPerRequest,
                status = EventStatus.PUBLISHED
            )
        )
        val price = req.ticketPrice.setScale(2, RoundingMode.HALF_UP)
        for (i in 1..req.totalTickets) {
            entityManager.persist(Ticket(event = event, price = price, status = TicketStatus.AVAILABLE))
            if (i % batchSize == 0) {
                entityManager.flush()
                entityManager.clear()
            }
        }
        RBT_LOGGER.info("Event published successfully, ID: {}", event.id)
        return event.id!!
    }

    override fun getAllEvents(
        page: Int,
        size: Int,
        sort: String
    ): List<GetEventResponse> {
        TODO("Not yet implemented")
    }
}