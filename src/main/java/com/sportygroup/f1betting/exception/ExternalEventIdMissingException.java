package com.sportygroup.f1betting.exception;

public class ExternalEventIdMissingException extends RuntimeException {
    public ExternalEventIdMissingException() {
        super("External event ID is missing.");
    }
}

