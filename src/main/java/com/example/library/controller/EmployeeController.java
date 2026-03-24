package com.example.library.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.library.entity.Book;
import com.example.library.entity.User;
import com.example.library.entity.UserRole;
import com.example.library.service.EmployeeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping("/api/employee")
@PreAuthorize("hasRole('EMPLOYEE')")
@Tag(name = "Employee Endpoints", description = "Employee-only operations for managing books and users")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping("/books")
    @Operation(summary = "Add a book", description = "Add a new book to the library")
    public BookResponse createBook(@Valid @RequestBody CreateBookRequest request) {
        Book book = employeeService.createBook(request.title(), request.author(), request.availableCopies());
        return BookResponse.from(book);
    }

    @PutMapping("/books/{id}")
    @Operation(summary = "Update book", description = "Update title, author, and available copies")
    public BookResponse updateBook(@PathVariable Long id, @Valid @RequestBody UpdateBookRequest request) {
        Book book = employeeService.updateBook(id, request.title(), request.author(), request.availableCopies());
        return BookResponse.from(book);
    }

    @PatchMapping("/books/{id}/stock")
    @Operation(summary = "Adjust book stock", description = "Set available copies for a book")
    public BookResponse adjustBookStock(@PathVariable Long id, @Valid @RequestBody AdjustStockRequest request) {
        Book book = employeeService.adjustStock(id, request.availableCopies());
        return BookResponse.from(book);
    }

    @DeleteMapping("/books/{id}")
    @Operation(summary = "Delete book", description = "Delete a book when it has no active borrows")
    public DeleteResponse deleteBook(@PathVariable Long id) {
        employeeService.deleteBook(id);
        return new DeleteResponse("Book deleted successfully");
    }

    @GetMapping("/books")
    @Operation(summary = "List books", description = "List and search books with pagination")
    public PagedResponse<BookResponse> getBooks(
            @RequestParam(required = false) String query,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<BookResponse> page = employeeService.getBooks(query, pageable).map(BookResponse::from);
        return PagedResponse.from(page);
    }

    @GetMapping("/users")
    @Operation(summary = "List users", description = "List and search users with pagination")
    public PagedResponse<UserResponse> getUsers(
            @RequestParam(required = false) String query,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<UserResponse> page = employeeService.getUsers(query, pageable).map(UserResponse::from);
        return PagedResponse.from(page);
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get user by id", description = "Get a single user profile by id")
    public UserResponse getUserById(@PathVariable Long id) {
        return UserResponse.from(employeeService.getUserById(id));
    }

    @PatchMapping("/users/{id}/role")
    @Operation(summary = "Update user role", description = "Change a user role to USER or EMPLOYEE")
    public UserResponse updateUserRole(@PathVariable Long id, @Valid @RequestBody UpdateUserRoleRequest request) {
        return UserResponse.from(employeeService.updateUserRole(id, request.role()));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete user", description = "Delete user if there are no active borrows")
    public DeleteResponse deleteUser(@PathVariable Long id) {
        employeeService.deleteUser(id);
        return new DeleteResponse("User deleted successfully");
    }

    public record CreateBookRequest(
            @NotBlank String title,
            @NotBlank String author,
            @NotNull
            @PositiveOrZero Integer availableCopies
            ) {

    }

    public record UpdateBookRequest(
            @NotBlank String title,
            @NotBlank String author,
            @NotNull
            @PositiveOrZero Integer availableCopies
            ) {

    }

    public record AdjustStockRequest(
            @NotNull
            @PositiveOrZero Integer availableCopies
            ) {

    }

    public record DeleteResponse(String message) {

    }

    public record UpdateUserRoleRequest(@NotNull UserRole role) {

    }

    public record BookResponse(Long id, String title, String author, int availableCopies) {

        static BookResponse from(Book book) {
            return new BookResponse(book.getId(), book.getTitle(), book.getAuthor(), book.getAvailableCopies());
        }
    }

    public record UserResponse(
            Long id,
            String name,
            String email,
            UserRole role,
            LocalDate createdDate
            ) {

        static UserResponse from(User user) {
            return new UserResponse(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getRole(),
                    user.getCreatedAt().toLocalDate()
            );
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
}
