package com.tsw.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ClientAddressResponse(
        ClientAddressIdResponse id,
        AddressResponse address,
        @JsonProperty("isDefault") Boolean isDefault,
        String name
) {
}
