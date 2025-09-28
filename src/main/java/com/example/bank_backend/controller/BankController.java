package com.example.bank_backend.controller;

import com.example.bank_backend.exception.NoBanksFoundException;
import com.example.bank_backend.model.Bank;
import com.example.bank_backend.service.BankService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/banks")
public class BankController {

    private final BankService bankService;

    @Autowired
    public BankController(BankService bankService) {
        this.bankService = bankService;
    }


    // Получить все банки (поиск + фильтрация)
    @GetMapping
    public List<Bank> getAllBanks(@RequestParam(required = false) String name,
                                  @RequestParam(required = false) String bik,
                                  @RequestParam(defaultValue = "id") String sortBy,
                                  @RequestParam(defaultValue = "asc") String direction
    ) {
        List<Bank> banks = bankService.findAllBanks(name, bik, sortBy, direction);
        if (banks.isEmpty()) {
            throw new NoBanksFoundException("Банки с указанными критериями не найдены");
        }
        return banks;
    }

    // Получить банк по ID
    @GetMapping("/{id}")
    public Bank getClientById(@PathVariable Long id) {
        return bankService.findBankById(id);
    }

    // Добавить банк
    @PostMapping
    public Bank createBank(@Valid @RequestBody Bank bank) {
        return bankService.createBank(bank);
    };

    // Обновить (изменить) банк по ID
    @PutMapping("/{id}")
    public Bank updateBank(@PathVariable Long id, @Valid @RequestBody Bank bankDetails) {
        return bankService.updateBank(id, bankDetails);
    }

    // Удалить банк по ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBank(@PathVariable Long id) {
        try {
            bankService.deleteBank(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
