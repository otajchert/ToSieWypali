package com.tsw.service;

import com.tsw.dto.AuthenticatedClientData;
import com.tsw.model.Client;
import com.tsw.repository.ClientRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthenticationServiceTest {

    private final ClientRepository clientRepository = mock(ClientRepository.class);
    private final AuthenticationService authenticationService = new AuthenticationService(clientRepository);

    @Test
    void returnsAuthenticationDataForExistingClient() {
        UUID clientId = UUID.randomUUID();
        Client client = new Client();
        client.setId(clientId);
        client.setRole("ADMIN");
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        Optional<AuthenticatedClientData> result = authenticationService.findAuthenticatedClient(clientId);

        assertTrue(result.isPresent());
        AuthenticatedClientData clientData = result.orElseThrow();
        assertEquals(clientId, clientData.id());
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
}
