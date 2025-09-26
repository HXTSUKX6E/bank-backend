package com.example.bank_backend.repository;

import com.example.bank_backend.model.Bank;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankRepository extends JpaRepository<Bank, Long> {
}
