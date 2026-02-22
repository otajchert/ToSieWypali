package com.tsw.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "address")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "UniqueID")
    private UUID id;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String region;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "street_number", length = 50)
    private String streetNumber;

    @Column(length = 20)
    private String flat;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public String getStreetNumber() { return streetNumber; }
    public void setStreetNumber(String streetNumber) { this.streetNumber = streetNumber; }
    public String getFlat() { return flat; }
    public void setFlat(String flat) { this.flat = flat; }
}
