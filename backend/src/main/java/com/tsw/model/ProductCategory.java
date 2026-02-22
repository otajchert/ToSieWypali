package com.tsw.model;

import jakarta.persistence.*;

// junction: product <-> category
@Entity
@Table(name = "product_category")
public class ProductCategory {

    @EmbeddedId
    private ProductCategoryId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("categoryId")
    @JoinColumn(name = "category_id")
    private Category category;

    public ProductCategoryId getId() { return id; }
    public void setId(ProductCategoryId id) { this.id = id; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
}
