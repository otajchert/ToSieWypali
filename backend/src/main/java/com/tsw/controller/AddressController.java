package com.tsw.controller;

import com.tsw.dto.AddressRequest;
import com.tsw.model.ClientAddress;
import com.tsw.service.AddressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clients/{clientId}/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public List<ClientAddress> getAll(@PathVariable UUID clientId) {
        return addressService.findByClient(clientId);
    }

    @PostMapping
    public ResponseEntity<?> add(@PathVariable UUID clientId, @RequestBody AddressRequest req) {
        try {
            return ResponseEntity.ok(addressService.add(clientId, req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> remove(@PathVariable UUID clientId, @PathVariable UUID addressId) {
        return addressService.remove(clientId, addressId)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PutMapping("/{addressId}/default")
    public ResponseEntity<Void> setDefault(@PathVariable UUID clientId, @PathVariable UUID addressId) {
        return addressService.setDefault(clientId, addressId)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
