package com.example.bank_backend.service;

import com.example.bank_backend.exception.ClientAlreadyExistsException;
import com.example.bank_backend.exception.ClientNotFoundException;
import com.example.bank_backend.exception.NoClientsFoundException;
import com.example.bank_backend.model.Client;
import com.example.bank_backend.model.LegalForm;
import com.example.bank_backend.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    @Autowired
    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public List<Client> findAllClients(String name, String shortName, String address, LegalForm legalForm, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        List<Client> clients = clientRepository.findAll(sort).stream()
                .filter(c -> (name == null || c.getName().toLowerCase().contains(name.toLowerCase())))
                .filter(c -> (shortName == null || c.getShortName().toLowerCase().contains(shortName.toLowerCase())))
                .filter(c -> (address == null || c.getAddress().toLowerCase().contains(address.toLowerCase())))
                .filter(c -> (legalForm == null || c.getLegalForm() == legalForm))
                .toList();

        if (clients.isEmpty()) {
            throw new NoClientsFoundException("Список клиентов пуст.");
        }

        return clients;
    }

    public Client findClientById(long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException("Клиент с ID: " + id + " не найден."));
    }

    public Client createClient(Client client) {
        if (clientRepository.existsByName(client.getName())) {
            throw new ClientAlreadyExistsException("Клиент с именем '" + client.getName() + "' уже существует.");
        }
        return clientRepository.save(client);
    }

    public Client updateClient(Long id, Client clientDetails) {
        Client client = findClientById(id);

        client.setName(clientDetails.getName());
        client.setShortName(clientDetails.getShortName());
        client.setAddress(clientDetails.getAddress());
        client.setLegalForm(clientDetails.getLegalForm());

        return clientRepository.save(client);
    }

    public void deleteClient(Long id) {
        Client client = findClientById(id);
        clientRepository.delete(client);
    }

}