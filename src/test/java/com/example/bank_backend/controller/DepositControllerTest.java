package com.example.bank_backend.controller;

import com.example.bank_backend.dto.DepositRequest;
import com.example.bank_backend.exception.NoDepositsFoundException;
import com.example.bank_backend.model.Bank;
import com.example.bank_backend.model.Client;
import com.example.bank_backend.model.Deposit;
import com.example.bank_backend.model.LegalForm;
import com.example.bank_backend.service.DepositService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DepositController.class)
@ExtendWith(MockitoExtension.class)
class DepositControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DepositService depositService;

    @Autowired
    private ObjectMapper objectMapper;

    private Client createTestClient() {
        Client client = new Client("Иван Иванов", "aa", "fff", LegalForm.AO);
        client.setId(1L);
        return client;
    }

    private Bank createTestBank() {
        Bank bank = new Bank("БанкТест", "123456789");
        bank.setId(1L);
        return bank;
    }

    private Deposit createTestDeposit() {
        Deposit deposit = new Deposit(createTestClient(), createTestBank(), LocalDate.now(), 5.5, 12);
        deposit.setId(1L);
        return deposit;
    }

    private DepositRequest createTestDepositRequest() {
        return new DepositRequest(1L, 1L, LocalDate.now(), 5.5, 12);
    }

    @Test
    void createDeposit_ShouldReturnCreatedDeposit() throws Exception {
        Deposit deposit = createTestDeposit();
        DepositRequest depositRequest = createTestDepositRequest();

        when(depositService.createDeposit(any(DepositRequest.class))).thenReturn(deposit);

        String depositJson = """
            {
              "clientId": %d,
              "bankId": %d,
              "openingDate": "%s",
              "percentage": 5.5,
              "termMonths": 12
            }
            """.formatted(1L, 1L, LocalDate.now());

        mockMvc.perform(post("/api/deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(depositJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.client.id").value(1))
                .andExpect(jsonPath("$.bank.id").value(1))
                .andExpect(jsonPath("$.percentage").value(5.5))
                .andExpect(jsonPath("$.termMonths").value(12));
    }

    @Test
    void getDepositById_ShouldReturnDeposit() throws Exception {
        Deposit deposit = createTestDeposit();

        when(depositService.findDepositById(1L)).thenReturn(deposit);

        mockMvc.perform(get("/api/deposits/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.percentage").value(5.5))
                .andExpect(jsonPath("$.termMonths").value(12));
    }

    @Test
    void getDepositById_NotFound_ShouldReturn404() throws Exception {
        when(depositService.findDepositById(9999L))
                .thenThrow(new NoDepositsFoundException("Депозит с ID: 9999 не найден."));

        mockMvc.perform(get("/api/deposits/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllDeposits_ShouldReturnList() throws Exception {
        Deposit deposit1 = createTestDeposit();
        Deposit deposit2 = createTestDeposit();
        deposit2.setId(2L);
        deposit2.setPercentage(7.0);

        when(depositService.findAllDeposits(
                isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(),
                anyString(), anyString()
        )).thenReturn(List.of(deposit1, deposit2));

        mockMvc.perform(get("/api/deposits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].percentage").value(5.5))
                .andExpect(jsonPath("$[1].percentage").value(7.0));
    }

    @Test
    void getAllDeposits_ShouldReturn404WhenEmpty() throws Exception {
        when(depositService.findAllDeposits(
                isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(),
                anyString(), anyString()
        )).thenThrow(new NoDepositsFoundException("Депозиты с указанными критериями не найдены"));

        mockMvc.perform(get("/api/deposits"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllDeposits_FilterByClientId_ShouldReturnFiltered() throws Exception {
        Deposit deposit = createTestDeposit();

        when(depositService.findAllDeposits(
                eq(1L), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(),
                anyString(), anyString()
        )).thenReturn(List.of(deposit));

        mockMvc.perform(get("/api/deposits")
                        .param("clientId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].client.id").value(1));
    }

    @Test
    void getAllDeposits_FilterByBankId_ShouldReturnFiltered() throws Exception {
        Deposit deposit = createTestDeposit();

        when(depositService.findAllDeposits(
                isNull(), eq(1L), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(),
                anyString(), anyString()
        )).thenReturn(List.of(deposit));

        mockMvc.perform(get("/api/deposits")
                        .param("bankId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].bank.id").value(1));
    }

    @Test
    void getAllDeposits_FilterByMinPercentage_ShouldReturnFiltered() throws Exception {
        Deposit deposit = createTestDeposit();
        deposit.setPercentage(8.5);

        when(depositService.findAllDeposits(
                isNull(), isNull(), isNull(), isNull(),
                eq(5.0), isNull(), isNull(), isNull(),
                anyString(), anyString()
        )).thenReturn(List.of(deposit));

        mockMvc.perform(get("/api/deposits")
                        .param("minPercentage", "5.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].percentage").value(8.5));
    }

    @Test
    void getAllDeposits_FilterByMaxPercentage_ShouldReturnFiltered() throws Exception {
        Deposit deposit = createTestDeposit();
        deposit.setPercentage(4.5);

        when(depositService.findAllDeposits(
                isNull(), isNull(), isNull(), isNull(),
                isNull(), eq(5.0), isNull(), isNull(),
                anyString(), anyString()
        )).thenReturn(List.of(deposit));

        mockMvc.perform(get("/api/deposits")
                        .param("maxPercentage", "5.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].percentage").value(4.5));
    }

    @Test
    void getAllDeposits_FilterByMinTerm_ShouldReturnFiltered() throws Exception {
        Deposit deposit = createTestDeposit();
        deposit.setTermMonths(24);

        when(depositService.findAllDeposits(
                isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), eq(12), isNull(),
                anyString(), anyString()
        )).thenReturn(List.of(deposit));

        mockMvc.perform(get("/api/deposits")
                        .param("minTerm", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].termMonths").value(24));
    }

    @Test
    void getAllDeposits_FilterByMaxTerm_ShouldReturnFiltered() throws Exception {
        Deposit deposit = createTestDeposit();
        deposit.setTermMonths(6);

        when(depositService.findAllDeposits(
                isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), eq(12),
                anyString(), anyString()
        )).thenReturn(List.of(deposit));

        mockMvc.perform(get("/api/deposits")
                        .param("maxTerm", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].termMonths").value(6));
    }

    @Test
    void getAllDeposits_FilterByDateFrom_ShouldReturnFiltered() throws Exception {
        Deposit deposit = createTestDeposit();
        LocalDate testDate = LocalDate.now().minusDays(5);

        when(depositService.findAllDeposits(
                isNull(), isNull(), eq(testDate), isNull(),
                isNull(), isNull(), isNull(), isNull(),
                anyString(), anyString()
        )).thenReturn(List.of(deposit));

        mockMvc.perform(get("/api/deposits")
                        .param("openingDateFrom", testDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].percentage").value(5.5));
    }

    @Test
    void getAllDeposits_FilterByDateTo_ShouldReturnFiltered() throws Exception {
        Deposit deposit = createTestDeposit();
        LocalDate testDate = LocalDate.now().plusDays(1);

        when(depositService.findAllDeposits(
                isNull(), isNull(), isNull(), eq(testDate),
                isNull(), isNull(), isNull(), isNull(),
                anyString(), anyString()
        )).thenReturn(List.of(deposit));

        mockMvc.perform(get("/api/deposits")
                        .param("openingDateTo", testDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void updateDeposit_ShouldReturnUpdatedDeposit() throws Exception {
        Deposit updatedDeposit = createTestDeposit();
        updatedDeposit.setPercentage(6.0);
        updatedDeposit.setTermMonths(24);

        when(depositService.updateDeposit(eq(1L), any(DepositRequest.class))).thenReturn(updatedDeposit);

        String updateJson = """
            {
              "clientId": %d,
              "bankId": %d,
              "openingDate": "%s",
              "percentage": 6.0,
              "termMonths": 24
            }
            """.formatted(1L, 1L, LocalDate.now());

        mockMvc.perform(put("/api/deposits/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.percentage").value(6.0))
                .andExpect(jsonPath("$.termMonths").value(24));
    }

    @Test
    void updateDeposit_NotFound_ShouldReturn404() throws Exception {
        when(depositService.updateDeposit(eq(9999L), any(DepositRequest.class)))
                .thenThrow(new NoDepositsFoundException("Депозит с ID: 9999 не найден."));

        String updateJson = """
            {
              "clientId": %d,
              "bankId": %d,
              "openingDate": "%s",
              "percentage": 5.0,
              "termMonths": 12
            }
            """.formatted(1L, 1L, LocalDate.now());

        mockMvc.perform(put("/api/deposits/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteDeposit_ShouldReturnNoContent() throws Exception {
        doNothing().when(depositService).deleteDeposit(1L);

        mockMvc.perform(delete("/api/deposits/1"))
                .andExpect(status().isNoContent());

        verify(depositService).deleteDeposit(1L);
    }

    @Test
    void deleteDeposit_NotFound_ShouldReturn404() throws Exception {
        doThrow(new NoDepositsFoundException("Депозит с ID: 9999 не найден."))
                .when(depositService).deleteDeposit(9999L);

        mockMvc.perform(delete("/api/deposits/9999"))
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
            """.formatted(1L, 1L, LocalDate.now());

        mockMvc.perform(post("/api/deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(depositJson))
                .andExpect(status().isBadRequest());

        verify(depositService, never()).createDeposit(any(DepositRequest.class));
    }

    @Test
    void getAllDeposits_WithSorting_ShouldReturnSortedDeposits() throws Exception {
        Deposit deposit1 = createTestDeposit();
        Deposit deposit2 = createTestDeposit();
        deposit2.setId(2L);
        deposit2.setPercentage(3.5);

        when(depositService.findAllDeposits(
                isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(),
                eq("percentage"), eq("desc")
        )).thenReturn(List.of(deposit1, deposit2));

        mockMvc.perform(get("/api/deposits")
                        .param("sortBy", "percentage")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].percentage").value(5.5))
                .andExpect(jsonPath("$[1].percentage").value(3.5));
    }

    @Test
    void getAllDeposits_WithMultipleFilters_ShouldReturnFiltered() throws Exception {
        Deposit deposit = createTestDeposit();
        LocalDate testDate = LocalDate.now().minusDays(10);

        when(depositService.findAllDeposits(
                eq(1L), eq(1L), eq(testDate), isNull(),
                eq(5.0), eq(10.0), eq(6), eq(24),
                anyString(), anyString()
        )).thenReturn(List.of(deposit));

        mockMvc.perform(get("/api/deposits")
                        .param("clientId", "1")
                        .param("bankId", "1")
                        .param("openingDateFrom", testDate.toString())
                        .param("minPercentage", "5.0")
                        .param("maxPercentage", "10.0")
                        .param("minTerm", "6")
                        .param("maxTerm", "24"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].percentage").value(5.5));
    }
}