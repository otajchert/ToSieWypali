package com.tsw.config;

import com.tsw.service.AuthenticationService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final AuthenticationService authenticationService;

    public JwtFilter(JwtUtil jwtUtil, AuthenticationService authenticationService) {
        this.jwtUtil = jwtUtil;
        this.authenticationService = authenticationService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                String subject = jwtUtil.parse(token).getSubject();
                if (subject != null) {
                    UUID clientId = UUID.fromString(subject);
                    authenticationService.findAuthenticatedClient(clientId).ifPresent(client -> {
                        AuthenticatedClient principal = new AuthenticatedClient(client.id());
                        var authentication = new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + client.role()))
                        );
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    });
                }
            } catch (JwtException | IllegalArgumentException ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(req, res);
    }
}
