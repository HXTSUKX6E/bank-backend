package com.example.bank_backend.controller;

import com.example.bank_backend.exception.NoClientsFoundException;
import com.example.bank_backend.model.Client;
import com.example.bank_backend.model.LegalForm;
import com.example.bank_backend.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;

    @Autowired
    public ClientController(ClientService clientService)
    {
        this.clientService = clientService;
    }

    // Получить всех клиентов (поиск + фильтрация)
    @GetMapping
    public List<Client> getAllClients(@RequestParam(required = false) String name,
            @RequestParam(required = false) String shortName,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) LegalForm legalForm,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        List<Client> clients = clientService.findAllClients(name, shortName, address, legalForm, sortBy, direction);
        if (clients.isEmpty()) {
            throw new NoClientsFoundException("Клиенты с указанными критериями не найдены");
        }
        return clients;
    }

    // Получить клиента по ID
    @GetMapping("/{id}")
    public Client getClientById(@PathVariable Long id) {
        return clientService.findClientById(id);
    }

    // Создать нового клиента
    @PostMapping
    public Client createClient(@Valid @RequestBody Client client) {
        return clientService.createClient(client);
    }

    // Обновить (изменить) клиента по ID
    @PutMapping("/{id}")
    public Client updateClient(@PathVariable Long id, @Valid @RequestBody Client clientDetails) {
        return clientService.updateClient(id, clientDetails);
    }

    // Удалить клиента по ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        try {
            clientService.deleteClient(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) { // клиент с депозитами
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}