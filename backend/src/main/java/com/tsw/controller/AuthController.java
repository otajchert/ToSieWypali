package com.tsw.controller;

import com.tsw.config.JwtUtil;
import com.tsw.dto.AuthenticatedClientData;
import com.tsw.dto.AuthResponse;
import com.tsw.dto.ClientResponse;
import com.tsw.dto.LoginRequest;
import com.tsw.dto.RegisterRequest;
import com.tsw.exception.InvalidCredentialsException;
import com.tsw.service.AuthenticationService;
import com.tsw.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final ClientService clientService;
    private final AuthenticationService authenticationService;
    private final JwtUtil jwtUtil;

    public AuthController(ClientService clientService,
                          AuthenticationService authenticationService,
                          JwtUtil jwtUtil) {
        this.clientService = clientService;
        this.authenticationService = authenticationService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        AuthenticatedClientData client = authenticationService.authenticate(request.email(), request.password())
                .orElseThrow(InvalidCredentialsException::new);
        String token = jwtUtil.generate(client.id());
        return new AuthResponse(token, client.id(), client.email(), client.role());
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        ClientResponse client = clientService.register(request);
        String token = jwtUtil.generate(client.id());
        return new AuthResponse(token, client.id(), client.email(), client.role());
    }
}
