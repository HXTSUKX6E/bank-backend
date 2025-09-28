package com.example.bank_backend.service;

import com.example.bank_backend.exception.*;
import com.example.bank_backend.model.Bank;
import com.example.bank_backend.repository.BankRepository;
import com.example.bank_backend.repository.DepositRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BankService {

    private final BankRepository bankRepository;
    private final DepositRepository depositRepository;

    @Autowired
    public BankService(BankRepository bankRepository, DepositRepository depositRepository) {
        this.bankRepository = bankRepository;
        this.depositRepository = depositRepository;
    }

    public List<Bank> findAllBanks(String name, String bik, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        List<Bank> banks = bankRepository.findAll(sort).stream()
                .filter(b -> (name == null || b.getName().toLowerCase().contains(name.toLowerCase())))
                .filter(b -> (bik == null || b.getBik().contains(bik)))
                .toList();

        if (banks.isEmpty()) {
            throw new NoBanksFoundException("Список банков пуст.");
        }

        return banks;
    }

    public Bank findBankById(Long id) {
        return bankRepository.findById(id)
                .orElseThrow(() -> new BankNotFoundException("Банк с ID: " + id + " не найден."));
    }

    public Bank createBank(Bank bank) {
        if (bankRepository.existsByName(bank.getName())) {
            throw new BankAlreadyExistsException(bank.getName());
        }
        if (bankRepository.existsByBik(bank.getBik())) {
            throw new BankAlreadyExistsException(bank.getBik());
        }
        return bankRepository.save(bank);
    }

    public Bank updateBank(Long id, Bank bankDetails) {
        Bank bank = findBankById(id);

        bank.setName(bankDetails.getName());
        bank.setBik(bankDetails.getBik());
        return bankRepository.save(bank);
    }

    public void deleteBank(Long bakId) {
        Bank bank = findBankById(bakId);

        boolean hasDeposits = depositRepository.existsByBankId(bakId);

        if (hasDeposits) {
            throw new IllegalStateException("Нельзя удалить банк с депозитами");
        }

        bankRepository.delete(bank);
    }
}
