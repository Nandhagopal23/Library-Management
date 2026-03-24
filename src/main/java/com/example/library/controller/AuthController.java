package com.example.library.controller;

import com.example.library.entity.User;
import com.example.library.entity.UserRole;
import com.example.library.service.RegistrationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/register")
public class AuthController {

    private final RegistrationService registrationService;

    public AuthController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/user")
    public RegistrationResponse registerUser(@Valid @RequestBody RegistrationRequest request) {
        User user = registrationService.registerUser(request.name(), request.email(), request.password());
        return RegistrationResponse.from(user);
    }

    @PostMapping("/employee")
    public RegistrationResponse registerEmployee(@Valid @RequestBody RegistrationRequest request) {
        User user = registrationService.registerEmployee(request.name(), request.email(), request.password());
        return RegistrationResponse.from(user);
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
}
