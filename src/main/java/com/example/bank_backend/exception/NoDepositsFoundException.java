package com.example.bank_backend.exception;

public class NoDepositsFoundException extends RuntimeException {
    public NoDepositsFoundException(String message) {
        super(message);
    }
}
