package com.tsw.dto;

import java.util.UUID;

public record OrderStatusResponse(
        UUID id,
        String name
) {
}
