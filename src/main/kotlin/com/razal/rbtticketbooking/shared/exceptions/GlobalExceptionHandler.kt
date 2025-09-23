package com.razal.rbtticketbooking.shared.exceptions

import RBT_LOGGER
import com.razal.rbtticketbooking.shared.dto.ErrorResponseDto
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.time.LocalDateTime


@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any> {
        val errors = mutableMapOf<String, String?>()
        ex.bindingResult.allErrors.forEach { error ->
            val field = (error as? FieldError)?.field ?: "object"
            errors[field] = error.defaultMessage
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors)
    }

    @ExceptionHandler(Exception::class)
    fun handleGlobalException(ex: Exception, webRequest: WebRequest): ResponseEntity<ErrorResponseDto> {
        val body = ErrorResponseDto(
            path = webRequest.getDescription(false),
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            message = ex.message,
            timestamp = LocalDateTime.now()
        )
        RBT_LOGGER.error("unhandled error path={} msg={}", webRequest.getDescription(false), ex.message, ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body)
    }

    @ExceptionHandler(AuthorizationDeniedException::class)
    fun handleAuthorizationDenied(ex: AuthorizationDeniedException, webRequest: WebRequest): ResponseEntity<ErrorResponseDto> {
        val body = ErrorResponseDto(
            path = webRequest.getDescription(false),
            status = HttpStatus.FORBIDDEN,
            message = "Access Denied",
            timestamp = LocalDateTime.now()
        )
        RBT_LOGGER.warn("authorization denied path={} msg={}", webRequest.getDescription(false), ex.message)
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body)
    }

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(ex: AuthenticationException, webRequest: WebRequest): ResponseEntity<ErrorResponseDto> {
        val body = ErrorResponseDto(
            path = webRequest.getDescription(false),
            status = HttpStatus.UNAUTHORIZED,
            message = "Authentication required",
            timestamp = LocalDateTime.now()
        )
        RBT_LOGGER.warn("authentication failed path={} msg={}", webRequest.getDescription(false), ex.message)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body)
    }

    @ExceptionHandler(HttpClientErrorException::class)
    fun handleHttpClientErrorException(ex: HttpClientErrorException, webRequest: WebRequest): ResponseEntity<ErrorResponseDto> {
        val body = ErrorResponseDto(
            path = webRequest.getDescription(false),
            status = HttpStatus.BAD_REQUEST,
            message = ex.responseBodyAsString.takeIf { it.isNotBlank() } ?: ex.message ?: "External API error",
            timestamp = LocalDateTime.now()
        )
        RBT_LOGGER.warn("external API error path={} status={} msg={}", webRequest.getDescription(false), ex.statusCode, ex.message)
        return ResponseEntity.status(ex.statusCode).body(body)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException, webRequest: WebRequest): ResponseEntity<ErrorResponseDto> {
        val body = ErrorResponseDto(
            path = webRequest.getDescription(false),
            status = HttpStatus.BAD_REQUEST,
            message = ex.message ?: "Invalid request parameters",
            timestamp = LocalDateTime.now()
        )
        RBT_LOGGER.warn("validation error path={} msg={}", webRequest.getDescription(false), ex.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    @ExceptionHandler(DummyJsonException::class)
    fun handleDummyJsonLoginException(ex: DummyJsonException, webRequest: WebRequest): ResponseEntity<ErrorResponseDto> {
        val body = ErrorResponseDto(
            path = webRequest.getDescription(false),
            status = HttpStatus.BAD_REQUEST,
            message = ex.message,
            timestamp = LocalDateTime.now()
        )
        RBT_LOGGER.warn("dummy json login error path={} msg={}", webRequest.getDescription(false), ex.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    @ExceptionHandler(EntityAlreadyExistsException::class)
    fun handleEntityAlreadyExists(ex: EntityAlreadyExistsException, webRequest: WebRequest): ResponseEntity<ErrorResponseDto> {
        val body = ErrorResponseDto(
            path = webRequest.getDescription(false),
            status = HttpStatus.BAD_REQUEST,
            message = ex.message,
            timestamp = LocalDateTime.now()
        )
        RBT_LOGGER.warn("already exists path={} msg={}", webRequest.getDescription(false), ex.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFound(ex: ResourceNotFoundException, webRequest: WebRequest): ResponseEntity<ErrorResponseDto> {
        val body = ErrorResponseDto(
            path = webRequest.getDescription(false),
            status = HttpStatus.NOT_FOUND,
            message = ex.message,
            timestamp = LocalDateTime.now()
        )
        RBT_LOGGER.warn("not found path={} msg={}", webRequest.getDescription(false), ex.message)
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body)
    }

    @ExceptionHandler(PaymentException::class)
    fun handleResourceNotFound(ex: PaymentException, webRequest: WebRequest): ResponseEntity<ErrorResponseDto> {
        val body = ErrorResponseDto(
            path = webRequest.getDescription(false),
            status = HttpStatus.SERVICE_UNAVAILABLE,
            message = ex.message,
            timestamp = LocalDateTime.now()
        )
        RBT_LOGGER.warn("payment provider service error, path={} msg={}", webRequest.getDescription(false), ex.message)
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body)
    }

}