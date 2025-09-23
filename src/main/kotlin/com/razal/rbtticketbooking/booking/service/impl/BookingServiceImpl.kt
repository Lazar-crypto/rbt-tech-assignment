package com.razal.rbtticketbooking.booking.service.impl

import RBT_LOGGER
import com.razal.rbtticketbooking.booking.dto.FinalizeRequest
import com.razal.rbtticketbooking.booking.dto.ReserveTicketResponse
import com.razal.rbtticketbooking.booking.repository.EventRepo
import com.razal.rbtticketbooking.booking.repository.TicketRepo
import com.razal.rbtticketbooking.booking.service.BookingService
import com.razal.rbtticketbooking.booking.service.PaymentIdempotencyStore
import com.razal.rbtticketbooking.payment.service.PaymentClient
import com.razal.rbtticketbooking.shared.config.AppProperties
import com.razal.rbtticketbooking.shared.domain.EventStatus
import com.razal.rbtticketbooking.shared.exceptions.PaymentException
import com.razal.rbtticketbooking.shared.exceptions.TicketHoldException
import com.razal.rbtticketbooking.shared.service.TicketHoldService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
class BookingServiceImpl(
    private val eventRepo: EventRepo,
    private val ticketHoldService: TicketHoldService,
    private val ticketRepo: TicketRepo,
    private val paymentCache: PaymentIdempotencyStore,
    private val paymentClient: PaymentClient,
    private val appProps: AppProperties,
) : BookingService {

    @Transactional(readOnly = true)
    override fun reserveTickets(
        user: String,
        eventId: Long,
        qty: Int
    ): ReserveTicketResponse {
        require(qty > 0) { "Quantity must be greater than zero" }
        val event = eventRepo.findById(eventId).orElseThrow { throw Exception("Event not found") }
        require(event.status == EventStatus.PUBLISHED) {"Event is closed"}
        require(event.startTime.isAfter(OffsetDateTime.now())) { "Event already started/passed" }
        require(qty <= event.maxPerRequest) { "Quantity exceeds maxPerRequest=${event.maxPerRequest}" }
        val remainingTickets = ticketHoldService.calculateRemainingTickets(eventId)
        require(remainingTickets >= qty) { "Not enough tickets available" }

        val picked = mutableListOf<Long>()
        val maxTries = 3
        val pageSize = event.maxPerRequest * 10
        val ttlSeconds = (appProps.ticketTtlMin * 60).toLong()
        repeat(maxTries) {
            if (picked.size == qty) return@repeat

            val heldTickets = ticketHoldService.activeHeldTickets(eventId)

            val page = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.ASC, "id"))
            val ticketsToHold =
                if (heldTickets.isEmpty()) ticketRepo.findAvailableIds(eventId, pageable = page)
                else ticketRepo.findAvailableExcludingIds(eventId, heldTickets, pageable = page)

            for (ticketId in ticketsToHold) {
                if (ticketHoldService.tryHold(eventId, ticketId, user, ttlSeconds)) {
                    picked += ticketId
                    if (picked.size == qty) break
                }
            }
        }
        if (picked.size < qty) {
            releaseHolds(eventId, picked, user)
            throw TicketHoldException("Could not hold requested amount")
        }
        return ReserveTicketResponse(eventId, picked, ttlSeconds)
    }

    @Transactional
    override fun finalizeBooking(user: String, req: FinalizeRequest) {
        paymentCache.getFromCache(req.idempotencyKey)?.let { status ->
            RBT_LOGGER.info("Idempotency key: {} found in cache, status: {}", req.idempotencyKey, status)
            return
        }
        val heldNow = ticketHoldService.activeHeldTickets(req.eventId)
        require(heldNow.containsAll(req.ticketIds)) {"Hold expired or not owned by this user"}

        if (!req.confirmed) {
            releaseHolds(req.eventId, req.ticketIds, user)
            RBT_LOGGER.info("Booking canceled for eventId={}, ticketIds={}", req.eventId, req.ticketIds)
            return
        }
        // TODO - web hook for payment would be better
        if (!paymentClient.isPaymentSuccessful(req.paymentRef)) throw PaymentException("Payment was not successful, paymentRef=${req.paymentRef}")

        ticketRepo.bulkBook(req.ticketIds, user)
        releaseHolds(req.eventId, req.ticketIds, user)
        paymentCache.saveToCache(req.idempotencyKey, "CONFIRMED")
    }

    private fun releaseHolds(eventId: Long, ticketIds: List<Long>, user: String) {
        ticketIds.forEach { ticketHoldService.release(eventId, it, user) }
    }

}