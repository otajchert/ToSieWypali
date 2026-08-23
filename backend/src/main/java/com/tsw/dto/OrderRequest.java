package com.tsw.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record OrderRequest(
        @NotEmpty(message = "ORDER_ITEMS_REQUIRED")
        @Size(max = 100, message = "TOO_MANY_ORDER_ITEMS")
        List<@NotNull(message = "ORDER_ITEM_REQUIRED") @Valid OrderItemRequest> items,
        @NotNull(message = "SHIPPING_METHOD_REQUIRED") UUID shippingMethodId,
        UUID addressId
) {
}
