package com.tsw.controller;

import com.tsw.dto.ShippingMethodRequest;
import com.tsw.model.ShippingMethod;
import com.tsw.service.ShippingMethodService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/shipping-methods")
public class ShippingMethodController {

    private final ShippingMethodService shippingMethodService;

    public ShippingMethodController(ShippingMethodService shippingMethodService) {
        this.shippingMethodService = shippingMethodService;
    }

    @GetMapping
    public List<ShippingMethod> getAll() {
        return shippingMethodService.findAll();
    }

    @PostMapping
    public ShippingMethod create(@Valid @RequestBody ShippingMethodRequest request) {
        return shippingMethodService.create(request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        shippingMethodService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
