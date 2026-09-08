package com.tsw.dto;

import java.util.UUID;

public record AddressResponse(
        UUID id,
        String city,
        String region,
        String postalCode,
        String streetNumber,
        String flat
) {
}
