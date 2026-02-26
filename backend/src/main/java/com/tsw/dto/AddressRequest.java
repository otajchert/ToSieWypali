package com.tsw.dto;

// used when a client adds or updates a delivery address
public class AddressRequest {

    private String name;        // label, e.g. "Dom", "Praca"
    private String city;
    private String region;
    private String postalCode;
    private String streetNumber;
    private String flat;
    private boolean isDefault;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
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
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }
}
