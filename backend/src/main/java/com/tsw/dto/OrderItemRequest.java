package com.tsw.dto;

import java.util.UUID;

// one line in an order - which product and how many
public class OrderItemRequest {

    private UUID productId;
    private int qty;

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }
}
