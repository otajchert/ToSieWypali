package com.tsw.service;

import com.tsw.dto.ClientRequest;
import com.tsw.model.Client;
import com.tsw.repository.ClientRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    public ClientService(ClientRepository clientRepository, PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
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

    public Client register(ClientRequest req) {
        if (emailExists(req.getEmail())) {
            throw new IllegalArgumentException("Email jest już zajęty");
        }
        Client client = new Client();
        client.setEmail(req.getEmail());
        client.setPassword(passwordEncoder.encode(req.getPassword()));
        client.setFirstName(req.getFirstName());
        client.setLastName(req.getLastName());
        client.setPhoneNumber(req.getPhoneNumber());
        client.setRole("CLIENT");
        return clientRepository.save(client);
    }

    public Optional<Client> update(UUID id, ClientRequest req) {
        return clientRepository.findById(id).map(client -> {
            if (req.getEmail() != null && !req.getEmail().equals(client.getEmail())) {
                if (emailExists(req.getEmail())) {
                    throw new IllegalArgumentException("Email jest już zajęty");
                }
                client.setEmail(req.getEmail());
            }
            if (req.getFirstName() != null) {
                if (req.getFirstName().isBlank()) throw new IllegalArgumentException("Imię nie może być puste");
                client.setFirstName(req.getFirstName());
            }
            if (req.getLastName() != null) {
                if (req.getLastName().isBlank()) throw new IllegalArgumentException("Nazwisko nie może być puste");
                client.setLastName(req.getLastName());
            }
            if (req.getPhoneNumber() != null) client.setPhoneNumber(req.getPhoneNumber());
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
