package com.tsw.controller;

import com.tsw.config.AuthenticatedClient;
import com.tsw.dto.AddressRequest;
import com.tsw.model.ClientAddress;
import com.tsw.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ClientAddress add(@AuthenticationPrincipal AuthenticatedClient client,
                             @Valid @RequestBody AddressRequest request) {
        return addressService.add(client.id(), request);
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal AuthenticatedClient client,
                                       @PathVariable UUID addressId) {
        addressService.remove(client.id(), addressId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{addressId}")
    public ClientAddress update(@AuthenticationPrincipal AuthenticatedClient client,
                                @PathVariable UUID addressId,
                                @Valid @RequestBody AddressRequest request) {
        return addressService.update(client.id(), addressId, request);
    }

    @PutMapping("/{addressId}/default")
    public ResponseEntity<Void> setDefault(@AuthenticationPrincipal AuthenticatedClient client,
                                           @PathVariable UUID addressId) {
        addressService.setDefault(client.id(), addressId);
        return ResponseEntity.noContent().build();
    }
}
