package com.example.bank_backend.controller;

import com.example.bank_backend.exception.ClientAlreadyExistsException;
import com.example.bank_backend.exception.ClientNotFoundException;
import com.example.bank_backend.exception.NoClientsFoundException;
import com.example.bank_backend.model.Client;
import com.example.bank_backend.model.LegalForm;
import com.example.bank_backend.service.ClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@WebMvcTest(ClientController.class)
@ExtendWith(MockitoExtension.class)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createClient_ShouldReturnCreatedClient() throws Exception {
        Client client = new Client("Иван Иванович Иванов", "Иван", "Адрес", LegalForm.AO);
        client.setId(1L);

        when(clientService.createClient(any(Client.class))).thenReturn(client);

        String clientJson = """
            {
                "name": "Иван Иванович Иванов",
                "shortName": "Иван",
                "address": "Адрес",
                "legalForm": "АО"
            }
            """;

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Иван Иванович Иванов"))
                .andExpect(jsonPath("$.legalForm").value("АО"));
    }

    @Test
    void createClient_WithDuplicateName_ShouldReturnError() throws Exception {
        when(clientService.createClient(any(Client.class)))
                .thenThrow(new ClientAlreadyExistsException("Client with this name already exists"));

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

        verify(clientService, never()).createClient(any(Client.class));
    }

    @Test
    void createClient_WithInvalidLegalForm_ShouldReturnBadRequestError() throws Exception {
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

        verify(clientService, never()).createClient(any(Client.class));
    }

    @Test
    void getClient_WithExistingId_ShouldReturnClient() throws Exception {
        Client client = new Client("WW", "ОООООО", "Адрес", LegalForm.IP);
        client.setId(1L);

        when(clientService.findClientById(1L)).thenReturn(client);

        mockMvc.perform(get("/api/clients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("WW"))
                .andExpect(jsonPath("$.legalForm").value("ИП"));
    }

    @Test
    void getClient_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        when(clientService.findClientById(99999L))
                .thenThrow(new ClientNotFoundException("Клиент с ID: 99999 не найден."));

        mockMvc.perform(get("/api/clients/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllClients_ShouldReturnClientsList() throws Exception {
        Client client1 = new Client("Нэйм", "Н", "Адрес 1", LegalForm.OOO);
        client1.setId(1L);
        Client client2 = new Client("Нэйм2", "Н", "Адрес 2", LegalForm.AO);
        client2.setId(2L);

        when(clientService.findAllClients(isNull(), isNull(), isNull(), isNull(), anyString(), anyString()))
                .thenReturn(List.of(client1, client2));

        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Нэйм"))
                .andExpect(jsonPath("$[1].name").value("Нэйм2"));
    }

    @Test
    void getAllClients_ShouldReturnNotFoundWhenEmpty() throws Exception {
        when(clientService.findAllClients(isNull(), isNull(), isNull(), isNull(), anyString(), anyString()))
                .thenThrow(new NoClientsFoundException("Список клиентов пуст."));

        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllClients_WithNameFilter_ShouldReturnFilteredClients() throws Exception {
        Client client = new Client("Иван", "Ив", "Москва", LegalForm.OOO);
        client.setId(1L);

        when(clientService.findAllClients(eq("Иван"), isNull(), isNull(), isNull(), anyString(), anyString()))
                .thenReturn(List.of(client));

        mockMvc.perform(get("/api/clients")
                        .param("name", "Иван"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Иван"));
    }

    @Test
    void getAllClients_WithShortNameFilter_ShouldReturnFilteredClients() throws Exception {
        Client client = new Client("Иван Иванов", "Ив", "Москва", LegalForm.OOO);
        client.setId(1L);

        when(clientService.findAllClients(isNull(), eq("Ив"), isNull(), isNull(), anyString(), anyString()))
                .thenReturn(List.of(client));

        mockMvc.perform(get("/api/clients")
                        .param("shortName", "Ив"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].shortName").value("Ив"));
    }

    @Test
    void getAllClients_WithAddressFilter_ShouldReturnFilteredClients() throws Exception {
        Client client = new Client("Иван Иванов", "Ив", "Москва", LegalForm.OOO);
        client.setId(1L);

        when(clientService.findAllClients(isNull(), isNull(), eq("Москва"), isNull(), anyString(), anyString()))
                .thenReturn(List.of(client));

        mockMvc.perform(get("/api/clients")
                        .param("address", "Москва"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].address").value("Москва"));
    }

    @Test
    void getAllClients_WithLegalFormFilter_ShouldReturnFilteredClients() throws Exception {
        Client client = new Client("Иван Иванов", "Ив", "Москва", LegalForm.OOO);
        client.setId(1L);

        when(clientService.findAllClients(isNull(), isNull(), isNull(), eq(LegalForm.OOO), anyString(), anyString()))
                .thenReturn(List.of(client));

        mockMvc.perform(get("/api/clients")
                        .param("legalForm", "ООО"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].legalForm").value("ООО"));
    }

    @Test
    void getAllClients_WithMultipleFilters_ShouldReturnFilteredClients() throws Exception {
        Client client = new Client("Иван Иванов", "Ив", "Москва", LegalForm.OOO);
        client.setId(1L);

        when(clientService.findAllClients(eq("Иван"), eq("Ив"), eq("Москва"), eq(LegalForm.OOO), anyString(), anyString()))
                .thenReturn(List.of(client));

        mockMvc.perform(get("/api/clients")
                        .param("name", "Иван")
                        .param("shortName", "Ив")
                        .param("address", "Москва")
                        .param("legalForm", "ООО"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Иван Иванов"));
    }

    @Test
    void updateClient_WithValid_ShouldReturnUpdatedClient() throws Exception {
        Client updatedClient = new Client("new имя", "Нов", "новый адрес", LegalForm.IP);
        updatedClient.setId(1L);

        when(clientService.updateClient(eq(1L), any(Client.class))).thenReturn(updatedClient);

        String updateJson = """
        {
            "name": "new имя",
            "shortName": "Нов",
            "address": "новый адрес",
            "legalForm": "ИП"
        }
        """;

        mockMvc.perform(put("/api/clients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("new имя"))
                .andExpect(jsonPath("$.legalForm").value("ИП"));
    }

    @Test
    void updateClient_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        when(clientService.updateClient(eq(999L), any(Client.class)))
                .thenThrow(new ClientNotFoundException("Клиент с ID: 999 не найден."));

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
        when(clientService.updateClient(anyLong(), any(Client.class)))
                .thenThrow(new ClientAlreadyExistsException("Client with this name already exists"));

        String updateJson = """
        {
            "name": "Клиент 2",
            "shortName": "К1Нов",
            "address": "Новый адрес",
            "legalForm": "ООО"
        }
        """;

        mockMvc.perform(put("/api/clients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteClient_WithExistingId_ShouldReturnNoContent() throws Exception {
        doNothing().when(clientService).deleteClient(1L);

        mockMvc.perform(delete("/api/clients/1"))
                .andExpect(status().isNoContent());

        verify(clientService).deleteClient(1L);
    }

    @Test
    void deleteClient_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        doThrow(new ClientNotFoundException("Клиент с ID: 99 не найден."))
                .when(clientService).deleteClient(99L);

        mockMvc.perform(delete("/api/clients/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteClient_WithDeposits_ShouldReturnBadRequest() throws Exception {
        doThrow(new IllegalStateException("Cannot delete client with existing deposits"))
                .when(clientService).deleteClient(1L);

        mockMvc.perform(delete("/api/clients/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllClients_WithSorting_ShouldReturnSortedClients() throws Exception {
        Client client1 = new Client("Альфа", "А", "Адрес 1", LegalForm.OOO);
        Client client2 = new Client("Бета", "Б", "Адрес 2", LegalForm.IP);

        when(clientService.findAllClients(isNull(), isNull(), isNull(), isNull(), eq("name"), eq("desc")))
                .thenReturn(List.of(client2, client1));

        mockMvc.perform(get("/api/clients")
                        .param("sortBy", "name")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Бета"))
                .andExpect(jsonPath("$[1].name").value("Альфа"));
    }
}