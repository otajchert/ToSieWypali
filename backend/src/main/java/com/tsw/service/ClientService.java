package com.tsw.service;

import com.tsw.dto.ClientResponse;
import com.tsw.dto.RegisterRequest;
import com.tsw.dto.UpdateClientRequest;
import com.tsw.exception.ApiErrorCode;
import com.tsw.exception.EmailAlreadyUsedException;
import com.tsw.exception.ResourceNotFoundException;
import com.tsw.model.Client;
import com.tsw.repository.ClientRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    public ClientService(ClientRepository clientRepository, PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<ClientResponse> findAll() {
        return clientRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public ClientResponse getById(UUID id) {
        return toResponse(getClient(id));
    }

    private boolean emailExists(String email) {
        return clientRepository.findByEmail(email).isPresent();
    }

    public ClientResponse register(RegisterRequest request) {
        if (emailExists(request.email())) {
            throw new EmailAlreadyUsedException();
        }
        Client client = new Client();
        client.setEmail(request.email());
        client.setPassword(passwordEncoder.encode(request.password()));
        client.setFirstName(request.firstName());
        client.setLastName(request.lastName());
        client.setPhoneNumber(request.phoneNumber());
        client.setRole("CLIENT");
        return toResponse(clientRepository.save(client));
    }

    public ClientResponse update(UUID id, UpdateClientRequest request) {
        Client client = getClient(id);
        if (request.email() != null && !request.email().equals(client.getEmail())) {
            if (emailExists(request.email())) {
                throw new EmailAlreadyUsedException();
            }
            client.setEmail(request.email());
        }
        if (request.firstName() != null) {
            client.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            client.setLastName(request.lastName());
        }
        if (request.phoneNumber() != null) {
            client.setPhoneNumber(request.phoneNumber());
        }
        return toResponse(clientRepository.save(client));
    }

    public void delete(UUID id) {
        clientRepository.delete(getClient(id));
    }

    private Client getClient(UUID id) {
        return clientRepository.findById(id)
                .orElseThrow(this::clientNotFound);
    }

    private ClientResponse toResponse(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getEmail(),
                client.getFirstName(),
                client.getLastName(),
                client.getPhoneNumber(),
                client.getRole()
        );
    }

    private ResourceNotFoundException clientNotFound() {
        return new ResourceNotFoundException(ApiErrorCode.CLIENT_NOT_FOUND, "Nie znaleziono klienta");
    }
}
