package com.razal.rbtticketbooking.event.repository

import com.razal.rbtticketbooking.shared.domain.Venue
import org.springframework.data.jpa.repository.JpaRepository

interface VenueRepo: JpaRepository<Venue, Long>
