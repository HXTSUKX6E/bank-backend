package com.example.bank_backend.model;

import java.time.LocalDate;
import jakarta.persistence.*;

@Entity
@Table(name = "deposits")
public class Deposit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "bank_id", nullable = false)
    private Bank bank;

    @Column(name = "opening_date", nullable = false)
    private LocalDate openingDate;

    @Column(name = "percentage", nullable = false)
    private Double percentage;

    @Column(name = "term_months", nullable = false)
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
