package com.tsw.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

// composite PK for order_product junction
@Embeddable
public class OrderProductId implements Serializable {

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "order_id")
    private UUID orderId;

    public OrderProductId() {}

    public OrderProductId(UUID productId, UUID orderId) {
        this.productId = productId;
        this.orderId = orderId;
    }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderProductId that)) return false;
        return Objects.equals(productId, that.productId) && Objects.equals(orderId, that.orderId);
    }

    @Override
    public int hashCode() { return Objects.hash(productId, orderId); }
}
