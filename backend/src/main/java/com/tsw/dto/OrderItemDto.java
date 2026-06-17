package com.tsw.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemDto(
        UUID productId,
        String productName,
        String productPhoto,
        int qty,
        BigDecimal price
) {}
