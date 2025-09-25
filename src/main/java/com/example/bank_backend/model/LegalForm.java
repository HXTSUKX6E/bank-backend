package com.example.bank_backend.model;

import lombok.Getter;

@Getter
public enum LegalForm {
    OOO("ООО"),
    AO("АО"),
    PT("ПТ"),
    KT("КТ"),
    PK("ПК"),
    IP("ИП"),
    PAO("ПАО"),
    GUP("ГУП"),
    MUP("МУП");

    private final String description;

    LegalForm(String description) {
        this.description = description;
    }
}
