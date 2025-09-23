package com.razal.rbtticketbooking.booking.service

import com.razal.rbtticketbooking.booking.dto.FinalizeRequest
import com.razal.rbtticketbooking.booking.dto.ReserveTicketResponse

interface BookingService {

    fun reserveTickets(
        user: String,
        eventId: Long,
        qty: Int
    ): ReserveTicketResponse

    fun finalizeBooking(user: String, req: FinalizeRequest)

}