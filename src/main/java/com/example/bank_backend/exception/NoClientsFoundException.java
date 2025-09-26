package com.example.bank_backend.exception;

public class NoClientsFoundException extends RuntimeException {
    public NoClientsFoundException(String message) {
        super(message);
    }
}
