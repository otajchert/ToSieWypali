package com.tsw.controller;

import com.tsw.config.AuthenticatedClient;
import com.tsw.dto.ClientRequest;
import com.tsw.model.Client;
import com.tsw.service.ClientService;
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
    public ResponseEntity<Client> get(@AuthenticationPrincipal AuthenticatedClient client) {
        return clientService.findById(client.id())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping
    public ResponseEntity<?> update(@AuthenticationPrincipal AuthenticatedClient client,
                                    @RequestBody ClientRequest req) {
        try {
            return clientService.update(client.id(), req)
                    .map(updated -> (ResponseEntity<?>) ResponseEntity.ok(updated))
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthenticatedClient client) {
        return clientService.delete(client.id())
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
