package com.tsw.dto;

import java.util.UUID;

public record CartItemResponse(
        UUID id,
        ProductResponse product,
        int qty
) {
}
