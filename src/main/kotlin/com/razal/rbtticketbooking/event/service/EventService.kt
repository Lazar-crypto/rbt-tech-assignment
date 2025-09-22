package com.razal.rbtticketbooking.event.service

import com.razal.rbtticketbooking.event.dto.CreateEventRequest
import com.razal.rbtticketbooking.event.dto.GetEventResponse

interface EventService {
    fun publish(req: CreateEventRequest): Long
    fun getAllEvents(
        page: Int,
        size: Int,
        sort: String
    ): List<GetEventResponse>
}
