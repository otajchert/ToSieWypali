package com.tsw.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// shape of JSON the frontend sends when creating or updating a product
public class ProductRequest {

    private String name;
    private String description;
    private BigDecimal price;
    private int qtyInStock;
    private String sku;
    private String weight;
    private String height;
    private String width;
    private String productLength;
    private List<UUID> categoryIds = new ArrayList<>();

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public int getQtyInStock() { return qtyInStock; }
    public void setQtyInStock(int qtyInStock) { this.qtyInStock = qtyInStock; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getWeight() { return weight; }
    public void setWeight(String weight) { this.weight = weight; }
    public String getHeight() { return height; }
    public void setHeight(String height) { this.height = height; }
    public String getWidth() { return width; }
    public void setWidth(String width) { this.width = width; }
    public String getProductLength() { return productLength; }
    public void setProductLength(String productLength) { this.productLength = productLength; }
    public List<UUID> getCategoryIds() { return categoryIds; }
    public void setCategoryIds(List<UUID> categoryIds) { this.categoryIds = categoryIds; }
}
