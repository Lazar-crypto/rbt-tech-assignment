package com.razal.rbtticketbooking.shared.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

@Schema(name = "ErrorResponse", description = "Error response information")
data class ErrorResponseDto(
    @field:Schema(description = "API path invoked by client")
    val path: String?,
    @field:Schema(description = "HTTP status code")
    val status: HttpStatus,
    @field:Schema(description = "Error message")
    val message: String?,
    @field:Schema(description = "Time when the error happened")
    val timestamp: LocalDateTime = LocalDateTime.now()
)

@Schema(name = "Response", description = "Successful response wrapper")
data class ResponseDto<T>(
    @field:Schema(description = "HTTP status included in response body")
    val code: Int = 200,
    @field:Schema(description = "Human-readable status message")
    val msg: String = "OK",
    @field:Schema(description = "Payload")
    val data: T?
)