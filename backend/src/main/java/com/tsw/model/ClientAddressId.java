package com.tsw.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ClientAddressId implements Serializable {

    @Column(name = "client_id")
    private UUID clientId;

    @Column(name = "address_id")
    private UUID addressId;

    public ClientAddressId() {}

    public ClientAddressId(UUID clientId, UUID addressId) {
        this.clientId = clientId;
        this.addressId = addressId;
    }

    public UUID getClientId() { return clientId; }
    public void setClientId(UUID clientId) { this.clientId = clientId; }
    public UUID getAddressId() { return addressId; }
    public void setAddressId(UUID addressId) { this.addressId = addressId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientAddressId that)) return false;
        return Objects.equals(clientId, that.clientId) && Objects.equals(addressId, that.addressId);
    }

    @Override
    public int hashCode() { return Objects.hash(clientId, addressId); }
}
