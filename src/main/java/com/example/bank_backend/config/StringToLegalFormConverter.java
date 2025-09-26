package com.example.bank_backend.config;

import com.example.bank_backend.model.LegalForm;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToLegalFormConverter implements Converter<String, LegalForm> {
    @Override
    public LegalForm convert(String source) {
        try {
            return LegalForm.fromString(source);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Некорректная юридическая форма: " + source + ". Допустимые значения: ООО, АО, ПТ, КТ, ПК, ИП, ПАО, ГУП, МУП");
        }
    }
}