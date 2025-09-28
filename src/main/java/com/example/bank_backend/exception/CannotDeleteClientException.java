package com.example.bank_backend.exception;

public class CannotDeleteClientException extends RuntimeException {
    public CannotDeleteClientException(String message) {
        super(message);
    }
}
