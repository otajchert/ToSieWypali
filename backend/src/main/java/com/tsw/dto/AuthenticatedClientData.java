package com.tsw.dto;

import java.util.UUID;

public record AuthenticatedClientData(UUID id, String email, String role) {
}
