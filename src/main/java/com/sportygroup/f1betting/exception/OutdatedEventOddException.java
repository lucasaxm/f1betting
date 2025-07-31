package com.sportygroup.f1betting.exception;

public class OutdatedEventOddException extends RuntimeException {
    public OutdatedEventOddException() {
        super("The provided event odd is outdated");
    }
}
