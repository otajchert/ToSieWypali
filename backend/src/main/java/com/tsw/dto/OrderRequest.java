package com.tsw.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public class OrderRequest {

    @NotEmpty(message = "Zamówienie musi zawierać co najmniej jeden produkt")
    @Valid
    private List<OrderItemRequest> items;

    @NotNull(message = "Metoda dostawy jest wymagana")
    private UUID shippingMethodId;

    private UUID addressId;

    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }
    public UUID getShippingMethodId() { return shippingMethodId; }
    public void setShippingMethodId(UUID shippingMethodId) { this.shippingMethodId = shippingMethodId; }
    public UUID getAddressId() { return addressId; }
    public void setAddressId(UUID addressId) { this.addressId = addressId; }
}
