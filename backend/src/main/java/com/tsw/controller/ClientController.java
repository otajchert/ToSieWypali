package com.tsw.controller;

import com.tsw.dto.ClientResponse;
import com.tsw.dto.UpdateClientRequest;
import com.tsw.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public List<ClientResponse> getAll() {
        return clientService.findAll();
    }

    @GetMapping("/{id}")
    public ClientResponse getById(@PathVariable UUID id) {
        return clientService.getById(id);
    }

    @PutMapping("/{id}")
    public ClientResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateClientRequest request) {
        return clientService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        clientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
