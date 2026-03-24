package com.example.library.controller;

import java.math.BigDecimal;
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
import com.example.library.entity.Borrow;
import com.example.library.service.EmployeeService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping("/api/employee")
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping("/books")
    public BookResponse createBook(@Valid @RequestBody CreateBookRequest request) {
        Book book = employeeService.createBook(request.title(), request.author(), request.availableCopies());
        return BookResponse.from(book);
    }

    @PutMapping("/books/{id}")
    public BookResponse updateBook(@PathVariable Long id, @Valid @RequestBody UpdateBookRequest request) {
        Book book = employeeService.updateBook(id, request.title(), request.author(), request.availableCopies());
        return BookResponse.from(book);
    }

    @PatchMapping("/books/{id}/stock")
    public BookResponse adjustBookStock(@PathVariable Long id, @Valid @RequestBody AdjustStockRequest request) {
        Book book = employeeService.adjustStock(id, request.availableCopies());
        return BookResponse.from(book);
    }

    @DeleteMapping("/books/{id}")
    public DeleteResponse deleteBook(@PathVariable Long id) {
        employeeService.deleteBook(id);
        return new DeleteResponse("Book deleted successfully");
    }

    @GetMapping("/books")
    public PagedResponse<BookResponse> getBooks(
            @RequestParam(required = false) String query,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<BookResponse> page = employeeService.getBooks(query, pageable).map(BookResponse::from);
        return PagedResponse.from(page);
    }

    @GetMapping("/borrows/active")
    public PagedResponse<BorrowResponse> getActiveBorrows(@PageableDefault(size = 10) Pageable pageable) {
        Page<BorrowResponse> page = employeeService.getActiveBorrows(pageable).map(BorrowResponse::from);
        return PagedResponse.from(page);
    }

    @GetMapping("/borrows/overdue")
    public PagedResponse<BorrowResponse> getOverdueBorrows(@PageableDefault(size = 10) Pageable pageable) {
        Page<BorrowResponse> page = employeeService.getOverdueBorrows(pageable).map(BorrowResponse::from);
        return PagedResponse.from(page);
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

    public record BookResponse(Long id, String title, String author, int availableCopies) {

        static BookResponse from(Book book) {
            return new BookResponse(book.getId(), book.getTitle(), book.getAuthor(), book.getAvailableCopies());
        }
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
