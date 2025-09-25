package com.example.bank_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "banks")

@Getter
@Setter
public class Bank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "bik", nullable = false, unique = true, length = 9)
    private String bik;

    public Bank() {
    }

    public Bank(String name, String bik) {
        this.name = name;
        this.bik = bik;
    }
}
