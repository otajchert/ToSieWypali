package com.tsw.controller;

import com.tsw.config.AuthenticatedClient;
import com.tsw.dto.AddressRequest;
import com.tsw.model.ClientAddress;
import com.tsw.service.AddressService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/me/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public List<ClientAddress> getAll(@AuthenticationPrincipal AuthenticatedClient client) {
        return addressService.findByClient(client.id());
    }

    @PostMapping
    public ResponseEntity<?> add(@AuthenticationPrincipal AuthenticatedClient client,
                                 @RequestBody AddressRequest req) {
        try {
            return ResponseEntity.ok(addressService.add(client.id(), req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal AuthenticatedClient client,
                                       @PathVariable UUID addressId) {
        return addressService.remove(client.id(), addressId)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<?> update(@AuthenticationPrincipal AuthenticatedClient client,
                                    @PathVariable UUID addressId,
                                    @RequestBody AddressRequest req) {
        return addressService.update(client.id(), addressId, req)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{addressId}/default")
    public ResponseEntity<Void> setDefault(@AuthenticationPrincipal AuthenticatedClient client,
                                           @PathVariable UUID addressId) {
        return addressService.setDefault(client.id(), addressId)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
