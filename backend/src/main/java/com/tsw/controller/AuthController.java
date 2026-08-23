package com.tsw.controller;

import com.tsw.config.JwtUtil;
import com.tsw.dto.AuthResponse;
import com.tsw.dto.LoginRequest;
import com.tsw.dto.RegisterRequest;
import com.tsw.exception.InvalidCredentialsException;
import com.tsw.model.Client;
import com.tsw.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final ClientService clientService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthController(ClientService clientService, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.clientService = clientService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        Client client = clientService.findByEmail(request.email())
                .filter(found -> passwordEncoder.matches(request.password(), found.getPassword()))
                .orElseThrow(InvalidCredentialsException::new);
        String token = jwtUtil.generate(client.getId());
        return new AuthResponse(token, client.getId(), client.getEmail(), client.getRole());
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        Client client = clientService.register(request);
        String token = jwtUtil.generate(client.getId());
        return new AuthResponse(token, client.getId(), client.getEmail(), client.getRole());
    }
}
