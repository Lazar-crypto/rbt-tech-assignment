package com.razal.rbtticketbooking.event

import com.razal.rbtticketbooking.event.dto.CreateEventRequest
import com.razal.rbtticketbooking.event.repository.EventRepo
import com.razal.rbtticketbooking.event.repository.PerformerRepo
import com.razal.rbtticketbooking.event.repository.VenueRepo
import com.razal.rbtticketbooking.event.service.impl.EventServiceImpl
import com.razal.rbtticketbooking.shared.domain.Event
import com.razal.rbtticketbooking.shared.domain.EventStatus
import com.razal.rbtticketbooking.shared.domain.Performer
import com.razal.rbtticketbooking.shared.domain.Venue
import com.razal.rbtticketbooking.shared.exceptions.ResourceNotFoundException
import com.razal.rbtticketbooking.shared.service.TicketHoldService
import jakarta.persistence.EntityManager
import junit.framework.TestCase.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.Optional
import kotlin.test.Test

class EventServiceImplTest {

    private lateinit var eventRepo: EventRepo
    private lateinit var venueRepo: VenueRepo
    private lateinit var performerRepo: PerformerRepo
    private lateinit var entityManager: EntityManager
    private lateinit var eventService: EventServiceImpl
    private lateinit var ticketHold: TicketHoldService

    private val batchSize = 20

    @BeforeEach
    fun setup() {
        eventRepo = mock()
        venueRepo = mock()
        performerRepo = mock()
        entityManager = mock()
        ticketHold = mock()
        eventService = EventServiceImpl(
            eventRepo = eventRepo,
            venueRepo = venueRepo,
            performerRepo = performerRepo,
            batchSize = batchSize,
            entityManager = entityManager,
            ticketHoldService = ticketHold

        )
    }

    @Test
    fun `publish should create event and tickets successfully - happy flow`() {
        val venue = Venue(id = 1L, name = "Test Venue", capacity = 1000, address = "Test Address")
        val performer = Performer(id = 1L, name = "Test Performer", genre = "Rock")
        val savedEvent = Event(
            id = 123L,
            venue = venue,
            performer = performer,
            name = "Test Concert",
            description = "Test Description",
            startTime = OffsetDateTime.now().plusDays(30),
            totalTickets = 100,
            maxPerRequest = 4,
            status = EventStatus.PUBLISHED
        )
        val request = CreateEventRequest(
            name = "Test Concert",
            description = "Test Description",
            venueId = 1L,
            performerId = 1L,
            startTime = OffsetDateTime.now().plusDays(30),
            totalTickets = 100,
            maxPerRequest = 4,
            ticketPrice = BigDecimal("50.00")
        )
        whenever(venueRepo.findById(1L)).thenReturn(Optional.of(venue))
        whenever(performerRepo.findById(1L)).thenReturn(Optional.of(performer))
        whenever(eventRepo.save(any<Event>())).thenReturn(savedEvent)

        val result = eventService.publish(request)
        assertEquals(123L, result)

        verify(venueRepo).findById(1L)
        verify(performerRepo).findById(1L)
        verify(eventRepo).save(argThat<Event> {
            name == "Test Concert" &&
                    totalTickets == 100 &&
                    status == EventStatus.PUBLISHED
        })
        verify(entityManager, times(100)).persist(any())
        verify(entityManager, times(5)).flush()
        verify(entityManager, times(5)).clear()
    }

    @Test
    fun `publish should throw ResourceNotFoundException when venue not found`() {
        val request = CreateEventRequest(
            name = "Test Concert",
            description = "Test Description",
            venueId = 999L,
            performerId = 1L,
            startTime = OffsetDateTime.now().plusDays(30),
            totalTickets = 100,
            maxPerRequest = 4,
            ticketPrice = BigDecimal("50.00")
        )
        whenever(venueRepo.findById(999L)).thenReturn(Optional.empty())

        val exception = assertThrows<ResourceNotFoundException> {
            eventService.publish(request)
        }
        assertEquals("Venue not found", exception.message)

        verify(eventRepo, never()).save(any<Event>())
        verify(entityManager, never()).persist(any())
    }

    @Test
    fun `publish should throw ResourceNotFoundException when performer not found`() {
        val venue = Venue(id = 1L, name = "Test Venue", capacity = 1000, address = "Test Address")
        val request = CreateEventRequest(
            name = "Test Concert",
            description = "Test Description",
            venueId = 1L,
            performerId = 999L,
            startTime = OffsetDateTime.now().plusDays(30),
            totalTickets = 100,
            maxPerRequest = 4,
            ticketPrice = BigDecimal("50.00")
        )
        whenever(venueRepo.findById(1L)).thenReturn(Optional.of(venue))
        whenever(performerRepo.findById(999L)).thenReturn(Optional.empty())

        val exception = assertThrows<ResourceNotFoundException> {
            eventService.publish(request)
        }
        assertEquals("Performer not found", exception.message)

        verify(eventRepo, never()).save(any<Event>())
        verify(entityManager, never()).persist(any())
    }

    @Test
    fun `publish should throw IllegalArgumentException when totalTickets exceeds venue capacity`() {
        val venue = Venue(id = 1L, name = "Test Venue", capacity = 50, address = "Test Address")
        val performer = Performer(id = 1L, name = "Test Performer", genre = "Rock")
        val request = CreateEventRequest(
            name = "Test Concert",
            description = "Test Description",
            venueId = 1L,
            performerId = 1L,
            startTime = OffsetDateTime.now().plusDays(30),
            totalTickets = 100,
            maxPerRequest = 4,
            ticketPrice = BigDecimal("50.00")
        )
        whenever(venueRepo.findById(1L)).thenReturn(Optional.of(venue))
        whenever(performerRepo.findById(1L)).thenReturn(Optional.of(performer))

        val exception = assertThrows<IllegalArgumentException> {
            eventService.publish(request)
        }
        assertEquals("Total tickets must be less than or equal to venue capacity", exception.message)

        verify(eventRepo, never()).save(any<Event>())
        verify(entityManager, never()).persist(any())
    }

    @Test
    fun `publish should throw IllegalArgumentException when ticketPrice is negative`() {
        val venue = Venue(id = 1L, name = "Test Venue", capacity = 1000, address = "Test Address")
        val performer = Performer(id = 1L, name = "Test Performer", genre = "Rock")

        val request = CreateEventRequest(
            name = "Test Concert",
            description = "Test Description",
            venueId = 1L,
            performerId = 1L,
            startTime = OffsetDateTime.now().plusDays(30),
            totalTickets = 100,
            maxPerRequest = 4,
            ticketPrice = BigDecimal("-10.00")
        )
        whenever(venueRepo.findById(1L)).thenReturn(Optional.of(venue))
        whenever(performerRepo.findById(1L)).thenReturn(Optional.of(performer))

        val exception = assertThrows<IllegalArgumentException> {
            eventService.publish(request)
        }
        assertEquals("Ticket price must greater than 0", exception.message)

        verify(eventRepo, never()).save(any<Event>())
        verify(entityManager, never()).persist(any())
    }
}