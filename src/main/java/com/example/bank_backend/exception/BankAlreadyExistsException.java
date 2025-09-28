package com.example.bank_backend.exception;

public class BankAlreadyExistsException extends RuntimeException {
    public BankAlreadyExistsException(String message) {
        super(message);
    }

//    public BankAlreadyExistsException(String name, String bik) {
//        super("Банк с наименованием '" + name + "' или БИК '" + bik + "' уже существует");
//    }

}