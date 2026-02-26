package com.tsw.dto;

import java.util.UUID;

// used when admin creates or updates a category
public class CategoryRequest {

    private String categoryName;
    private UUID parentCategoryId;  // null for top-level categories

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public UUID getParentCategoryId() { return parentCategoryId; }
    public void setParentCategoryId(UUID parentCategoryId) { this.parentCategoryId = parentCategoryId; }
}
