package com.razal.rbtticketbooking.shared.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.OffsetDateTime

@Entity
@Table(name = "event", schema = "rbt")
class Event(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    var venue: Venue,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performer_id")
    var performer: Performer? = null,

    @Column(name = "name", nullable = false)
    var name: String,

    @Column(name = "description")
    var description: String? = null,

    @Column(name = "start_time", nullable = false)
    var startTime: OffsetDateTime,

    @Column(name = "total_tickets", nullable = false)
    var totalTickets: Int,

    @Column(name = "max_per_request", nullable = false)
    var maxPerRequest: Int,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    var status: EventStatus
) : BaseEntity()