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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DepositServiceTest {

    @Mock
    private DepositRepository depositRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private BankRepository bankRepository;

    @InjectMocks
    private DepositService depositService;

    @Test
    void findAllDeposits_ShouldReturnAllDeposits() {
        Client client = createTestClient(1L, "ААА Клиент");
        Bank bank = createTestBank(1L, "БББ Банк", "111111111");
        Deposit deposit1 = createTestDeposit(1L, client, bank, LocalDate.now().minusDays(10), 5.5, 12);
        Deposit deposit2 = createTestDeposit(2L, client, bank, LocalDate.now().minusDays(5), 6.0, 24);

        List<Deposit> deposits = Arrays.asList(deposit1, deposit2);

        given(depositRepository.findAll(any(Sort.class))).willReturn(deposits);

        List<Deposit> result = depositService.findAllDeposits(
                null, null, null, null, null, null, null, null, "id", "asc"
        );

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
    }

    @Test
    void findAllDeposits_WithClientFilter_ShouldReturnFilteredDeposits() {
        Client client1 = createTestClient(1L, "Клиент ААА");
        Client client2 = createTestClient(2L, "Клиент БББ");
        Bank bank = createTestBank(1L, "ВВВ Банк", "222222222");
        Deposit deposit1 = createTestDeposit(1L, client1, bank, LocalDate.now(), 4.5, 6);
        Deposit deposit2 = createTestDeposit(2L, client2, bank, LocalDate.now(), 5.5, 12);

        List<Deposit> allDeposits = Arrays.asList(deposit1, deposit2);

        given(depositRepository.findAll(any(Sort.class))).willReturn(allDeposits);

        List<Deposit> result = depositService.findAllDeposits(
                1L, null, null, null, null, null, null, null, "id", "asc"
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getClient().getId()).isEqualTo(1L);
        assertThat(result.get(0).getClient().getName()).isEqualTo("Клиент ААА");
    }

    @Test
    void findAllDeposits_WithBankFilter_ShouldReturnFilteredDeposits() {
        Client client = createTestClient(1L, "ККК Клиент");
        Bank bank1 = createTestBank(1L, "Банк ААА", "111111111");
        Bank bank2 = createTestBank(2L, "Банк БББ", "222222222");
        Deposit deposit1 = createTestDeposit(1L, client, bank1, LocalDate.now(), 4.0, 6);
        Deposit deposit2 = createTestDeposit(2L, client, bank2, LocalDate.now(), 5.0, 12);

        List<Deposit> allDeposits = Arrays.asList(deposit1, deposit2);

        given(depositRepository.findAll(any(Sort.class))).willReturn(allDeposits);
        List<Deposit> result = depositService.findAllDeposits(
                null, 1L, null, null, null, null, null, null, "id", "asc"
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getBank().getId()).isEqualTo(1L);
        assertThat(result.get(0).getBank().getName()).isEqualTo("Банк ААА");
    }

    @Test
    void findAllDeposits_WithDateFilters_ShouldReturnFilteredDeposits() {
        // Given
        Client client = createTestClient(1L, "Клиент ДДД");
        Bank bank = createTestBank(1L, "Банк ЕЕЕ", "333333333");

        LocalDate date1 = LocalDate.of(2024, 1, 1);
        LocalDate date2 = LocalDate.of(2024, 2, 1);
        LocalDate date3 = LocalDate.of(2024, 3, 1);

        Deposit deposit1 = createTestDeposit(1L, client, bank, date1, 4.5, 6);
        Deposit deposit2 = createTestDeposit(2L, client, bank, date2, 5.5, 12);
        Deposit deposit3 = createTestDeposit(3L, client, bank, date3, 6.5, 24);

        List<Deposit> allDeposits = Arrays.asList(deposit1, deposit2, deposit3);
        given(depositRepository.findAll(any(Sort.class))).willReturn(allDeposits);

        List<Deposit> result = depositService.findAllDeposits(
                null, null, LocalDate.of(2024, 1, 15), LocalDate.of(2024, 2, 15),
                null, null, null, null, "id", "asc"
        );
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(2L);
        assertThat(result.get(0).getOpeningDate()).isEqualTo(date2);
    }

    @Test
    void findAllDeposits_WithPercentageFilters_ShouldReturnFilteredDeposits() {
        Client client = createTestClient(1L, "Клиент ЖЖЖ");
        Bank bank = createTestBank(1L, "Банк ЗЗЗ", "444444444");
        Deposit deposit1 = createTestDeposit(1L, client, bank, LocalDate.now(), 4.5, 6);
        Deposit deposit2 = createTestDeposit(2L, client, bank, LocalDate.now(), 5.5, 12);
        Deposit deposit3 = createTestDeposit(3L, client, bank, LocalDate.now(), 6.5, 24);
        List<Deposit> allDeposits = Arrays.asList(deposit1, deposit2, deposit3);

        given(depositRepository.findAll(any(Sort.class))).willReturn(allDeposits);

        List<Deposit> result = depositService.findAllDeposits(
                null, null, null, null, 5.0, 6.0, null, null, "id", "asc"
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(2L);
        assertThat(result.get(0).getPercentage()).isEqualTo(5.5);
    }

    @Test
    void findAllDeposits_WithTermFilters_ShouldReturnFilteredDeposits() {
        Client client = createTestClient(1L, "Клиент ИИИ");
        Bank bank = createTestBank(1L, "Банк ККК", "555555555");

        Deposit deposit1 = createTestDeposit(1L, client, bank, LocalDate.now(), 4.0, 6);
        Deposit deposit2 = createTestDeposit(2L, client, bank, LocalDate.now(), 5.0, 12);
        Deposit deposit3 = createTestDeposit(3L, client, bank, LocalDate.now(), 6.0, 24);
        List<Deposit> allDeposits = Arrays.asList(deposit1, deposit2, deposit3);

        given(depositRepository.findAll(any(Sort.class))).willReturn(allDeposits);

        List<Deposit> result = depositService.findAllDeposits(
                null, null, null, null, null, null, 10, 18, "id", "asc"
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(2L);
        assertThat(result.get(0).getTermMonths()).isEqualTo(12);
    }

    @Test
    void findAllDeposits_WhenNoDepositsFound_ShouldThrowException() {
        given(depositRepository.findAll(any(Sort.class))).willReturn(Collections.emptyList());
        assertThatThrownBy(() -> depositService.findAllDeposits(
                null, null, null, null, null, null, null, null, "id", "asc"
        ))
                .isInstanceOf(NoDepositsFoundException.class)
                .hasMessage("Депозиты с указанными критериями не найдены");
    }

    @Test
    void findDepositById_WithExistingId_ShouldReturnDeposit() {
        Client client = createTestClient(1L, "Клиент ЛЛЛ");
        Bank bank = createTestBank(1L, "Банк МММ", "666666666");
        Deposit deposit = createTestDeposit(1L, client, bank, LocalDate.now(), 5.0, 12);
        given(depositRepository.findById(1L)).willReturn(Optional.of(deposit));

        Deposit result = depositService.findDepositById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getClient().getName()).isEqualTo("Клиент ЛЛЛ");
        assertThat(result.getBank().getName()).isEqualTo("Банк МММ");
    }

    @Test
    void findDepositById_WithNonExistingId_ShouldThrowException() {
        given(depositRepository.findById(999L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> depositService.findDepositById(999L))
                .isInstanceOf(ClientNotFoundException.class)
                .hasMessage("Депозита с ID: 999 не существует.");
    }

    @Test
    void createDeposit_WithValidData_ShouldSaveAndReturnDeposit() {
        Client client = createTestClient(1L, "Клиент ННН");
        Bank bank = createTestBank(1L, "Банк ООО", "777777777");

        DepositRequest request = new DepositRequest(1L, 1L, LocalDate.now(), 5.5, 12);
        Deposit deposit = createTestDeposit(1L, client, bank, LocalDate.now(), 5.5, 12);

        given(clientRepository.findById(1L)).willReturn(Optional.of(client));
        given(bankRepository.findById(1L)).willReturn(Optional.of(bank));
        given(depositRepository.save(any(Deposit.class))).willReturn(deposit);

        // When
        Deposit result = depositService.createDeposit(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPercentage()).isEqualTo(5.5);
        assertThat(result.getTermMonths()).isEqualTo(12);
        assertThat(result.getOpeningDate()).isNotNull();
        assertThat(result.getClient().getName()).isEqualTo("Клиент ННН");
        assertThat(result.getBank().getName()).isEqualTo("Банк ООО");
        verify(depositRepository).save(any(Deposit.class));
    }

    @Test
    void createDeposit_WithNonExistingClient_ShouldThrowException() {
        // Given
        // Исправлено: добавлен openingDate в конструктор
        DepositRequest request = new DepositRequest(999L, 1L, LocalDate.now(), 5.5, 12);
        given(clientRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> depositService.createDeposit(request))
                .isInstanceOf(ClientNotFoundException.class)
                .hasMessage("Клиент не найден с ID: 999");

        verify(depositRepository, never()).save(any(Deposit.class));
    }

    @Test
    void createDeposit_WithNonExistingBank_ShouldThrowException() {
        // Given
        Client client = createTestClient(1L, "Клиент ППП");
        // Исправлено: добавлен openingDate в конструктор
        DepositRequest request = new DepositRequest(1L, 999L, LocalDate.now(), 5.5, 12);

        given(clientRepository.findById(1L)).willReturn(Optional.of(client));
        given(bankRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> depositService.createDeposit(request))
                .isInstanceOf(BankNotFoundException.class)
                .hasMessage("Банк не найден с ID: 999");

        verify(depositRepository, never()).save(any(Deposit.class));
    }

    @Test
    void updateDeposit_WithValidData_ShouldUpdateAndReturnDeposit() {
        // Given
        Client client = createTestClient(1L, "Клиент РРР");
        Bank bank = createTestBank(1L, "Банк ССС", "888888888");
        LocalDate openingDate = LocalDate.of(2024, 1, 1);

        Deposit existingDeposit = createTestDeposit(1L, client, bank, LocalDate.now(), 4.0, 6);

        // Исправлено: правильный конструктор с openingDate
        DepositRequest request = new DepositRequest(1L, 1L, openingDate, 6.0, 24);
        Deposit updatedDeposit = createTestDeposit(1L, client, bank, openingDate, 6.0, 24);

        given(depositRepository.findById(1L)).willReturn(Optional.of(existingDeposit));
        given(clientRepository.findById(1L)).willReturn(Optional.of(client));
        given(bankRepository.findById(1L)).willReturn(Optional.of(bank));
        given(depositRepository.save(existingDeposit)).willReturn(updatedDeposit);

        // When
        Deposit result = depositService.updateDeposit(1L, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPercentage()).isEqualTo(6.0);
        assertThat(result.getTermMonths()).isEqualTo(24);
        assertThat(result.getOpeningDate()).isEqualTo(openingDate);
        assertThat(result.getClient().getName()).isEqualTo("Клиент РРР");
        assertThat(result.getBank().getName()).isEqualTo("Банк ССС");
        verify(depositRepository).save(existingDeposit);
    }

    @Test
    void updateDeposit_WithNonExistingDeposit_ShouldThrowException() {
        // Ищем клиент и банк
        Client client = createTestClient(1L, "Клиент РРР");
        Bank bank = createTestBank(1L, "Банк ССС", "888888888");
        given(clientRepository.findById(1L)).willReturn(Optional.of(client));
        given(bankRepository.findById(1L)).willReturn(Optional.of(bank));
        given(depositRepository.findById(999L)).willReturn(Optional.empty());

        DepositRequest request = new DepositRequest(1L, 1L, LocalDate.now(), 5.5, 12);

        assertThatThrownBy(() -> depositService.updateDeposit(999L, request))
                .isInstanceOf(ClientNotFoundException.class)
                .hasMessage("Депозита с ID: 999 не существует.");
    }

    @Test
    void deleteDeposit_WithExistingId_ShouldDeleteDeposit() {
        Client client = createTestClient(1L, "Клиент ТТТ");
        Bank bank = createTestBank(1L, "Банк УУУ", "999999999");
        Deposit deposit = createTestDeposit(1L, client, bank, LocalDate.now(), 5.0, 12);

        given(depositRepository.findById(1L)).willReturn(Optional.of(deposit));
        depositService.deleteDeposit(1L);

        verify(depositRepository).delete(deposit);
    }

    @Test
    void deleteDeposit_WithNonExistingId_ShouldThrowException() {
        given(depositRepository.findById(999L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> depositService.deleteDeposit(999L))
                .isInstanceOf(ClientNotFoundException.class)
                .hasMessage("Депозита с ID: 999 не существует.");

        verify(depositRepository, never()).delete(any(Deposit.class));
    }

    // методы-конструкторы для создания тест. данных

    private Client createTestClient(Long id, String name) {
        Client client = new Client();
        client.setId(id);
        client.setName(name);
        return client;
    }

    private Bank createTestBank(Long id, String name, String bik) {
        Bank bank = new Bank();
        bank.setId(id);
        bank.setName(name);
        bank.setBik(bik);
        return bank;
    }

    private Deposit createTestDeposit(Long id, Client client, Bank bank, LocalDate openingDate, Double percentage, Integer termMonths) {
        Deposit deposit = new Deposit();
        deposit.setId(id);
        deposit.setClient(client);
        deposit.setBank(bank);
        deposit.setOpeningDate(openingDate);
        deposit.setPercentage(percentage);
        deposit.setTermMonths(termMonths);
        return deposit;
    }
}