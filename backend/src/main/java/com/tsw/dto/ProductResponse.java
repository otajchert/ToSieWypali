package com.tsw.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String description,
        String photo,
        int qtyInStock,
        String sku,
        BigDecimal price,
        String weight,
        String height,
        String width,
        String productLength,
        Set<CategoryResponse> categories,
        List<ProductImageResponse> images
) {
}
