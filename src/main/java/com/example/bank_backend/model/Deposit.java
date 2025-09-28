package com.example.bank_backend.model;

import java.time.LocalDate;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "deposits")
@Getter
@Setter
public class Deposit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    @NotNull(message = "Клиент не может быть пустым")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "bank_id", nullable = false)
    @NotNull(message = "Банк не может быть пустым")
    private Bank bank;

    @Column(name = "opening_date", nullable = false)
    @NotNull(message = "Дата открытия не может быть пустой")
    private LocalDate openingDate;

    @Column(name = "percentage", nullable = false)
    @DecimalMin(value = "0.01", message = "Процент должен быть больше 0.01%")
    // @DecimalMax(value = "100.0", message = "Процент должен быть меньше 100%")
    private Double percentage;

    @Column(name = "term_months", nullable = false)
    @Min(value = 1, message = "Срок должен быть не менее 1 месяца")
    @Max(value = 600, message = "Срок не может быть больше 300 месяцев (25 лет)") // random
    private Integer termMonths;

    public Deposit() {
    }

    public Deposit(Client client, Bank bank, LocalDate openingDate, Double percentage, Integer termMonths) {
        this.client = client;
        this.bank = bank;
        this.openingDate = openingDate;
        this.percentage = percentage;
        this.termMonths = termMonths;
    }
}
