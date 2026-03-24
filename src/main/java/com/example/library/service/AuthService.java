package com.example.library.service;

import com.example.library.entity.User;
import com.example.library.entity.UserRole;
import com.example.library.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(String email, String password) {
        User user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        String token = jwtService.generateToken(user);
        return new LoginResponse(
                token,
                "Bearer",
                jwtService.getExpirationMs(),
                user.getId(),
                user.getEmail(),
                user.getRole()
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    public record LoginResponse(
            String accessToken,
            String tokenType,
            long expiresInMs,
            Long userId,
            String email,
            UserRole role
            ) {

    }
}
