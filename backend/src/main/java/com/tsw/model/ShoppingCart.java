package com.tsw.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(
        name = "shopping_cart",
        uniqueConstraints = @UniqueConstraint(name = "uk_shopping_cart_client", columnNames = "client_id")
)
public class ShoppingCart {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "UniqueID")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
}
