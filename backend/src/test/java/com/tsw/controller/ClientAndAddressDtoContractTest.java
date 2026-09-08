package com.tsw.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tsw.config.JwtUtil;
import com.tsw.dto.AddressRequest;
import com.tsw.dto.LoginRequest;
import com.tsw.dto.RegisterRequest;
import com.tsw.dto.UpdateClientRequest;
import com.tsw.model.Client;
import com.tsw.repository.ClientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ClientAndAddressDtoContractTest {

    private static final Set<String> AUTH_FIELDS = Set.of("token", "id", "email", "role");
    private static final Set<String> CLIENT_FIELDS = Set.of(
            "id", "email", "firstName", "lastName", "phoneNumber", "role"
    );
    private static final Set<String> CLIENT_ADDRESS_FIELDS = Set.of("id", "address", "isDefault", "name");
    private static final Set<String> CLIENT_ADDRESS_ID_FIELDS = Set.of("clientId", "addressId");
    private static final Set<String> ADDRESS_FIELDS = Set.of(
            "id", "city", "region", "postalCode", "streetNumber", "flat"
    );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void registrationAndLoginKeepAuthenticationContract() throws Exception {
        String email = "dto-register-" + UUID.randomUUID() + "@example.com";
        String password = "valid-password";
        RegisterRequest registrationRequest = new RegisterRequest(
                email,
                password,
                "Anna",
                "Nowak",
                "123456789"
        );

        JsonNode registration = responseBody(mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest))));
        UUID clientId = clientRepository.findByEmail(email).orElseThrow().getId();
        assertAuthResponse(registration, clientId, email, "CLIENT");
        assertEquals(clientId.toString(), jwtUtil.parse(registration.path("token").asText()).getSubject());

        LoginRequest loginRequest = new LoginRequest(email, password);
        JsonNode login = responseBody(mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))));
        assertAuthResponse(login, clientId, email, "CLIENT");
        assertEquals(clientId.toString(), jwtUtil.parse(login.path("token").asText()).getSubject());
    }

    @Test
    void profileEndpointsReturnClientDto() throws Exception {
        Client client = createClient("CLIENT", "Anna", "Nowak", "123456789");

        JsonNode profile = responseBody(mockMvc.perform(get("/api/me")
                .header(HttpHeaders.AUTHORIZATION, bearer(client))));
        assertClientResponse(profile, client.getId(), client.getEmail(), "Anna", "Nowak", "123456789", "CLIENT");

        String updatedEmail = "dto-updated-" + UUID.randomUUID() + "@example.com";
        UpdateClientRequest updateRequest = new UpdateClientRequest(
                updatedEmail,
                "Jan",
                "Kowalski",
                "987654321"
        );
        JsonNode updatedProfile = responseBody(mockMvc.perform(put("/api/me")
                .header(HttpHeaders.AUTHORIZATION, bearer(client))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))));
        assertClientResponse(
                updatedProfile,
                client.getId(),
                updatedEmail,
                "Jan",
                "Kowalski",
                "987654321",
                "CLIENT"
        );
    }

    @Test
    void administratorClientEndpointsReturnClientDto() throws Exception {
        Client admin = createClient("ADMIN", "Admin", "Sklepu", "111222333");
        Client client = createClient("CLIENT", "Anna", "Nowak", "123456789");

        JsonNode clients = responseBody(mockMvc.perform(get("/api/clients")
                .header(HttpHeaders.AUTHORIZATION, bearer(admin))));
        assertTrue(clients.isArray());
        clients.forEach(node -> assertEquals(CLIENT_FIELDS, fieldNames(node)));
        assertClientResponse(
                findClient(clients, client.getId()),
                client.getId(),
                client.getEmail(),
                "Anna",
                "Nowak",
                "123456789",
                "CLIENT"
        );

        JsonNode clientDetails = responseBody(mockMvc.perform(get("/api/clients/{id}", client.getId())
                .header(HttpHeaders.AUTHORIZATION, bearer(admin))));
        assertClientResponse(
                clientDetails,
                client.getId(),
                client.getEmail(),
                "Anna",
                "Nowak",
                "123456789",
                "CLIENT"
        );

        UpdateClientRequest updateRequest = new UpdateClientRequest(null, "Maria", null, null);
        JsonNode updatedClient = responseBody(mockMvc.perform(put("/api/clients/{id}", client.getId())
                .header(HttpHeaders.AUTHORIZATION, bearer(admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))));
        assertClientResponse(
                updatedClient,
                client.getId(),
                client.getEmail(),
                "Maria",
                "Nowak",
                "123456789",
                "CLIENT"
        );
    }

    @Test
    void addressEndpointsKeepNestedAddressContract() throws Exception {
        Client client = createClient("CLIENT", "Anna", "Nowak", "123456789");
        AddressRequest request = new AddressRequest(
                "Dom",
                "Warszawa",
                "mazowieckie",
                "00-001",
                "Długa 1",
                "2",
                true
        );

        JsonNode created = responseBody(mockMvc.perform(post("/api/me/addresses")
                .header(HttpHeaders.AUTHORIZATION, bearer(client))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))));
        UUID addressId = UUID.fromString(created.path("address").path("id").asText());
        assertAddressResponse(created, client.getId(), addressId, request);

        JsonNode addresses = responseBody(mockMvc.perform(get("/api/me/addresses")
                .header(HttpHeaders.AUTHORIZATION, bearer(client))));
        assertTrue(addresses.isArray());
        assertEquals(1, addresses.size());
        assertAddressResponse(addresses.get(0), client.getId(), addressId, request);

        AddressRequest updateRequest = new AddressRequest(
                "Pracownia",
                "Kraków",
                "małopolskie",
                "30-001",
                "Krótka 3",
                null,
                false
        );
        JsonNode updated = responseBody(mockMvc.perform(put("/api/me/addresses/{addressId}", addressId)
                .header(HttpHeaders.AUTHORIZATION, bearer(client))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))));
        assertAddressResponse(updated, client.getId(), addressId, updateRequest);
    }

    private Client createClient(String role, String firstName, String lastName, String phoneNumber) {
        Client client = new Client();
        client.setEmail("dto-client-" + UUID.randomUUID() + "@example.com");
        client.setPassword(passwordEncoder.encode("valid-password"));
        client.setFirstName(firstName);
        client.setLastName(lastName);
        client.setPhoneNumber(phoneNumber);
        client.setRole(role);
        return clientRepository.saveAndFlush(client);
    }

    private String bearer(Client client) {
        return "Bearer " + jwtUtil.generate(client.getId());
    }

    private JsonNode responseBody(ResultActions resultActions) throws Exception {
        String body = resultActions
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(body);
    }

    private void assertAuthResponse(JsonNode response, UUID id, String email, String role) {
        assertEquals(AUTH_FIELDS, fieldNames(response));
        assertFalse(response.path("token").asText().isBlank());
        assertEquals(id.toString(), response.path("id").asText());
        assertEquals(email, response.path("email").asText());
        assertEquals(role, response.path("role").asText());
    }

    private void assertClientResponse(JsonNode response,
                                      UUID id,
                                      String email,
                                      String firstName,
                                      String lastName,
                                      String phoneNumber,
                                      String role) {
        assertEquals(CLIENT_FIELDS, fieldNames(response));
        assertEquals(id.toString(), response.path("id").asText());
        assertEquals(email, response.path("email").asText());
        assertEquals(firstName, response.path("firstName").asText());
        assertEquals(lastName, response.path("lastName").asText());
        assertEquals(phoneNumber, response.path("phoneNumber").asText());
        assertEquals(role, response.path("role").asText());
        assertFalse(response.has("password"));
    }

    private void assertAddressResponse(JsonNode response,
                                       UUID clientId,
                                       UUID addressId,
                                       AddressRequest request) {
        assertEquals(CLIENT_ADDRESS_FIELDS, fieldNames(response));
        assertEquals(CLIENT_ADDRESS_ID_FIELDS, fieldNames(response.path("id")));
        assertEquals(ADDRESS_FIELDS, fieldNames(response.path("address")));
        assertEquals(clientId.toString(), response.path("id").path("clientId").asText());
        assertEquals(addressId.toString(), response.path("id").path("addressId").asText());
        assertEquals(addressId.toString(), response.path("address").path("id").asText());
        assertEquals(request.city(), response.path("address").path("city").asText());
        assertEquals(request.region(), response.path("address").path("region").asText());
        assertEquals(request.postalCode(), response.path("address").path("postalCode").asText());
        assertEquals(request.streetNumber(), response.path("address").path("streetNumber").asText());
        if (request.flat() == null) {
            assertTrue(response.path("address").path("flat").isNull());
        } else {
            assertEquals(request.flat(), response.path("address").path("flat").asText());
        }
        assertEquals(request.isDefault(), response.path("isDefault").asBoolean());
        assertEquals(request.name(), response.path("name").asText());
        assertFalse(response.has("client"));
    }

    private JsonNode findClient(JsonNode clients, UUID clientId) {
        for (JsonNode client : clients) {
            if (clientId.toString().equals(client.path("id").asText())) {
                return client;
            }
        }
        return fail("Nie znaleziono klienta w odpowiedzi");
    }

    private Set<String> fieldNames(JsonNode node) {
        Set<String> names = new HashSet<>();
        node.fieldNames().forEachRemaining(names::add);
        return names;
    }
}
