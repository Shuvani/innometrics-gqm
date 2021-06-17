package com.innometrics.gqm_api.configuration;

import com.innometrics.gqm_api.dto.ApiErrorResponse;
import lombok.*;
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@RestControllerAdvice
public class RestExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiErrorResponse handleBadRequest(
            HttpServletRequest req, MethodArgumentNotValidException ex
    ) {
        val apiError = ApiErrorResponse
                .builder()
                .path(req.getServletPath())
                .status(HttpStatus.BAD_REQUEST)
                .message("Validation error")
                .dateTime(LocalDateTime.now())
                .build();

        for (val fieldError : ex.getBindingResult().getFieldErrors()) {
            apiError.addError(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return apiError;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(JdbcSQLIntegrityConstraintViolationException.class)
    public ApiErrorResponse handleJdbcSQLIntegrityConstraintViolation(
            HttpServletRequest request, JdbcSQLIntegrityConstraintViolationException exception
    ) {
        return ApiErrorResponse.builder()
                               .dateTime(LocalDateTime.now())
                               .path(request.getServletPath())
                               .status(HttpStatus.BAD_REQUEST)
                               .message(exception.getMessage())
                               .build();

    }

}
