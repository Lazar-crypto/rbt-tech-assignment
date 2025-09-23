package com.razal.rbtticketbooking.payment.service

interface PaymentClient {
    fun isPaymentSuccessful(paymentRef: String): Boolean
}