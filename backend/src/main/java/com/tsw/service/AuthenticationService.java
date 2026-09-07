package com.tsw.service;

import com.tsw.dto.AuthenticatedClientData;
import com.tsw.repository.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthenticationService {

    private final ClientRepository clientRepository;

    public AuthenticationService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Transactional(readOnly = true)
    public Optional<AuthenticatedClientData> findAuthenticatedClient(UUID clientId) {
        return clientRepository.findById(clientId)
                .map(client -> new AuthenticatedClientData(client.getId(), client.getRole()));
    }
}
