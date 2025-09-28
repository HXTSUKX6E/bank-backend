package com.example.bank_backend.controller;
/*
package com.example.bank_backend.controller;

import com.example.bank_backend.model.Client;
import com.example.bank_backend.model.LegalForm;
import com.example.bank_backend.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ClientControllerIntegrationTest {

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    private final ClientRepository clientRepository;

    @Autowired
    public ClientControllerIntegrationTest(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createClient_ShouldReturnCreatedClient() {
        // Given
        String clientJson = """
            {
                "name": "Иван Иванович Иванов",
                "legalForm": "АО"
            }
            """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(clientJson, headers);

        // http
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/clients", request, String.class
        );

        System.out.println("=== RESPONSE ===");
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Body: " + response.getBody());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK); // статус
        assertThat(response.getBody()).isNotNull(); // вернуло тело
    }

    @Test
    void createClient_WithDuplicateName_ShouldReturnError() {

        String firstClientJson = """
            {
                "name": "Иван Иванович Иванов",
                "shortName": "Иван",
                "address": "Адресс",
                "legalForm": "АО"
            }
            """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Создаем 1-го клиента
        restTemplate.postForEntity("/api/clients",
                new HttpEntity<>(firstClientJson, headers), Client.class);

        // Создаем 2-го клиента с тем же name
        String duplicateClientJson = """
            {
                "name": "Иван Иванович Иванов",
                "shortName": "Иван",
                "address": "Адресс2",
                "legalForm": "ООО"
            }
            """;

        ResponseEntity<String> response = restTemplate.postForEntity("/api/clients",
                new HttpEntity<>(duplicateClientJson, headers), String.class);

        // 409
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void createClient_WithoutLegalForm_ShouldReturnBadRequestError() {

        String clientJson = """
        {
            "name": "БезФормы",
            "shortName": "БезФормы",
            "address": "Адрес"
        }
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(clientJson, headers);

        ResponseEntity<String> response = restTemplate.postForEntity("/api/clients",
                request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createClient_WithExceptionInLegalForm_ShouldReturnBadRequestError() {

        String clientJson = """
        {
            "name": "БезФормы",
            "shortName": "БезФормы",
            "address": "Адрес"
            "legalForm": "ОЧЕНЬ НЕПОНЯТНАЯ ФОРМА"
        }
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(clientJson, headers);

        ResponseEntity<String> response = restTemplate.postForEntity("/api/clients",
                request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getClient_WithExistingId_ShouldReturnClient() {

        String clientJson = """
        {
            "name": "WW",
            "shortName": "ОООООО",
            "address": "Адрес",
            "legalForm": "ИП"
        }
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create and get ID
        ResponseEntity<Client> createResponse = restTemplate.postForEntity("/api/clients",
                new HttpEntity<>(clientJson, headers), Client.class);

        Long clientId = Objects.requireNonNull(createResponse.getBody()).getId();

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/clients/" + clientId, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void getClient_WithNonExistingId_ShouldReturnNotFound() {
        // несущ. ID
        long nonExistingId = 99999L;

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/clients/" + nonExistingId, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getAllClients_ShouldReturnClientsList() {

        String client1Json = """
        {
            "name": "Нэйм",
            "shortName": "Н",
            "address": "Адрес 1",
            "legalForm": "OOO"
        }
        """;

        String client2Json = """
        {
            "name": "Нэйм2",
            "shortName": "Н",
            "address": "Адрес 2",
            "legalForm": "АО"
        }
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.postForEntity("/api/clients", new HttpEntity<>(client1Json, headers), Client.class);
        restTemplate.postForEntity("/api/clients", new HttpEntity<>(client2Json, headers), Client.class);

        ResponseEntity<Client[]> response = restTemplate.getForEntity("/api/clients", Client[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void getAllClients_ShouldReturnEmptyList() {
        clientRepository.deleteAll();
        ResponseEntity<String> response = restTemplate.getForEntity("/api/clients", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateClient_WithValid_ShouldReturnUpdatedClient() {

        String clientJson = """
        {
            "name": "ЧЕЛ522",
            "shortName": "До",
            "address": "адрес",
            "legalForm": "АО"
        }
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // add
        ResponseEntity<Client> createResponse = restTemplate.postForEntity("/api/clients",
                new HttpEntity<>(clientJson, headers), Client.class);

        Long clientId = Objects.requireNonNull(createResponse.getBody()).getId();

        String updateJson = """
        {
            "name": "new имя",
            "legalForm": "ИП"
        }
        """;

        ResponseEntity<Client> response = restTemplate.exchange(
                "/api/clients/" + clientId,
                HttpMethod.PUT,
                new HttpEntity<>(updateJson, headers),
                Client.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void updateClient_WithNonExistingId_ShouldReturnNotFound() {

        long nonExistingId = 999L;

        String clientJson = """
        {
            "name": "ФФФФ",
            "shortName": "ФФФФ",
            "address": "ФФФФ",
            "legalForm": "ИП"
        }
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/clients/" + nonExistingId,
                HttpMethod.PUT,
                new HttpEntity<>(clientJson, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateClient_WithDuplicateName_ShouldReturnConflict() {

        String client1Json = """
        {
            "name": "Клиент 1",
            "shortName": "К1",
            "address": "Адрес 1",
            "legalForm": "OOO"
        }
        """;

        String client2Json = """
        {
            "name": "Клиент 2",
            "shortName": "К2",
            "address": "Адрес 2",
            "legalForm": "ИП"
        }
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Client> client1Response = restTemplate.postForEntity("/api/clients",
                new HttpEntity<>(client1Json, headers), Client.class);
        restTemplate.postForEntity("/api/clients",
                new HttpEntity<>(client2Json, headers), Client.class);

        Long client1Id = Objects.requireNonNull(client1Response.getBody()).getId();

        // создаем дубликат имени
        String updateJson = """
        {
            "name": "Клиент 2",
            "shortName": "К1Нов",
            "address": "Новый адрес",
            "legalForm": "OOO"
        }
        """;

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/clients/" + client1Id,
                HttpMethod.PUT,
                new HttpEntity<>(updateJson, headers),
                String.class
        );

        // 400
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void deleteClient_WithExistingId_ShouldReturnOk() {

        String clientJson = """
        {
            "name": "Клиент",
            "shortName": "Удалить",
            "address": "удаления",
            "legalForm": "ООО"
        }
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Client> createResponse = restTemplate.postForEntity("/api/clients",
                new HttpEntity<>(clientJson, headers), Client.class);

        Long clientId = Objects.requireNonNull(createResponse.getBody()).getId();

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/clients/" + clientId,
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = restTemplate.getForEntity(
                "/api/clients/" + clientId, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteClient_WithNonExistingId_ShouldReturnNotFound() {

        long nonExistingId = 99L;

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/clients/" + nonExistingId,
                HttpMethod.DELETE,
                null,
                String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
 */
public class TestContainerExample {
}
