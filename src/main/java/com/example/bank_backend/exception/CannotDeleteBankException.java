package com.example.bank_backend.exception;

public class CannotDeleteBankException extends RuntimeException {
    public CannotDeleteBankException(String message) {
        super(message);
    }
}
