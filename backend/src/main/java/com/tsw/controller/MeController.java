package com.tsw.controller;

import com.tsw.config.AuthenticatedClient;
import com.tsw.dto.UpdateClientRequest;
import com.tsw.model.Client;
import com.tsw.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class MeController {

    private final ClientService clientService;

    public MeController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public Client get(@AuthenticationPrincipal AuthenticatedClient client) {
        return clientService.getById(client.id());
    }

    @PutMapping
    public Client update(@AuthenticationPrincipal AuthenticatedClient client,
                         @Valid @RequestBody UpdateClientRequest request) {
        return clientService.update(client.id(), request);
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthenticatedClient client) {
        clientService.delete(client.id());
        return ResponseEntity.noContent().build();
    }
}
