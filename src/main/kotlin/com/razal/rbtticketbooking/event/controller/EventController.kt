package com.razal.rbtticketbooking.event.controller

import com.razal.rbtticketbooking.event.dto.CreateEventRequest
import com.razal.rbtticketbooking.event.service.EventService
import com.razal.rbtticketbooking.shared.dto.ResponseDto
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class EventController(private val eventService: EventService) {

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/events")
    fun create(@RequestBody req: CreateEventRequest) = ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ResponseDto(
                code = 201,
                data = eventService.publish(req),
                msg = "Event created and published successfully")
        )

}