package com.example.bank_backend.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record DepositRequest(
        @NotNull(message = "Укажите ID клиента")
        Long clientId,

        @NotNull(message = "Укажите ID банка")
        Long bankId,

        @NotNull(message = "Укажите дату открытия")
        @PastOrPresent(message = "Дата открытия должна быть сегодня или раньше")
        LocalDate openingDate,

        @NotNull(message = "Укажите процентную ставку")
        @DecimalMin(value = "0.01", message = "Процентная ставка должна быть от 0.01%")
        //@DecimalMax(value = "100.0", message = "Процентная ставка не может быть больше 100%")
        Double percentage,

        @NotNull(message = "Укажите срок вклада")
        @Min(value = 1, message = "Минимальный срок - 1 месяц")
        @Max(value = 300, message = "Максимальный срок - 300 месяцев (25 лет)")
        Integer termMonths
) {}
