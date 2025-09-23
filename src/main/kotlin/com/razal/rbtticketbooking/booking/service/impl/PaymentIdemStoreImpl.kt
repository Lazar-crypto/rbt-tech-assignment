package com.razal.rbtticketbooking.booking.service.impl

import com.razal.rbtticketbooking.booking.service.PaymentIdempotencyStore
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.time.Duration

@Service
class PaymentIdemStoreImpl(
    private val redisTemplate: StringRedisTemplate
): PaymentIdempotencyStore {

    override fun getFromCache(key: String) = redisTemplate.opsForValue().get(key(key))

    override fun saveToCache(key: String, status: String, ttlSeconds: Long) {
        val k = key(key)
        TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
            override fun afterCommit() {
                redisTemplate.opsForValue()
                    .set(k, status, Duration.ofSeconds(ttlSeconds))
            }
        })
    }

    private fun key(k: String) = "idem:$k"


}