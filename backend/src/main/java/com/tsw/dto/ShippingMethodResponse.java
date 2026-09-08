package com.tsw.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ShippingMethodResponse(
        UUID id,
        String name,
        BigDecimal price
) {
}
