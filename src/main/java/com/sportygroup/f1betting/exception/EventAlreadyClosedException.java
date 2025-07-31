package com.sportygroup.f1betting.exception;

public class EventAlreadyClosedException extends RuntimeException {
    public EventAlreadyClosedException() {
        super("Winner already recorded for this event");
    }
}
