package com.example.bank_backend.service;

import com.example.bank_backend.dto.DepositRequest;
import com.example.bank_backend.exception.BankNotFoundException;
import com.example.bank_backend.exception.ClientNotFoundException;
import com.example.bank_backend.exception.NoDepositsFoundException;
import com.example.bank_backend.model.Bank;
import com.example.bank_backend.model.Client;
import com.example.bank_backend.model.Deposit;
import com.example.bank_backend.repository.BankRepository;
import com.example.bank_backend.repository.ClientRepository;
import com.example.bank_backend.repository.DepositRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DepositService {

    private final DepositRepository depositRepository;
    private final ClientRepository clientRepository;
    private final BankRepository bankRepository;

    @Autowired
    public DepositService(DepositRepository depositRepository, ClientRepository clientRepository, BankRepository bankRepository) {
        this.depositRepository = depositRepository;
        this.clientRepository = clientRepository;
        this.bankRepository = bankRepository;
    }

    public List<Deposit> findAllDeposits(
            Long clientId, Long bankId,
            LocalDate openingDateFrom, LocalDate openingDateTo,
            Double minPercentage, Double maxPercentage,
            Integer minTerm, Integer maxTerm,
            String sortBy, String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        List<Deposit> deposits = depositRepository.findAll(sort).stream()
                .filter(d -> (clientId == null || d.getClient().getId().equals(clientId)))
                .filter(d -> (bankId == null || d.getBank().getId().equals(bankId)))
                .filter(d -> (openingDateFrom == null || !d.getOpeningDate().isBefore(openingDateFrom)))
                .filter(d -> (openingDateTo == null || !d.getOpeningDate().isAfter(openingDateTo)))
                .filter(d -> (minPercentage == null || d.getPercentage() >= minPercentage))
                .filter(d -> (maxPercentage == null || d.getPercentage() <= maxPercentage))
                .filter(d -> (minTerm == null || d.getTermMonths() >= minTerm))
                .filter(d -> (maxTerm == null || d.getTermMonths() <= maxTerm))
                .toList();

        if (deposits.isEmpty()) {
            throw new NoDepositsFoundException("Депозиты с указанными критериями не найдены");
        }

        return deposits;
    }

    public Deposit findDepositById(long id) {
        return depositRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException("Депозита с ID: " + id + " не существует."));
    }

    public Deposit createDeposit(DepositRequest depositRequest) {
        Client client = findClientById(depositRequest.clientId());
        Bank bank = findBankById(depositRequest.bankId());

        Deposit deposit = new Deposit();

        deposit.setClient(client); // клиент
        deposit.setBank(bank); // банк
        deposit.setOpeningDate(LocalDate.now());
        deposit.setPercentage(depositRequest.percentage());
        deposit.setTermMonths(depositRequest.termMonths());

        return depositRepository.save(deposit);
    }

    public Deposit updateDeposit(Long id, DepositRequest depositRequest) {

        Client client = findClientById(depositRequest.clientId());
        Bank bank = findBankById(depositRequest.bankId());

        Deposit deposit = findDepositById(id);

        deposit.setClient(client);
        deposit.setBank(bank);
        deposit.setOpeningDate(depositRequest.openingDate());
        deposit.setPercentage(depositRequest.percentage());
        deposit.setTermMonths(depositRequest.termMonths());

        return depositRepository.save(deposit);
    }

    public void deleteDeposit(Long id) {
        Deposit deposit = findDepositById(id);
        depositRepository.delete(deposit);
    }


    // методы получения ID банка и клиента

    private Client findClientById(Long clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException("Клиент не найден с ID: " + clientId));
    }

    private Bank findBankById(Long bankId) {
        return bankRepository.findById(bankId)
                .orElseThrow(() -> new BankNotFoundException("Банк не найден с ID: " + bankId));
    }
}
