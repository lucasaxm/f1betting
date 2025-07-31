package com.sportygroup.f1betting.exception;

public class DuplicateBetException extends RuntimeException {
    public DuplicateBetException() {
        super("Bet already exists for this user and event odd");
    }
}
