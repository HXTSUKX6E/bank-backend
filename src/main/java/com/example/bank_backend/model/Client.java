package com.example.bank_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "clients")

@Getter
@Setter
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "short_name", length = 100)
    private String shortName;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "legal_form", nullable = false, length = 10)
    private LegalForm legalForm;

    public Client() {
    }

    public Client(String name, String shortName, String address, LegalForm legalForm) {
        this.name = name;
        this.shortName = shortName;
        this.address = address;
        this.legalForm = legalForm;
    }
}
