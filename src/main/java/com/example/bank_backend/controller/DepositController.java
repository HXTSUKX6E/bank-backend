package com.example.bank_backend.controller;

import com.example.bank_backend.dto.DepositRequest;
import com.example.bank_backend.exception.NoDepositsFoundException;
import com.example.bank_backend.model.Client;
import com.example.bank_backend.model.Deposit;
import com.example.bank_backend.service.DepositService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/deposits")
public class DepositController {

    private final DepositService depositService;

    @Autowired
    public DepositController(DepositService depositService) {
        this.depositService = depositService;
    }

    // Получить все депозиты (поиск + фильтрация)
    @GetMapping
    public List<Deposit> getAllDeposits(
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long bankId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate openingDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate openingDateTo,
            @RequestParam(required = false) Double minPercentage,
            @RequestParam(required = false) Double maxPercentage,
            @RequestParam(required = false) Integer minTerm,
            @RequestParam(required = false) Integer maxTerm,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        List<Deposit> deposits = depositService.findAllDeposits(
                clientId, bankId, openingDateFrom, openingDateTo,
                minPercentage, maxPercentage, minTerm, maxTerm,
                sortBy, direction
        );

        if (deposits.isEmpty()) {
            throw new NoDepositsFoundException("Депозиты с указанными критериями не найдены");
        }
        return deposits;
    }

    // Получить депозит по ID
    @GetMapping("/{id}")
    public Deposit getDepositById(@PathVariable Long id) {
        return depositService.findDepositById(id);
    }

    // Создать депозит
    @PostMapping
    public Deposit createDeposit(@Valid @RequestBody DepositRequest depositRequest) {
        return depositService.createDeposit(depositRequest);
    }

    @PutMapping("/{id}")
    public Deposit updateDeposit(@PathVariable Long id, @Valid @RequestBody DepositRequest DepositRequestDetails) {
        return depositService.updateDeposit(id, DepositRequestDetails);
    }

    // Удалить клиента по ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeposit(@PathVariable Long id) {
        try {
            depositService.deleteDeposit(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) { // клиент с депозитами
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
