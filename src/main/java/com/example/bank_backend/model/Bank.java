package com.example.bank_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "Наименование банка обязательно")
    @Size(max = 255, message = "Наименование банка не может превышать 255 символов")
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @NotBlank(message = "БИК банка обязателен")
    @Pattern(regexp = "\\d{9}", message = "БИК должен состоять из 9 цифр")
    @Size(min = 9, max = 9, message = "БИК должен состоять из 9 цифр")
    @Column(name = "bik", nullable = false, unique = true, length = 9)
    private String bik;

    public Bank() {
    }

    public Bank(String name, String bik) {
        this.name = name;
        this.bik = bik;
    }
}
