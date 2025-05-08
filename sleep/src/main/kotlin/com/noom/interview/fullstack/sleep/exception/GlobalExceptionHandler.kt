package com.noom.interview.fullstack.sleep.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import java.time.LocalDateTime

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        // Determine if this is a user validation error
        val isUserValidationError = ex.message?.contains("Username") == true ||
                ex.message?.contains("Email") == true

        val status = if (isUserValidationError) HttpStatus.CONFLICT else HttpStatus.BAD_REQUEST
        val error = if (isUserValidationError) "Conflict" else "Bad Request"

        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = status.value(),
            error = error,
            message = ex.message ?: "Invalid input provided",
            path = request.getDescription(false).replace("uri=", "")
        )

        return ResponseEntity(errorResponse, status)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(
        ex: HttpMessageNotReadableException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorMessage = when {
            ex.message?.contains("LocalDateTime") == true ->
                "Invalid date format. Please use ISO-8601 format (e.g., '2025-05-06T03:30:00'). No additional characters allowed."
            else -> "Invalid JSON format: ${ex.message?.split(":")?.get(0) ?: "Parse error"}"
        }

        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = errorMessage,
            path = request.getDescription(false).replace("uri=", "")
        )

        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }


    data class ErrorResponse(
        val timestamp: LocalDateTime,
        val status: Int,
        val error: String,
        val message: String,
        val path: String
    )
}
