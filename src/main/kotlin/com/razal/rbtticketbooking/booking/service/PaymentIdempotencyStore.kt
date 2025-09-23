package com.razal.rbtticketbooking.booking.service

interface PaymentIdempotencyStore {

    fun getFromCache(key: String): String?

    fun saveToCache(key: String, status: String, ttlSeconds: Long = 300)
}