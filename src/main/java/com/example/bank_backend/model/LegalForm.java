package com.example.bank_backend.model;

import com.fasterxml.jackson.annotation.JsonValue;
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

    private final String name;

    LegalForm(String name) {
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return name;
    }

    public static LegalForm fromString(String value) {
        for (LegalForm form : values()) {
            if (form.name.equals(value) || form.name().equalsIgnoreCase(value)) {
                return form;
            }
        }
        throw new IllegalArgumentException("Некорректная форма: " + value);
    }
}
