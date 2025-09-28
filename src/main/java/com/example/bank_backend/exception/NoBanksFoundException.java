package com.example.bank_backend.exception;

public class NoBanksFoundException extends RuntimeException {
    public NoBanksFoundException(String message) {
        super(message);
    }
}