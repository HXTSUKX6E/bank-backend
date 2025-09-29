package com.example.bank_backend.controller;

import com.example.bank_backend.exception.BankAlreadyExistsException;
import com.example.bank_backend.exception.BankNotFoundException;
import com.example.bank_backend.exception.NoBanksFoundException;
import com.example.bank_backend.model.Bank;
import com.example.bank_backend.service.BankService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BankController.class)
@ExtendWith(MockitoExtension.class)
class BankControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BankService bankService;

    @Test
    void createBank_ShouldReturnCreatedBank() throws Exception {
        Bank bank = new Bank("ААА Банк", "123456789");
        bank.setId(1L);

        when(bankService.createBank(any(Bank.class))).thenReturn(bank);

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
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("ААА Банк"))
                .andExpect(jsonPath("$.bik").value("123456789"));
    }

    @Test
    void createBank_WithDuplicateName_ShouldReturnError() throws Exception {
        when(bankService.createBank(any(Bank.class)))
                .thenThrow(new BankAlreadyExistsException("Bank with this name already exists"));

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

        verify(bankService, never()).createBank(any(Bank.class));
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

        verify(bankService, never()).createBank(any(Bank.class));
    }

    @Test
    void getBank_WithExistingId_ShouldReturnBank() throws Exception {
        Bank bank = new Bank("TestBank", "333333333");
        bank.setId(1L);

        when(bankService.findBankById(1L)).thenReturn(bank);

        mockMvc.perform(get("/api/banks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("TestBank"))
                .andExpect(jsonPath("$.bik").value("333333333"));
    }

    @Test
    void getBank_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        when(bankService.findBankById(99999L))
                .thenThrow(new BankNotFoundException("Банк с ID: 99999 не найден."));

        mockMvc.perform(get("/api/banks/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllBanks_ShouldReturnBanksList() throws Exception {
        Bank bank1 = new Bank("Банк1", "444444444");
        bank1.setId(1L);
        Bank bank2 = new Bank("Банк2", "555555555");
        bank2.setId(2L);

        when(bankService.findAllBanks(isNull(), isNull(), anyString(), anyString()))
                .thenReturn(List.of(bank1, bank2));

        mockMvc.perform(get("/api/banks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Банк1"))
                .andExpect(jsonPath("$[1].name").value("Банк2"));
    }

    @Test
    void getAllBanks_WithFilters_ShouldReturnFilteredBanks() throws Exception {
        Bank bank = new Bank("Сбербанк", "044525225");
        bank.setId(1L);

        when(bankService.findAllBanks(eq("Сбер"), eq("044525"), anyString(), anyString()))
                .thenReturn(List.of(bank));

        mockMvc.perform(get("/api/banks")
                        .param("name", "Сбер")
                        .param("bik", "044525"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Сбербанк"));
    }

    @Test
    void getAllBanks_ShouldReturnEmptyList() throws Exception {
        when(bankService.findAllBanks(isNull(), isNull(), anyString(), anyString()))
                .thenThrow(new NoBanksFoundException("Список банков пуст."));

        mockMvc.perform(get("/api/banks"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateBank_WithValid_ShouldReturnUpdatedBank() throws Exception {
        Bank updatedBank = new Bank("UpdateBankNew", "777777777");
        updatedBank.setId(1L);

        when(bankService.updateBank(eq(1L), any(Bank.class))).thenReturn(updatedBank);

        String updateJson = """
            {
                "name": "UpdateBankNew",
                "bik": "777777777"
            }
            """;

        mockMvc.perform(put("/api/banks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("UpdateBankNew"))
                .andExpect(jsonPath("$.bik").value("777777777"));
    }

    @Test
    void updateBank_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        when(bankService.updateBank(eq(999L), any(Bank.class)))
                .thenThrow(new BankNotFoundException("Банк с ID: 999 не найден."));

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
        when(bankService.updateBank(anyLong(), any(Bank.class)))
                .thenThrow(new BankAlreadyExistsException("Bank with this name already exists"));

        String updateJson = """
        {
            "name": "БанкБ",
            "bik": "222333444"
        }
        """;

        mockMvc.perform(put("/api/banks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isConflict());
    }
    @Test
    void deleteBank_WithExistingId_ShouldReturnNoContent() throws Exception {
        doNothing().when(bankService).deleteBank(1L);

        mockMvc.perform(delete("/api/banks/1"))
                .andExpect(status().isNoContent());

        verify(bankService).deleteBank(1L);
    }

    @Test
    void deleteBank_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        doThrow(new BankNotFoundException("Банк с ID: 99 не найден."))
                .when(bankService).deleteBank(99L);

        mockMvc.perform(delete("/api/banks/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteBank_WithDeposits_ShouldReturnBadRequest() throws Exception {
        doThrow(new IllegalStateException("Cannot delete bank with existing deposits"))
                .when(bankService).deleteBank(1L);

        mockMvc.perform(delete("/api/banks/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllBanks_WithSorting_ShouldReturnSortedBanks() throws Exception {
        Bank bank1 = new Bank("Альфа", "111111111");
        Bank bank2 = new Bank("Бета", "222222222");

        when(bankService.findAllBanks(isNull(), isNull(), eq("name"), eq("desc")))
                .thenReturn(List.of(bank2, bank1));

        mockMvc.perform(get("/api/banks")
                        .param("sortBy", "name")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}