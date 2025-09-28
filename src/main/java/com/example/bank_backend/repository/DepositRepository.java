package com.example.bank_backend.repository;

import com.example.bank_backend.model.Deposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DepositRepository extends JpaRepository<Deposit, Long>, JpaSpecificationExecutor<Deposit> {
    boolean existsByClientId(Long id);

    long countByClientId(Long clientId);
}
