package com.tsw.service;

import com.tsw.dto.AuthenticatedClientData;
import com.tsw.model.Client;
import com.tsw.repository.ClientRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthenticationService {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(ClientRepository clientRepository, PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Optional<AuthenticatedClientData> findAuthenticatedClient(UUID clientId) {
        return clientRepository.findById(clientId)
                .map(this::toAuthenticatedClientData);
    }

    @Transactional(readOnly = true)
    public Optional<AuthenticatedClientData> authenticate(String email, String password) {
        return clientRepository.findByEmail(email)
                .filter(client -> passwordEncoder.matches(password, client.getPassword()))
                .map(this::toAuthenticatedClientData);
    }

    private AuthenticatedClientData toAuthenticatedClientData(Client client) {
        return new AuthenticatedClientData(client.getId(), client.getEmail(), client.getRole());
    }
}
