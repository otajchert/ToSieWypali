package com.tsw.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateCartItemQuantityRequest(
        @NotNull(message = "Ilość jest wymagana")
        @Positive(message = "Ilość musi być większa od zera") Integer qty
) {
}
