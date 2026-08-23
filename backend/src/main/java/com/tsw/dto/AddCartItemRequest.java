package com.tsw.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record AddCartItemRequest(
        @NotNull(message = "PRODUCT_ID_REQUIRED") UUID productId,
        @NotNull(message = "QUANTITY_REQUIRED")
        @Positive(message = "QUANTITY_MUST_BE_POSITIVE") Integer qty
) {
}
