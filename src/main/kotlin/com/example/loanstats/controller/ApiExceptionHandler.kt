package com.example.loanstats.controller

import com.example.loanstats.service.InvalidRequestException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(InvalidRequestException::class)
    fun handleInvalidRequest(ex: InvalidRequestException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.message ?: "Invalid request").apply {
            title = "Invalid request"
        }
}
