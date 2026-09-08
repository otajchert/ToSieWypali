package com.tsw.dto;

import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String categoryName
) {
}
