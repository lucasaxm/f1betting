package com.sportygroup.f1betting.exception;

public class EventOddNotFoundException extends RuntimeException {
    public EventOddNotFoundException() {
        super("Event odd not found");
    }
}
