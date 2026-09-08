package com.tsw.dto;

import java.util.UUID;

public record ClientAddressIdResponse(
        UUID clientId,
        UUID addressId
) {
}
