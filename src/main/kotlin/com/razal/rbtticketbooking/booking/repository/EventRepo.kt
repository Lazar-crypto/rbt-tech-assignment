package com.razal.rbtticketbooking.booking.repository

import com.razal.rbtticketbooking.shared.domain.Event
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository("eventBookingRepo")
interface EventRepo: JpaRepository<Event, Long>
