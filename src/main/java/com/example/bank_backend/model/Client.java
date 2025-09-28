package com.example.bank_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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

    @NotBlank(message = "Наименование клиента обязательно")
    @Size(max = 255, message = "Наименование клиента не может быть длиннее 255 символов")
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 100, message = "Краткое наименование не может быть длиннее 100 символов")
    @Column(name = "short_name", length = 100)
    private String shortName;

    @Size(max = 500, message = "Адрес не может быть длиннее 500 символов")
    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @NotNull(message = "Правовая форма обязательна")
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