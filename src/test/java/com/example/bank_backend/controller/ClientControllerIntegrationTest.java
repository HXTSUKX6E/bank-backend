package com.example.bank_backend.controller;

import com.example.bank_backend.model.Bank;
import com.example.bank_backend.model.Client;
import com.example.bank_backend.model.Deposit;
import com.example.bank_backend.model.LegalForm;
import com.example.bank_backend.repository.BankRepository;
import com.example.bank_backend.repository.ClientRepository;
import com.example.bank_backend.repository.DepositRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ClientControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private DepositRepository depositRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private BankRepository bankRepository;

    @BeforeEach
    void setUp() {
        depositRepository.deleteAll();
        clientRepository.deleteAll();
        bankRepository.deleteAll();
    }

    @Test
    void createClient_ShouldReturnCreatedClient() throws Exception {
        String clientJson = """
            {
                "name": "Иван Иванович Иванов",
                "legalForm": "АО"
            }
            """;

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.notNullValue()));
    }

    @Test
    void createClient_WithDuplicateName_ShouldReturnError() throws Exception {
        clientRepository.save(new Client("Иван Иванович Иванов", "Иван", "Адресс", LegalForm.AO));

        String duplicateClientJson = """
            {
                "name": "Иван Иванович Иванов",
                "shortName": "Иван",
                "address": "Адресс2",
                "legalForm": "ООО"
            }
            """;

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateClientJson))
                .andExpect(status().isConflict());
    }

    @Test
    void createClient_WithoutLegalForm_ShouldReturnBadRequestError() throws Exception {
        String clientJson = """
        {
            "name": "БезФормы",
            "shortName": "БезФормы",
            "address": "Адрес"
        }
        """;

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createClient_WithExceptionInLegalForm_ShouldReturnBadRequestError() throws Exception {
        String clientJson = """
        {
            "name": "БезФормы",
            "shortName": "БезФормы",
            "address": "Адрес",
            "legalForm": "ОЧЕНЬ НЕПОНЯТНАЯ ФОРМА"
        }
        """;

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getClient_WithExistingId_ShouldReturnClient() throws Exception {
        Client client = clientRepository.save(new Client("WW", "ОООООО", "Адрес", LegalForm.IP));

        mockMvc.perform(get("/api/clients/" + client.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.notNullValue()));
    }

    @Test
    void getClient_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/clients/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllClients_ShouldReturnClientsList() throws Exception {
        clientRepository.save(new Client("Нэйм", "Н", "Адрес 1", LegalForm.OOO));
        clientRepository.save(new Client("Нэйм2", "Н", "Адрес 2", LegalForm.AO));

        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.notNullValue()));
    }

    @Test
    void getAllClients_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateClient_WithValid_ShouldReturnUpdatedClient() throws Exception {
        Client client = clientRepository.save(new Client("ЧЕЛ522", "До", "адрес", LegalForm.AO));

        String updateJson = """
        {
            "name": "new имя",
            "legalForm": "ИП"
        }
        """;

        mockMvc.perform(put("/api/clients/" + client.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.notNullValue()));
    }

    @Test
    void updateClient_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        String clientJson = """
        {
            "name": "ФФФФ",
            "shortName": "ФФФФ",
            "address": "ФФФФ",
            "legalForm": "ИП"
        }
        """;

        mockMvc.perform(put("/api/clients/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateClient_WithDuplicateName_ShouldReturnConflict() throws Exception {
        Client client1 = clientRepository.save(new Client("Клиент 1", "К1", "Адрес 1", LegalForm.OOO));
        clientRepository.save(new Client("Клиент 2", "К2", "Адрес 2", LegalForm.IP));

        String updateJson = """
        {
            "name": "Клиент 2",
            "shortName": "К1Нов",
            "address": "Новый адрес",
            "legalForm": "OOO"
        }
        """;

        mockMvc.perform(put("/api/clients/" + client1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteClient_WithExistingId_ShouldReturnOk() throws Exception {
        Client client = clientRepository.save(new Client("Клиент", "Удалить", "удаления", LegalForm.OOO));

        mockMvc.perform(delete("/api/clients/" + client.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/clients/" + client.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteClient_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/clients/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteClient_WithDeposits_ShouldReturnBadRequest() throws Exception {
        Client client = clientRepository.save(new Client("КлиентДепозит", "КД", "Адрес", LegalForm.OOO));
        Bank bank = bankRepository.save(new Bank("БанкДляКлиента", "987654321"));

        depositRepository.save(new Deposit(client, bank, LocalDate.now(), 5.0, 12));

        mockMvc.perform(delete("/api/clients/" + client.getId()))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/clients/" + client.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(client.getId().intValue())));
    }
}