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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DepositControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private DepositRepository depositRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private BankRepository bankRepository;
    private Client client;
    private Bank bank;

    // Для депозитов Клиент + Банк
    @BeforeEach
    void setup() {
        depositRepository.deleteAll();
        clientRepository.deleteAll();
        bankRepository.deleteAll();

        client = clientRepository.save(new Client("Иван Иванов", "aa", "fff", LegalForm.AO));
        bank = bankRepository.save(new Bank("БанкТест", "123456789"));
    }

    @Test
    void createDeposit_ShouldReturnCreatedDeposit() throws Exception {
        String depositJson = """
            {
              "clientId": %d,
              "bankId": %d,
              "openingDate": "%s",
              "percentage": 5.5,
              "termMonths": 12
            }
            """.formatted(client.getId(), bank.getId(), LocalDate.now());

        mockMvc.perform(post("/api/deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(depositJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.client.id", is(client.getId().intValue())))
                .andExpect(jsonPath("$.bank.id", is(bank.getId().intValue())))
                .andExpect(jsonPath("$.percentage", is(5.5)))
                .andExpect(jsonPath("$.termMonths", is(12)));
    }

    @Test
    void getDepositById_ShouldReturnDeposit() throws Exception {
        Deposit deposit = depositRepository.save(new Deposit(
                client, bank, LocalDate.now(), 4.2, 24
        ));

        mockMvc.perform(get("/api/deposits/{id}", deposit.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(deposit.getId().intValue())))
                .andExpect(jsonPath("$.percentage", is(4.2)))
                .andExpect(jsonPath("$.termMonths", is(24)));
    }

    @Test
    void getDepositById_NotFound_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/deposits/{id}", 9999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllDeposits_ShouldReturnList() throws Exception {
        depositRepository.save(new Deposit(client, bank, LocalDate.now(), 3.5, 6));
        depositRepository.save(new Deposit(client, bank, LocalDate.now().minusDays(10), 7.0, 36));

        mockMvc.perform(get("/api/deposits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getAllDeposits_ShouldReturn404WhenEmpty() throws Exception {
        mockMvc.perform(get("/api/deposits"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllDeposits_FilterByPercentage_ShouldReturnFiltered() throws Exception {
        depositRepository.save(new Deposit(client, bank, LocalDate.now(), 2.5, 6));
        depositRepository.save(new Deposit(client, bank, LocalDate.now(), 8.5, 12));

        mockMvc.perform(get("/api/deposits")
                        .param("minPercentage", "5.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].percentage", is(8.5)));
    }

    @Test
    void getAllDeposits_FilterByDate_ShouldReturnFiltered() throws Exception {
        depositRepository.save(new Deposit(client, bank, LocalDate.now().minusDays(30), 4.5, 10));
        depositRepository.save(new Deposit(client, bank, LocalDate.now(), 6.0, 12));

        mockMvc.perform(get("/api/deposits")
                        .param("openingDateFrom", LocalDate.now().minusDays(5).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].percentage", is(6.0)));
    }

    @Test
    void getAllDeposits_FilterByTerm_ShouldReturnFiltered() throws Exception {
        depositRepository.save(new Deposit(client, bank, LocalDate.now(), 5.0, 6));
        depositRepository.save(new Deposit(client, bank, LocalDate.now(), 5.0, 24));

        mockMvc.perform(get("/api/deposits")
                        .param("minTerm", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].termMonths", is(24)));
    }

    @Test
    void updateDeposit_ShouldReturnUpdatedDeposit() throws Exception {
        Deposit deposit = depositRepository.save(new Deposit(client, bank, LocalDate.now(), 4.5, 18));

        String updateJson = """
            {
              "clientId": %d,
              "bankId": %d,
              "openingDate": "%s",
              "percentage": 6.0,
              "termMonths": 24
            }
            """.formatted(client.getId(), bank.getId(), LocalDate.now());

        mockMvc.perform(put("/api/deposits/{id}", deposit.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.percentage", is(6.0)))
                .andExpect(jsonPath("$.termMonths", is(24)));
    }

    @Test
    void updateDeposit_NotFound_ShouldReturn404() throws Exception {
        String updateJson = """
            {
              "clientId": %d,
              "bankId": %d,
              "openingDate": "%s",
              "percentage": 5.0,
              "termMonths": 12
            }
            """.formatted(client.getId(), bank.getId(), LocalDate.now());

        mockMvc.perform(put("/api/deposits/{id}", 9999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteDeposit_ShouldReturnNoContent() throws Exception {
        Deposit deposit = depositRepository.save(new Deposit(client, bank, LocalDate.now(), 5.5, 12));

        mockMvc.perform(delete("/api/deposits/{id}", deposit.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteDeposit_NotFound_ShouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/deposits/{id}", 9999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void createDeposit_InvalidData_ShouldReturnBadRequest() throws Exception {
        String depositJson = """
            {
              "clientId": %d,
              "bankId": %d,
              "openingDate": "%s",
              "percentage": 0.0,
              "termMonths": 0
            }
            """.formatted(client.getId(), bank.getId(), LocalDate.now());

        mockMvc.perform(post("/api/deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(depositJson))
                .andExpect(status().isBadRequest());
    }
}