package com.sportygroup.f1betting.exception;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class,
            InvalidDataAccessApiUsageException.class,
            MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequest(Exception ex) {
        String error = "BAD_REQUEST";
        if (ex.getMessage() != null && ex.getMessage().startsWith("Unknown sort field")) {
            error = "INVALID_SORT_FIELD";
        }
        return new ApiError(error, ex.getMessage());
    }
}
