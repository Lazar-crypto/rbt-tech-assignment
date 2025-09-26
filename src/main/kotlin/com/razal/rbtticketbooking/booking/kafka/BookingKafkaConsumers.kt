package com.razal.rbtticketbooking.booking.kafka

import RBT_LOGGER
import com.razal.rbtticketbooking.booking.dto.FinalizeRequest
import com.razal.rbtticketbooking.booking.dto.FinalizeRequestKafka
import com.razal.rbtticketbooking.booking.dto.ReserveRequestKafka
import com.razal.rbtticketbooking.booking.service.BookingService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class BookingKafkaConsumers(
    private val bookingService: BookingService,
    private val kafkaTemplate: KafkaTemplate<String, FinalizeRequestKafka>
) {
    companion object {
        private const val RESERVE_TOPIC = "ticket.booking.reserve"
        private const val FINALIZE_TOPIC = "ticket.booking.finalize"
    }

    @KafkaListener(topics = [RESERVE_TOPIC],
        concurrency = "3",
        groupId = "booking-service",
        containerFactory = "reserveKafkaListenerContainerFactory"
    )
    fun onReserve(
        msg: ReserveRequestKafka,
        @Header(KafkaHeaders.RECEIVED_PARTITION) p: Int,
        @Header(KafkaHeaders.OFFSET) off: Long,
        @Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?
    ) {
        RBT_LOGGER.debug("RESERVE IN p={} off={} key={} json={}", p, off, key, msg)
        val resp = bookingService.reserveTickets(msg.user, msg.eventId, msg.quantity)
        val finalize = FinalizeRequestKafka(
            user = msg.user,
            req = FinalizeRequest(
                eventId = resp.eventId,
                ticketIds = resp.ticketIds,
                confirmed = true,
                paymentRef = "will-succeed",
                idempotencyKey = UUID.randomUUID().toString()
            )
        )
        kafkaTemplate.send(FINALIZE_TOPIC, finalize)
        RBT_LOGGER.debug("FINALIZE OUT key={} json={}", msg.user, finalize)
    }

    @KafkaListener(
        topics = [FINALIZE_TOPIC],
        concurrency = "3",
        groupId = "booking-service",
        containerFactory = "finalizeKafkaListenerContainerFactory"
    )
    fun onFinalize(
        msg: FinalizeRequestKafka,
        @Header(KafkaHeaders.RECEIVED_PARTITION) p: Int,
        @Header(KafkaHeaders.OFFSET) off: Long,
        @Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?
    ) {
        RBT_LOGGER.debug("FINALIZE IN p={} off={} key={} json={}", p, off, key, msg)
        bookingService.finalizeBooking(msg.user, msg.req)
    }
}