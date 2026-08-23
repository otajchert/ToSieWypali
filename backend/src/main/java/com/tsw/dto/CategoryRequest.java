package com.tsw.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CategoryRequest(
        @NotBlank(message = "CATEGORY_NAME_REQUIRED")
        @Size(max = 100, message = "CATEGORY_NAME_TOO_LONG") String categoryName,
        UUID parentCategoryId
) {
}
