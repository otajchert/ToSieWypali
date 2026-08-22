package com.tsw.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public class OrderItemRequest {

    @NotNull(message = "Produkt jest wymagany")
    private UUID productId;

    @Positive(message = "Ilość musi być większa od zera")
    private int qty;

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }
}
