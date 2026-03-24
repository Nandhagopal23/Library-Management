package com.example.library.service;

import com.example.library.entity.User;
import com.example.library.entity.UserRole;
import com.example.library.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(String name, String email, String rawPassword) {
        return register(name, email, rawPassword, UserRole.USER);
    }

    @Transactional
    public User registerEmployee(String name, String email, String rawPassword) {
        return register(name, email, rawPassword, UserRole.EMPLOYEE);
    }

    private User register(String name, String email, String rawPassword, UserRole role) {
        String normalizedEmail = normalizeEmail(email);
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        User user = new User();
        user.setName(name.trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);

        return userRepository.save(user);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
