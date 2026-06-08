package com.tsw.dto;

import java.util.UUID;

public class AuthResponse {

    private String token;
    private UUID id;
    private String email;
    private String role;

    public AuthResponse(String token, UUID id, String email, String role) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.role = role;
    }

    public String getToken() { return token; }
    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}
