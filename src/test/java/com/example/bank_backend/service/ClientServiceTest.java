package com.example.bank_backend.service;

import com.example.bank_backend.exception.ClientAlreadyExistsException;
import com.example.bank_backend.exception.ClientNotFoundException;
import com.example.bank_backend.exception.NoClientsFoundException;
import com.example.bank_backend.model.Client;
import com.example.bank_backend.model.LegalForm;
import com.example.bank_backend.repository.ClientRepository;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private DepositRepository depositRepository;

    @InjectMocks
    private ClientService clientService;

    @Test
    void findAllClients_ShouldReturnAllClients() {
        Client client1 = createTestClient(1L, "ААА Клиент", "ААА", "Адрес ААА", LegalForm.OOO);
        Client client2 = createTestClient(2L, "БББ Клиент", "БББ", "Адрес БББ", LegalForm.AO);
        List<Client> clients = Arrays.asList(client1, client2);
        given(clientRepository.findAll(any(Sort.class))).willReturn(clients);

        List<Client> result = clientService.findAllClients(null, null, null, null, "name", "asc");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("ААА Клиент");
        assertThat(result.get(1).getName()).isEqualTo("БББ Клиент");
    }

    @Test
    void findAllClients_WithNameFilter_ShouldReturnFilteredClients() {
        Client client1 = createTestClient(1L, "Клиент ААА", "ААА", "Адрес 1", LegalForm.OOO);
        Client client2 = createTestClient(2L, "Клиент БББ", "БББ", "Адрес 2", LegalForm.AO);
        List<Client> allClients = Arrays.asList(client1, client2);
        given(clientRepository.findAll(any(Sort.class))).willReturn(allClients);

        List<Client> result = clientService.findAllClients("ААА", null, null, null, "name", "asc");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Клиент ААА");
        assertThat(result.get(0).getShortName()).isEqualTo("ААА");
    }

    @Test
    void findAllClients_WithShortNameFilter_ShouldReturnFilteredClients() {
        Client client1 = createTestClient(1L, "Клиент 1", "ААА", "Адрес 1", LegalForm.OOO);
        Client client2 = createTestClient(2L, "Клиент 2", "БББ", "Адрес 2", LegalForm.AO);
        List<Client> allClients = Arrays.asList(client1, client2);
        given(clientRepository.findAll(any(Sort.class))).willReturn(allClients);

        List<Client> result = clientService.findAllClients(null, "БББ", null, null, "name", "asc");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Клиент 2");
        assertThat(result.get(0).getShortName()).isEqualTo("БББ");
    }

    @Test
    void findAllClients_WithAddressFilter_ShouldReturnFilteredClients() {
        Client client1 = createTestClient(1L, "Клиент 1", "ААА", "Адрес ААА", LegalForm.OOO);
        Client client2 = createTestClient(2L, "Клиент 2", "БББ", "Адрес БББ", LegalForm.AO);
        List<Client> allClients = Arrays.asList(client1, client2);
        given(clientRepository.findAll(any(Sort.class))).willReturn(allClients);

        List<Client> result = clientService.findAllClients(null, null, "Адрес БББ", null, "name", "asc");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Клиент 2");
        assertThat(result.get(0).getAddress()).isEqualTo("Адрес БББ");
    }

    @Test
    void findAllClients_WithLegalFormFilter_ShouldReturnFilteredClients() {
        Client client1 = createTestClient(1L, "Клиент 1", "ААА", "Адрес 1", LegalForm.OOO);
        Client client2 = createTestClient(2L, "Клиент 2", "БББ", "Адрес 2", LegalForm.AO);
        Client client3 = createTestClient(3L, "Клиент 3", "ВВВ", "Адрес 3", LegalForm.IP);
        List<Client> allClients = Arrays.asList(client1, client2, client3);
        given(clientRepository.findAll(any(Sort.class))).willReturn(allClients);

        List<Client> result = clientService.findAllClients(null, null, null, LegalForm.AO, "name", "asc");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Клиент 2");
        assertThat(result.get(0).getLegalForm()).isEqualTo(LegalForm.AO);
    }

    @Test
    void findAllClients_WithSorting_ShouldReturnSortedClients() {
        Client client1 = createTestClient(1L, "БББ Клиент", "БББ", "Адрес 1", LegalForm.OOO);
        Client client2 = createTestClient(2L, "ААА Клиент", "ААА", "Адрес 2", LegalForm.AO);
        List<Client> clients = Arrays.asList(client1, client2);
        given(clientRepository.findAll(any(Sort.class))).willReturn(clients);

        List<Client> result = clientService.findAllClients(null, null, null, null, "name", "desc");

        assertThat(result).hasSize(2);
        verify(clientRepository).findAll(Sort.by("name").descending());
    }

    @Test
    void findAllClients_WhenNoClientsFound_ShouldThrowException() {
        given(clientRepository.findAll(any(Sort.class))).willReturn(Collections.emptyList());
        assertThatThrownBy(() -> clientService.findAllClients(null, null, null, null, "name", "asc"))
                .isInstanceOf(NoClientsFoundException.class)
                .hasMessage("Список клиентов пуст.");
    }

    @Test
    void findClientById_WithExistingId_ShouldReturnClient() {
        Client client = createTestClient(1L, "Тест Клиент", "ТК", "Тест Адрес", LegalForm.IP);
        given(clientRepository.findById(1L)).willReturn(Optional.of(client));

        Client result = clientService.findClientById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Тест Клиент");
        assertThat(result.getShortName()).isEqualTo("ТК");
        assertThat(result.getLegalForm()).isEqualTo(LegalForm.IP);
    }

    @Test
    void findClientById_WithNonExistingId_ShouldThrowException() {
        given(clientRepository.findById(999L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> clientService.findClientById(999L))
                .isInstanceOf(ClientNotFoundException.class)
                .hasMessage("Клиент с ID: 999 не найден.");
    }

    @Test
    void createClient_WithValidData_ShouldSaveAndReturnClient() {
        Client newClient = createTestClient(null, "Новый Клиент", "НК", "Новый Адрес", LegalForm.OOO);
        Client savedClient = createTestClient(1L, "Новый Клиент", "НК", "Новый Адрес", LegalForm.OOO);
        given(clientRepository.existsByName("Новый Клиент")).willReturn(false);
        given(clientRepository.save(newClient)).willReturn(savedClient);

        Client result = clientService.createClient(newClient);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Новый Клиент");
        verify(clientRepository).save(newClient);
    }

    @Test
    void createClient_WithDuplicateName_ShouldThrowException() {
        Client newClient = createTestClient(null, "Дубль Клиент", "ДК", "Адрес", LegalForm.AO);
        given(clientRepository.existsByName("Дубль Клиент")).willReturn(true);

        assertThatThrownBy(() -> clientService.createClient(newClient))
                .isInstanceOf(ClientAlreadyExistsException.class)
                .hasMessage("Клиент с именем 'Дубль Клиент' уже существует.");
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void updateClient_WithValidData_ShouldUpdateAndReturnClient() {
        Client existingClient = createTestClient(1L, "Старый Клиент", "СК", "Старый Адрес", LegalForm.OOO);
        Client clientDetails = createTestClient(null, "Новое Имя", "НИ", "Новый Адрес", LegalForm.AO);
        given(clientRepository.findById(1L)).willReturn(Optional.of(existingClient));
        given(clientRepository.save(existingClient)).willReturn(existingClient);

        Client result = clientService.updateClient(1L, clientDetails);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Новое Имя");
        assertThat(result.getShortName()).isEqualTo("НИ");
        assertThat(result.getAddress()).isEqualTo("Новый Адрес");
        assertThat(result.getLegalForm()).isEqualTo(LegalForm.AO);
        verify(clientRepository).save(existingClient);
    }

    @Test
    void updateClient_WithNonExistingId_ShouldThrowException() {
        Client clientDetails = createTestClient(null, "Клиент", "К", "Адрес", LegalForm.IP);
        given(clientRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.updateClient(999L, clientDetails))
                .isInstanceOf(ClientNotFoundException.class)
                .hasMessage("Клиент с ID: 999 не найден.");
    }

    @Test
    void deleteClient_WithExistingIdAndNoDeposits_ShouldDeleteClient() {
        Client client = createTestClient(1L, "Клиент для удаления", "УД", "Адрес", LegalForm.OOO);
        given(clientRepository.findById(1L)).willReturn(Optional.of(client));
        given(depositRepository.existsByClientId(1L)).willReturn(false);
        clientService.deleteClient(1L);
        verify(clientRepository).delete(client);
    }

    @Test
    void deleteClient_WithNonExistingId_ShouldThrowException() {
        given(clientRepository.findById(999L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> clientService.deleteClient(999L))
                .isInstanceOf(ClientNotFoundException.class)
                .hasMessage("Клиент с ID: 999 не найден.");

        verify(clientRepository, never()).delete(any(Client.class));
    }

    @Test
    void deleteClient_WithExistingDeposits_ShouldThrowException() {
        Client client = createTestClient(1L, "Клиент с депозитами", "КСД", "Адрес", LegalForm.AO);
        given(clientRepository.findById(1L)).willReturn(Optional.of(client));
        given(depositRepository.existsByClientId(1L)).willReturn(true);

        assertThatThrownBy(() -> clientService.deleteClient(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Нельзя удалить клиента с депозитами");

        verify(clientRepository, never()).delete(any(Client.class));
    }

    private Client createTestClient(Long id, String name, String shortName, String address, LegalForm legalForm) {
        Client client = new Client();
        if (id != null) {
            client.setId(id);
        }
        client.setName(name);
        client.setShortName(shortName);
        client.setAddress(address);
        client.setLegalForm(legalForm);
        return client;
    }
}