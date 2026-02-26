package com.tsw.service;

import com.tsw.dto.ClientRequest;
import com.tsw.model.Client;
import com.tsw.repository.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    public Optional<Client> findById(UUID id) {
        return clientRepository.findById(id);
    }

    public Optional<Client> findByEmail(String email) {
        return clientRepository.findByEmail(email);
    }

    public boolean emailExists(String email) {
        return clientRepository.findByEmail(email).isPresent();
    }

    // registration - password hashing will be added here when implementing auth
    public Client register(ClientRequest req) {
        if (emailExists(req.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }
        Client client = new Client();
        client.setEmail(req.getEmail());
        client.setPassword(req.getPassword()); // TODO: hash password before saving
        client.setPhoneNumber(req.getPhoneNumber());
        return clientRepository.save(client);
    }

    public Optional<Client> update(UUID id, ClientRequest req) {
        return clientRepository.findById(id).map(client -> {
            client.setPhoneNumber(req.getPhoneNumber());
            // email and password changes handled separately (need verification)
            return clientRepository.save(client);
        });
    }

    public boolean delete(UUID id) {
        return clientRepository.findById(id).map(client -> {
            clientRepository.delete(client);
            return true;
        }).orElse(false);
    }
}
