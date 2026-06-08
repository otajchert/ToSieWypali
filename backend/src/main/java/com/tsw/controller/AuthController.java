package com.tsw.controller;

import com.tsw.config.JwtUtil;
import com.tsw.dto.AuthResponse;
import com.tsw.dto.ClientRequest;
import com.tsw.dto.LoginRequest;
import com.tsw.model.Client;
import com.tsw.service.ClientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        return clientService.findByEmail(req.getEmail())
                .filter(c -> passwordEncoder.matches(req.getPassword(), c.getPassword()))
                .map(c -> {
                    String token = jwtUtil.generate(c.getEmail(), c.getRole());
                    return ResponseEntity.ok((Object) new AuthResponse(token, c.getId(), c.getEmail(), c.getRole()));
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Nieprawidłowy email lub hasło"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody ClientRequest req) {
        try {
            Client c = clientService.register(req);
            String token = jwtUtil.generate(c.getEmail(), c.getRole());
            return ResponseEntity.ok(new AuthResponse(token, c.getId(), c.getEmail(), c.getRole()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
