package com.example.library.controller;

import com.example.library.entity.Borrow;
import com.example.library.service.LibraryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/library")
public class LibraryController {

    private final LibraryService libraryService;

    public LibraryController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @PostMapping("/borrow")
    public BorrowResponse borrowBook(@Valid @RequestBody BorrowBookRequest request) {
        Borrow borrow = libraryService.borrowBook(request.userId(), request.bookId());
        return BorrowResponse.from(borrow);
    }

    @PostMapping("/return/{borrowId}")
    public BorrowResponse returnBook(@PathVariable Long borrowId) {
        Borrow borrow = libraryService.returnBook(borrowId);
        return BorrowResponse.from(borrow);
    }

    @GetMapping("/borrowed")
    public List<BorrowResponse> getBorrowedBooks(@RequestParam(required = false) Long userId) {
        return libraryService.getBorrowedBooks(userId).stream()
                .map(BorrowResponse::from)
                .toList();
    }

    public record BorrowBookRequest(
            @NotNull Long userId,
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
}
