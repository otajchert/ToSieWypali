package com.tsw.dto;

import java.util.UUID;

public record ProductImageResponse(
        UUID id,
        String imageUrl,
        int sortOrder
) {
}
