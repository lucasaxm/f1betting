package com.sportygroup.f1betting.exception;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleUserNotFound(UserNotFoundException ex) {
        return new ApiError("USER_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(EventOddNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleEventOddNotFound(EventOddNotFoundException ex) {
        return new ApiError("EVENT_ODD_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(OutdatedEventOddException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleOutdatedEventOdd(OutdatedEventOddException ex) {
        return new ApiError("OUTDATED_EVENT_ODD", ex.getMessage());
    }

    @ExceptionHandler(EventClosedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleEventClosed(EventClosedException ex) {
        return new ApiError("EVENT_CLOSED", ex.getMessage());
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInsufficientBalance(InsufficientBalanceException ex) {
        return new ApiError("INSUFFICIENT_BALANCE", ex.getMessage());
    }

    @ExceptionHandler(InvalidBetAmountException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInvalidBetAmount(InvalidBetAmountException ex) {
        return new ApiError("INVALID_BET_AMOUNT", ex.getMessage());
    }

    @ExceptionHandler(DuplicateBetException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDuplicateBet(DuplicateBetException ex) {
        return new ApiError("DUPLICATE_BET", ex.getMessage());
    }

    @ExceptionHandler(EventNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleEventNotFound(EventNotFoundException ex) {
        return new ApiError("EVENT_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(DriverNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleDriverNotFound(DriverNotFoundException ex) {
        return new ApiError("DRIVER_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(DriverNotInEventException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleDriverNotInEvent(DriverNotInEventException ex) {
        return new ApiError("DRIVER_NOT_IN_EVENT", ex.getMessage());
    }

    @ExceptionHandler(EventAlreadyClosedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleEventAlreadyClosed(EventAlreadyClosedException ex) {
        return new ApiError("EVENT_ALREADY_CLOSED", ex.getMessage());
    }
}
