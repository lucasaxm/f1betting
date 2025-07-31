package com.sportygroup.f1betting.exception;

public class DriverNotFoundException extends RuntimeException {
    public DriverNotFoundException() {
        super("Driver not found");
    }
}
