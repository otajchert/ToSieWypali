package com.tsw.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @Size(max = 100, message = "ADDRESS_NAME_TOO_LONG") String name,
        @NotBlank(message = "CITY_REQUIRED")
        @Size(max = 100, message = "CITY_TOO_LONG") String city,
        @NotBlank(message = "REGION_REQUIRED")
        @Size(max = 100, message = "REGION_TOO_LONG") String region,
        @NotBlank(message = "POSTAL_CODE_REQUIRED")
        @Size(max = 20, message = "POSTAL_CODE_TOO_LONG") String postalCode,
        @NotBlank(message = "STREET_NUMBER_REQUIRED")
        @Size(max = 50, message = "STREET_NUMBER_TOO_LONG") String streetNumber,
        @Size(max = 20, message = "FLAT_TOO_LONG") String flat,
        @JsonProperty("isDefault") boolean isDefault
) {
}
