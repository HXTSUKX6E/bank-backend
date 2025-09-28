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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BankControllerIntegrationTest {

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
        depositRepository.deleteAll(); // сначала депозиты
        clientRepository.deleteAll();
        bankRepository.deleteAll();
    }

    @Test
    void createBank_ShouldReturnCreatedBank() throws Exception {
        String bankJson = """
            {
                "name": "ААА Банк",
                "bik": "123456789"
            }
            """;

        mockMvc.perform(post("/api/banks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bankJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.notNullValue()));
    }

    @Test
    void createBank_WithDuplicateName_ShouldReturnError() throws Exception {
        bankRepository.save(new Bank("БанкТест1", "111111111"));

        String duplicateJson = """
            {
                "name": "БанкТест1",
                "bik": "222222222"
            }
            """;

        mockMvc.perform(post("/api/banks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateJson))
                .andExpect(status().isConflict());
    }

    @Test
    void createBank_WithInvalidBik_ShouldReturnBadRequest() throws Exception {
        String invalidJson = """
            {
                "name": "ФигняБанк",
                "bik": "12ABC"
            }
            """;

        mockMvc.perform(post("/api/banks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBank_WithoutBik_ShouldReturnBadRequest() throws Exception {
        String invalidJson = """
            {
                "name": "БезБикБанк"
            }
            """;

        mockMvc.perform(post("/api/banks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBank_WithExistingId_ShouldReturnBank() throws Exception {
        Bank bank = bankRepository.save(new Bank("TestBank", "333333333"));

        mockMvc.perform(get("/api/banks/" + bank.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.notNullValue()));
    }

    @Test
    void getBank_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/banks/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllBanks_ShouldReturnBanksList() throws Exception {
        bankRepository.save(new Bank("Банк1", "444444444"));
        bankRepository.save(new Bank("Банк2", "555555555"));

        mockMvc.perform(get("/api/banks"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.notNullValue()));
    }

    @Test
    void getAllBanks_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/banks"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateBank_WithValid_ShouldReturnUpdatedBank() throws Exception {
        Bank bank = bankRepository.save(new Bank("UpdateBank", "666666666"));

        String updateJson = """
            {
                "name": "UpdateBankNew",
                "bik": "777777777"
            }
            """;

        mockMvc.perform(put("/api/banks/" + bank.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.notNullValue()));
    }

    @Test
    void updateBank_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        String bankJson = """
            {
                "name": "НеСуществующий",
                "bik": "888888888"
            }
            """;

        mockMvc.perform(put("/api/banks/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bankJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateBank_WithDuplicateName_ShouldReturnConflict() throws Exception {
        Bank bank1 = bankRepository.save(new Bank("БанкА", "999999999"));
        bankRepository.save(new Bank("БанкБ", "111222333"));

        String updateJson = """
            {
                "name": "БанкБ",
                "bik": "222333444"
            }
            """;

        mockMvc.perform(put("/api/banks/" + bank1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteBank_WithExistingId_ShouldReturnNoContent() throws Exception {
        Bank bank = bankRepository.save(new Bank("УдалитьБанк", "121212121"));

        mockMvc.perform(delete("/api/banks/" + bank.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/banks/" + bank.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteBank_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/banks/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteBank_WithDeposits_ShouldReturnBadRequest() throws Exception {
        Client client = clientRepository.save(new Client("Клиент", "К1", "Адрес", LegalForm.OOO));
        Bank bank = bankRepository.save(new Bank("БанкУдалить", "987654321"));

        depositRepository.save(new Deposit(client, bank, LocalDate.now(), 5.0, 12));

        mockMvc.perform(delete("/api/banks/{id}", bank.getId()))
                .andExpect(status().isBadRequest());
    }
}