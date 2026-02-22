package com.tsw.model;

import jakarta.persistence.*;
import java.util.UUID;

// junction: client <-> address
@Entity
@Table(name = "client_address")
public class ClientAddress {

    @EmbeddedId
    private ClientAddressId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("clientId")
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("addressId")
    @JoinColumn(name = "address_id")
    private Address address;

    @Column(name = "is_default")
    private Boolean isDefault = false;

    @Column(length = 100)
    private String name;

    public ClientAddressId getId() { return id; }
    public void setId(ClientAddressId id) { this.id = id; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }
    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
