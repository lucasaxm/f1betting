package com.sportygroup.f1betting.exception;

public class DriverNotInEventException extends RuntimeException {
    public DriverNotInEventException() {
        super("Driver does not participate in this event");
    }
}
