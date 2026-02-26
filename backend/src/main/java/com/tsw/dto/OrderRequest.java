package com.tsw.dto;

import java.util.List;
import java.util.UUID;

// used when a client places an order
public class OrderRequest {

    private List<OrderItemRequest> items;
    private UUID shippingMethodId;
    private UUID addressId;

    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }
    public UUID getShippingMethodId() { return shippingMethodId; }
    public void setShippingMethodId(UUID shippingMethodId) { this.shippingMethodId = shippingMethodId; }
    public UUID getAddressId() { return addressId; }
    public void setAddressId(UUID addressId) { this.addressId = addressId; }
}
