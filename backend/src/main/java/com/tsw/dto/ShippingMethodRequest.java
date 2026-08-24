package com.tsw.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ShippingMethodRequest(
        @NotBlank(message = "SHIPPING_METHOD_NAME_REQUIRED")
        @Size(max = 100, message = "SHIPPING_METHOD_NAME_TOO_LONG") String name,
        @NotNull(message = "SHIPPING_METHOD_PRICE_REQUIRED")
        @PositiveOrZero(message = "SHIPPING_METHOD_PRICE_MUST_NOT_BE_NEGATIVE")
        @Digits(integer = 8, fraction = 2, message = "INVALID_SHIPPING_METHOD_PRICE_FORMAT") BigDecimal price
) {
}
