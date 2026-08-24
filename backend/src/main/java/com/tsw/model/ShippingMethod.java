package com.tsw.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "shipping_method")
public class ShippingMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "UniqueID")
    private UUID id;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean active = true;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    @JsonIgnore
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
