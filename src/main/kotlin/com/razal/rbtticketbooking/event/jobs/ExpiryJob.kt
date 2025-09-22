package com.razal.rbtticketbooking.event.jobs

import RBT_LOGGER
import com.razal.rbtticketbooking.event.repository.EventRepo
import com.razal.rbtticketbooking.event.repository.TicketRepo
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Component
class ExpiryJob(
    private val ticketRepo: TicketRepo,
    private val eventRepo: EventRepo,
) {

    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    fun expirePastEventTickets() {
        val nowUtc = OffsetDateTime.now(ZoneOffset.UTC)
        val cutoff = nowUtc.minusHours(24)

        val closedEvents = eventRepo.bulkClosePastEvents(cutoff)
        if (closedEvents > 0 ){
            val expiredTickets = ticketRepo.bulkExpireTickets()
            RBT_LOGGER.info("Closed $closedEvents events and expired $expiredTickets tickets")
        }

    }
}