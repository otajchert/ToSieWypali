package com.tsw.config;

import java.security.Principal;
import java.util.UUID;

public record AuthenticatedClient(UUID id) implements Principal {

    @Override
    public String getName() {
        return id.toString();
    }
}
