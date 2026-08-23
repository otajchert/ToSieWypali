package com.tsw.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateCartItemQuantityRequest(
        @NotNull(message = "QUANTITY_REQUIRED")
        @Positive(message = "QUANTITY_MUST_BE_POSITIVE") Integer qty
) {
}
