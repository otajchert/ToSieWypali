package com.tsw.dto;

import java.util.UUID;

public record ClientResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        String role
) {
}
