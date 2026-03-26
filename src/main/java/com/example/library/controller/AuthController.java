package com.example.library.controller;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.library.entity.User;
import com.example.library.entity.UserRole;
import com.example.library.service.AuthService;
import com.example.library.service.RegistrationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Common Endpoints", description = "Authentication and account registration endpoints")
public class AuthController {

    private final RegistrationService registrationService;
    private final AuthService authService;

    public AuthController(RegistrationService registrationService, AuthService authService) {
        this.registrationService = registrationService;
        this.authService = authService;
    }

    @PostMapping("/register/user")
    @Operation(summary = "Register user", description = "Create a new standard user account")
    public RegistrationResponse registerUser(@Valid @RequestBody RegistrationRequest request) {
        User user = registrationService.registerUser(request.name(), request.email(), request.password());
        return RegistrationResponse.from(user);
    }

    @PostMapping("/register/employee")
    @Operation(summary = "Register employee", description = "Create a new employee account")
    public RegistrationResponse registerEmployee(@Valid @RequestBody RegistrationRequest request) {
        User user = registrationService.registerEmployee(request.name(), request.email(), request.password());
        return RegistrationResponse.from(user);
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate and get JWT access token")
    public AuthService.LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request.email(), request.password());
    }

    public record RegistrationRequest(
            @NotBlank String name,
            @NotBlank
            @Email String email,
            @NotBlank
            @Size(min = 8, message = "Password must be at least 8 characters") String password
            ) {

    }

    public record RegistrationResponse(
            Long id,
            String name,
            String email,
            UserRole role,
            LocalDateTime createdAt
            ) {

        static RegistrationResponse from(User user) {
            return new RegistrationResponse(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getRole(),
                    user.getCreatedAt()
            );
        }
    }

    public record LoginRequest(
            @NotBlank
            @Email String email,
            @NotBlank String password
            ) {

    }
}
