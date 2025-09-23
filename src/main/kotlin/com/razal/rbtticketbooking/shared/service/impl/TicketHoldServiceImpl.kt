package com.razal.rbtticketbooking.shared.service.impl

import RBT_LOGGER
import com.razal.rbtticketbooking.shared.domain.TicketStatus
import com.razal.rbtticketbooking.shared.repository.TicketRepo
import com.razal.rbtticketbooking.shared.service.TicketHoldService
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class TicketHoldServiceImpl(
    private val redisTemplate: StringRedisTemplate,
    private val ticketRepo: TicketRepo,
): TicketHoldService {

    override fun tryHold(
        eventId: Long,
        ticketId: Long,
        userId: String,
        ttlSeconds: Long
    ): Boolean {
        val score = (Instant.now().epochSecond + ttlSeconds).toDouble()
        val added = redisTemplate.opsForZSet().addIfAbsent(key(eventId), ticketId.toString(), score)
        if (added == true) RBT_LOGGER.debug("Held ticketId={} by userId={} until {}", ticketId, userId, score.toLong())
        else RBT_LOGGER.warn("Failed to hold ticketId={} (already held)", ticketId)
        return added == true
    }

    override fun release(eventId: Long, ticketId: Long, userId: String) {
        redisTemplate.opsForZSet().remove(key(eventId), ticketId.toString())
        RBT_LOGGER.debug("Released ticketId={} (userId={})", ticketId, userId)
    }

    override fun calculateRemainingTickets(eventId: Long): Long {
        val holdsCount = activeHoldsCount(eventId)
        val availableCount = ticketRepo.countByEventIdAndStatus(eventId, TicketStatus.AVAILABLE)
        RBT_LOGGER.info("Event: {}, holdsCount: {}, availableCount: {}", eventId, holdsCount, availableCount)
        return availableCount - holdsCount
    }

    override fun activeHeldTickets(eventId: Long): List<Long> {
        purgeExpiredHolds(eventId)
        val now = Instant.now().epochSecond.toDouble()
        val key = key(eventId)

        val members: Set<String> =
            redisTemplate.opsForZSet()
                .rangeByScore(key, now, Double.POSITIVE_INFINITY)
                ?.toSet() ?: emptySet()

        return members.map { it.toLong() }
    }

    private fun activeHoldsCount(eventId: Long): Long {
        purgeExpiredHolds(eventId)
        return redisTemplate.opsForZSet().zCard(key(eventId)) ?: 0L
    }
    private fun purgeExpiredHolds(
        eventId: Long,
        nowEpochSeconds: Long = Instant.now().epochSecond) {

        redisTemplate.opsForZSet()
            .removeRangeByScore(key(eventId), 0.0, nowEpochSeconds.toDouble())
    }
    private fun key(eventId: Long) = "event:${eventId}:holds"
}