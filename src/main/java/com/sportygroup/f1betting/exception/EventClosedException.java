package com.sportygroup.f1betting.exception;

public class EventClosedException extends RuntimeException {
    public EventClosedException() {
        super("Event is already closed");
    }
}
