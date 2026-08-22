package com.tsw.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record AddCartItemRequest(
        @NotNull(message = "Identyfikator produktu jest wymagany") UUID productId,
        @NotNull(message = "Ilość jest wymagana")
        @Positive(message = "Ilość musi być większa od zera") Integer qty
) {
}
