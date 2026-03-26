package com.example.library.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.library.entity.Borrow;
import com.example.library.service.LibraryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/library")
@Tag(name = "User Endpoints", description = "Logged-in user operations: view books, borrow, return, and view borrowed books")
public class LibraryController {

    private final LibraryService libraryService;

    @Value("${security.library.auth-required:true}")
    private boolean libraryAuthRequired;

    public LibraryController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @PostMapping("/borrow")
    @Operation(summary = "Borrow a book", description = "Borrow a book as the authenticated user")
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
    @Operation(summary = "Return a borrowed book", description = "Return a book borrowed by the authenticated user")
    public BorrowResponse returnBook(
            @PathVariable Long borrowId,
            Authentication authentication,
            @RequestHeader(value = "X-User-Email", required = false) String fallbackEmail
    ) {
        String requesterEmail = resolveRequesterEmail(authentication, fallbackEmail);
        Borrow borrow = libraryService.returnBook(borrowId, requesterEmail);
        return BorrowResponse.from(borrow);
    }

    @GetMapping("/books")
    @Operation(summary = "View all books", description = "View all books in the library with optional search and pagination")
    public PagedResponse<BookResponse> getBooks(
            Authentication authentication,
            @RequestHeader(value = "X-User-Email", required = false) String fallbackEmail,
            @RequestParam(required = false) String query,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        resolveRequesterEmail(authentication, fallbackEmail);
        Page<BookResponse> page = libraryService.getAllBooks(query, pageable).map(BookResponse::from);
        return PagedResponse.from(page);
    }

    @GetMapping("/borrowed")
    @Operation(summary = "View my borrowed books", description = "View currently borrowed books for the authenticated user")
    public List<BorrowResponse> getBorrowedBooks(
            Authentication authentication,
            @RequestHeader(value = "X-User-Email", required = false) String fallbackEmail
    ) {
        String requesterEmail = resolveRequesterEmail(authentication, fallbackEmail);
        return libraryService.getBorrowedBooks(requesterEmail).stream()
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

    public record BookResponse(Long id, String title, String author, int availableCopies) {

        static BookResponse from(com.example.library.entity.Book book) {
            return new BookResponse(book.getId(), book.getTitle(), book.getAuthor(), book.getAvailableCopies());
        }
    }

    public record PagedResponse<T>(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext,
            boolean hasPrevious
            ) {

        static <T> PagedResponse<T> from(Page<T> page) {
            return new PagedResponse<>(
                    page.getContent(),
                    page.getNumber(),
                    page.getSize(),
                    page.getTotalElements(),
                    page.getTotalPages(),
                    page.hasNext(),
                    page.hasPrevious()
            );
        }
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
