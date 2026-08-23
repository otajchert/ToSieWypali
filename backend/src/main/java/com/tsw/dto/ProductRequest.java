package com.tsw.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductRequest(
        @NotBlank(message = "PRODUCT_NAME_REQUIRED")
        @Size(max = 255, message = "PRODUCT_NAME_TOO_LONG") String name,
        @Size(max = 10_000, message = "PRODUCT_DESCRIPTION_TOO_LONG") String description,
        @NotNull(message = "PRICE_REQUIRED")
        @Positive(message = "PRICE_MUST_BE_POSITIVE")
        @Digits(integer = 8, fraction = 2, message = "INVALID_PRICE_FORMAT") BigDecimal price,
        @NotNull(message = "STOCK_QUANTITY_REQUIRED")
        @PositiveOrZero(message = "STOCK_QUANTITY_MUST_NOT_BE_NEGATIVE") Integer qtyInStock,
        @Size(max = 50, message = "WEIGHT_TOO_LONG") String weight,
        @Size(max = 50, message = "HEIGHT_TOO_LONG") String height,
        @Size(max = 50, message = "WIDTH_TOO_LONG") String width,
        @Size(max = 50, message = "PRODUCT_LENGTH_TOO_LONG") String productLength,
        @NotNull(message = "CATEGORY_IDS_REQUIRED")
        @Size(max = 100, message = "TOO_MANY_CATEGORIES")
        List<@NotNull(message = "CATEGORY_ID_REQUIRED") UUID> categoryIds
) {
}
