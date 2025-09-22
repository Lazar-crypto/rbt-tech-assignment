package com.razal.rbtticketbooking.event.repository

import com.razal.rbtticketbooking.shared.domain.Performer
import org.springframework.data.jpa.repository.JpaRepository

interface PerformerRepo: JpaRepository<Performer, Long>
