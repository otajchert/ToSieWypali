package com.tsw.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tsw.config.JwtUtil;
import com.tsw.dto.ClientRequest;
import com.tsw.model.Client;
import com.tsw.repository.ClientRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClientAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ClientRepository clientRepository;

    private final List<UUID> clientIds = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        clientRepository.deleteAllById(clientIds);
    }

    @Test
    void clientUsesOwnIdentityAndCannotUpdateAnotherClient() throws Exception {
        Client authenticatedClient = createClient("Anna", "Nowak");
        Client otherClient = createClient("Jan", "Kowalski");
        String originalEmail = otherClient.getEmail();
        String originalFirstName = otherClient.getFirstName();

        mockMvc.perform(get("/api/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(authenticatedClient)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(authenticatedClient.getId().toString()))
                .andExpect(jsonPath("$.email").value(authenticatedClient.getEmail()))
                .andExpect(jsonPath("$.role").value("CLIENT"));

        ClientRequest update = new ClientRequest();
        update.setEmail("changed-" + UUID.randomUUID() + "@example.com");
        update.setFirstName("Changed");

        mockMvc.perform(put("/api/clients/{clientId}", otherClient.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(authenticatedClient))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isForbidden());

        Client unchangedClient = clientRepository.findById(otherClient.getId()).orElseThrow();
        assertEquals(originalEmail, unchangedClient.getEmail());
        assertEquals(originalFirstName, unchangedClient.getFirstName());
    }

    private Client createClient(String firstName, String lastName) {
        Client client = new Client();
        client.setEmail("client-auth-test-" + UUID.randomUUID() + "@example.com");
        client.setPassword("test-password");
        client.setFirstName(firstName);
        client.setLastName(lastName);
        client.setRole("CLIENT");
        client = clientRepository.saveAndFlush(client);
        clientIds.add(client.getId());
        return client;
    }

    private String bearer(Client client) {
        return "Bearer " + jwtUtil.generate(client.getId());
    }
}
