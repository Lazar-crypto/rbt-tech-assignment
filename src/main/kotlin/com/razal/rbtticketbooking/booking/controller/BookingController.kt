package com.razal.rbtticketbooking.booking.controller

import com.razal.rbtticketbooking.booking.dto.FinalizeRequest
import com.razal.rbtticketbooking.booking.dto.ReserveRequest
import com.razal.rbtticketbooking.booking.service.BookingService
import com.razal.rbtticketbooking.shared.dto.ResponseDto
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/booking", produces = [MediaType.APPLICATION_JSON_VALUE])
@PreAuthorize("hasRole('USER')")
class BookingController(
    private val bookingService: BookingService
) {

    @PostMapping("/reserve")
    fun reserve(
        auth: Authentication,
        @RequestBody req: ReserveRequest
    ) = ResponseEntity.ok(
        ResponseDto(data = bookingService.reserveTickets(auth.name, req.eventId, req.quantity))
    )

    @PostMapping("/finalize")
    fun finalizeBooking(
        auth: Authentication,
        @RequestBody req: FinalizeRequest
    ) = ResponseEntity.ok(
            ResponseDto(
                data = bookingService.finalizeBooking(auth.name, req),
                msg = "Booking ${if (req.confirmed) "confirmed" else "canceled"}"
            )
        )

}