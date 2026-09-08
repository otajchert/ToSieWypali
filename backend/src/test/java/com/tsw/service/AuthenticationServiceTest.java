package com.tsw.service;

import com.tsw.dto.AuthenticatedClientData;
import com.tsw.model.Client;
import com.tsw.repository.ClientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AuthenticationServiceTest {

    private final ClientRepository clientRepository = mock(ClientRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final AuthenticationService authenticationService =
            new AuthenticationService(clientRepository, passwordEncoder);

    @Test
    void returnsAuthenticationDataForExistingClient() {
        UUID clientId = UUID.randomUUID();
        Client client = new Client();
        client.setId(clientId);
        client.setEmail("admin@example.com");
        client.setRole("ADMIN");
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        Optional<AuthenticatedClientData> result = authenticationService.findAuthenticatedClient(clientId);

        assertTrue(result.isPresent());
        AuthenticatedClientData clientData = result.orElseThrow();
        assertEquals(clientId, clientData.id());
        assertEquals("admin@example.com", clientData.email());
        assertEquals("ADMIN", clientData.role());
        verify(clientRepository).findById(clientId);
    }

    @Test
    void returnsEmptyForMissingClient() {
        UUID clientId = UUID.randomUUID();
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        Optional<AuthenticatedClientData> result = authenticationService.findAuthenticatedClient(clientId);

        assertTrue(result.isEmpty());
        verify(clientRepository).findById(clientId);
    }

    @Test
    void authenticatesClientWithMatchingPassword() {
        Client client = new Client();
        client.setId(UUID.randomUUID());
        client.setEmail("client@example.com");
        client.setPassword("encoded-password");
        client.setRole("CLIENT");
        when(clientRepository.findByEmail("client@example.com")).thenReturn(Optional.of(client));
        when(passwordEncoder.matches("plain-password", "encoded-password")).thenReturn(true);

        Optional<AuthenticatedClientData> result =
                authenticationService.authenticate("client@example.com", "plain-password");

        assertTrue(result.isPresent());
        assertEquals(client.getId(), result.orElseThrow().id());
        assertEquals("client@example.com", result.orElseThrow().email());
        assertEquals("CLIENT", result.orElseThrow().role());
        verify(clientRepository).findByEmail("client@example.com");
        verify(passwordEncoder).matches("plain-password", "encoded-password");
    }

    @Test
    void rejectsClientWithNonMatchingPassword() {
        Client client = new Client();
        client.setEmail("client@example.com");
        client.setPassword("encoded-password");
        when(clientRepository.findByEmail("client@example.com")).thenReturn(Optional.of(client));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        Optional<AuthenticatedClientData> result =
                authenticationService.authenticate("client@example.com", "wrong-password");

        assertTrue(result.isEmpty());
        verify(clientRepository).findByEmail("client@example.com");
        verify(passwordEncoder).matches("wrong-password", "encoded-password");
    }

    @Test
    void returnsEmptyWithoutCheckingPasswordWhenEmailDoesNotExist() {
        when(clientRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        Optional<AuthenticatedClientData> result =
                authenticationService.authenticate("missing@example.com", "plain-password");

        assertTrue(result.isEmpty());
        verify(clientRepository).findByEmail("missing@example.com");
        verifyNoInteractions(passwordEncoder);
    }
}
