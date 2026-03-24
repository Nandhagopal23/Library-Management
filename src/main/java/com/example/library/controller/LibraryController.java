package com.example.library.controller;

import com.example.library.entity.Borrow;
import com.example.library.service.LibraryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/library")
public class LibraryController {

    private final LibraryService libraryService;

    @Value("${security.library.auth-required:true}")
    private boolean libraryAuthRequired;

    public LibraryController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @PostMapping("/borrow")
    public BorrowResponse borrowBook(
            @Valid @RequestBody BorrowBookRequest request,
            Authentication authentication,
            @RequestHeader(value = "X-User-Email", required = false) String fallbackEmail
    ) {
        String requesterEmail = resolveRequesterEmail(authentication, fallbackEmail);
        Borrow borrow = libraryService.borrowBook(requesterEmail, request.bookId());
        return BorrowResponse.from(borrow);
    }

    @PostMapping("/return/{borrowId}")
    public BorrowResponse returnBook(
            @PathVariable Long borrowId,
            Authentication authentication,
            @RequestHeader(value = "X-User-Email", required = false) String fallbackEmail
    ) {
        String requesterEmail = resolveRequesterEmail(authentication, fallbackEmail);
        boolean isEmployee = hasEmployeeRole(authentication);
        Borrow borrow = libraryService.returnBook(borrowId, requesterEmail, isEmployee);
        return BorrowResponse.from(borrow);
    }

    @GetMapping("/borrowed")
    public List<BorrowResponse> getBorrowedBooks(
            Authentication authentication,
            @RequestHeader(value = "X-User-Email", required = false) String fallbackEmail
    ) {
        String requesterEmail = resolveRequesterEmail(authentication, fallbackEmail);
        boolean isEmployee = hasEmployeeRole(authentication);
        return libraryService.getBorrowedBooks(requesterEmail, isEmployee).stream()
                .map(BorrowResponse::from)
                .toList();
    }

    public record BorrowBookRequest(
            @NotNull Long bookId
            ) {

    }

    public record BorrowResponse(
            Long id,
            Long userId,
            Long bookId,
            String bookTitle,
            String bookAuthor,
            LocalDate borrowDate,
            LocalDate returnDate,
            BigDecimal latePenalty
            ) {

        static BorrowResponse from(Borrow borrow) {
            return new BorrowResponse(
                    borrow.getId(),
                    borrow.getUser().getId(),
                    borrow.getBook().getId(),
                    borrow.getBook().getTitle(),
                    borrow.getBook().getAuthor(),
                    borrow.getBorrowDate(),
                    borrow.getReturnDate(),
                    borrow.getLatePenalty()
            );
        }
    }

    private boolean hasEmployeeRole(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities() != null
                && authentication.getAuthorities().stream()
                        .anyMatch(authority -> "ROLE_EMPLOYEE".equals(authority.getAuthority()));
    }

    private String resolveRequesterEmail(Authentication authentication, String fallbackEmail) {
        if (authentication != null && authentication.isAuthenticated()) {
            String name = authentication.getName();
            if (name != null && !"anonymousUser".equalsIgnoreCase(name)) {
                return name;
            }
        }

        if (!libraryAuthRequired && fallbackEmail != null && !fallbackEmail.isBlank()) {
            return fallbackEmail;
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
    }
}
