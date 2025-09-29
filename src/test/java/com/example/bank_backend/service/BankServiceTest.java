package com.example.bank_backend.service;

import com.example.bank_backend.exception.*;
import com.example.bank_backend.model.Bank;
import com.example.bank_backend.repository.BankRepository;
import com.example.bank_backend.repository.DepositRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BankServiceTest {

    @Mock
    private BankRepository bankRepository;

    @Mock
    private DepositRepository depositRepository;

    @InjectMocks
    private BankService bankService;

    @Test
    void findAllBanks_ShouldReturnAllBanks() {
        Bank bank1 = new Bank("Тест Банк 1", "111111111");
        bank1.setId(1L);
        Bank bank2 = new Bank("Тест Банк 2", "222222222");
        bank2.setId(2L);

        List<Bank> banks = Arrays.asList(bank1, bank2);

        given(bankRepository.findAll(any(Sort.class))).willReturn(banks);

        List<Bank> result = bankService.findAllBanks(null, null, "name", "asc");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Тест Банк 1");
        assertThat(result.get(1).getName()).isEqualTo("Тест Банк 2");
    }

    @Test
    void findAllBanks_WithNameFilter_ShouldReturnFilteredBanks() {
        Bank bank1 = new Bank("ААА Банк", "111111111");
        bank1.setId(1L);
        Bank bank2 = new Bank("БББ Банк", "222222222");
        bank2.setId(2L);

        List<Bank> allBanks = Arrays.asList(bank1, bank2);

        given(bankRepository.findAll(any(Sort.class))).willReturn(allBanks);

        List<Bank> result = bankService.findAllBanks("ААА", null, "name", "asc");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("ААА Банк");
    }

    @Test
    void findAllBanks_WithBikFilter_ShouldReturnFilteredBanks() {
        Bank bank1 = new Bank("Банк 1", "123456789");
        bank1.setId(1L);
        Bank bank2 = new Bank("Банк 2", "987654321");
        bank2.setId(2L);

        List<Bank> allBanks = Arrays.asList(bank1, bank2);

        given(bankRepository.findAll(any(Sort.class))).willReturn(allBanks);

        List<Bank> result = bankService.findAllBanks(null, "123", "name", "asc");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBik()).isEqualTo("123456789");
    }

    @Test
    void findAllBanks_WithSorting_ShouldReturnSortedBanks() {
        Bank bank1 = new Bank("Банк А", "111111111");
        bank1.setId(1L);
        Bank bank2 = new Bank("Банк Б", "222222222");
        bank2.setId(2L);

        List<Bank> banks = Arrays.asList(bank1, bank2);

        given(bankRepository.findAll(any(Sort.class))).willReturn(banks);

        List<Bank> result = bankService.findAllBanks(null, null, "name", "desc");

        assertThat(result).hasSize(2);
        verify(bankRepository).findAll(Sort.by("name").descending());
    }

    @Test
    void findAllBanks_WhenNoBanksFound_ShouldThrowException() {
        given(bankRepository.findAll(any(Sort.class))).willReturn(Collections.emptyList());

        assertThatThrownBy(() -> bankService.findAllBanks(null, null, "name", "asc"))
                .isInstanceOf(NoBanksFoundException.class)
                .hasMessage("Список банков пуст.");
    }

    @Test
    void findBankById_WithExistingId_ShouldReturnBank() {
        Bank bank = new Bank("Тест Банк", "123456789");
        bank.setId(1L);
        given(bankRepository.findById(1L)).willReturn(Optional.of(bank));

        Bank result = bankService.findBankById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Тест Банк");
        assertThat(result.getBik()).isEqualTo("123456789");
    }

    @Test
    void findBankById_WithNonExistingId_ShouldThrowException() {
        given(bankRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> bankService.findBankById(999L))
                .isInstanceOf(BankNotFoundException.class)
                .hasMessage("Банк с ID: 999 не найден.");
    }

    @Test
    void createBank_WithValidData_ShouldSaveAndReturnBank() {
        Bank newBank = new Bank("Новый Банк", "555555555");
        Bank savedBank = new Bank("Новый Банк", "555555555");
        savedBank.setId(1L);

        given(bankRepository.existsByName("Новый Банк")).willReturn(false);
        given(bankRepository.existsByBik("555555555")).willReturn(false);
        given(bankRepository.save(newBank)).willReturn(savedBank);
        Bank result = bankService.createBank(newBank);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Новый Банк");
        verify(bankRepository).save(newBank);
    }

    @Test
    void createBank_WithDuplicateName_ShouldThrowException() {
        Bank newBank = new Bank("Дубль Банк", "123456789");
        given(bankRepository.existsByName("Дубль Банк")).willReturn(true);

        assertThatThrownBy(() -> bankService.createBank(newBank))
                .isInstanceOf(BankAlreadyExistsException.class)
                .hasMessage("Дубль Банк");
        verify(bankRepository, never()).save(any(Bank.class));
    }

    @Test
    void createBank_WithDuplicateBik_ShouldThrowException() {
        Bank newBank = new Bank("Уникальный Банк", "111111111");
        given(bankRepository.existsByName("Уникальный Банк")).willReturn(false);
        given(bankRepository.existsByBik("111111111")).willReturn(true);

        assertThatThrownBy(() -> bankService.createBank(newBank))
                .isInstanceOf(BankAlreadyExistsException.class)
                .hasMessage("111111111");

        verify(bankRepository, never()).save(any(Bank.class));
    }

    @Test
    void updateBank_WithValidData_ShouldUpdateAndReturnBank() {
        Bank existingBank = new Bank("Старый Банк", "111111111");
        existingBank.setId(1L);
        Bank bankDetails = new Bank("Новое Название", "999999999");

        given(bankRepository.findById(1L)).willReturn(Optional.of(existingBank));
        given(bankRepository.save(existingBank)).willReturn(existingBank);
        Bank result = bankService.updateBank(1L, bankDetails);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Новое Название");
        assertThat(result.getBik()).isEqualTo("999999999");
        verify(bankRepository).save(existingBank);
    }

    @Test
    void updateBank_WithNonExistingId_ShouldThrowException() {
        Bank bankDetails = new Bank("Банк", "123456789");
        given(bankRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> bankService.updateBank(999L, bankDetails))
                .isInstanceOf(BankNotFoundException.class)
                .hasMessage("Банк с ID: 999 не найден.");
    }

    @Test
    void deleteBank_WithExistingIdAndNoDeposits_ShouldDeleteBank() {
        Bank bank = new Bank("Банк для удаления", "123456789");
        bank.setId(1L);
        given(bankRepository.findById(1L)).willReturn(Optional.of(bank));
        given(depositRepository.existsByBankId(1L)).willReturn(false);
        bankService.deleteBank(1L);

        verify(bankRepository).delete(bank);
    }

    @Test
    void deleteBank_WithNonExistingId_ShouldThrowException() {
        given(bankRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> bankService.deleteBank(999L))
                .isInstanceOf(BankNotFoundException.class)
                .hasMessage("Банк с ID: 999 не найден.");
        verify(bankRepository, never()).delete(any(Bank.class));
    }

    @Test
    void deleteBank_WithExistingDeposits_ShouldThrowException() {
        Bank bank = new Bank("Банк с депозитами", "123456789");
        bank.setId(1L);
        given(bankRepository.findById(1L)).willReturn(Optional.of(bank));
        given(depositRepository.existsByBankId(1L)).willReturn(true);

        assertThatThrownBy(() -> bankService.deleteBank(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Нельзя удалить банк с депозитами");
        verify(bankRepository, never()).delete(any(Bank.class));
    }
}