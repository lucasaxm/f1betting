package com.sportygroup.f1betting.exception;

public class InvalidBetAmountException extends RuntimeException {
    public InvalidBetAmountException() {
        super("Bet amount must be positive");
    }
}
