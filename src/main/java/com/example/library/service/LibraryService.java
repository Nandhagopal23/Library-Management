package com.example.library.service;

import com.example.library.entity.Book;
import com.example.library.entity.Borrow;
import com.example.library.entity.User;
import com.example.library.repository.BookRepository;
import com.example.library.repository.BorrowRepository;
import com.example.library.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LibraryService {

    private static final int MAX_BORROWS_PER_USER = 3;
    private static final long LOAN_PERIOD_DAYS = 14;
    private static final BigDecimal PENALTY_PER_LATE_DAY = new BigDecimal("2.00");

    private final BookRepository bookRepository;
    private final BorrowRepository borrowRepository;
    private final UserRepository userRepository;

    public LibraryService(BookRepository bookRepository, BorrowRepository borrowRepository, UserRepository userRepository) {
        this.bookRepository = bookRepository;
        this.borrowRepository = borrowRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Borrow borrowBook(String requesterEmail, Long bookId) {
        String normalizedEmail = normalizeEmail(requesterEmail);
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        long activeBorrows = borrowRepository.countByUserEmailIgnoreCaseAndReturnDateIsNull(normalizedEmail);
        if (activeBorrows >= MAX_BORROWS_PER_USER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Max 3 books per user reached");
        }

        Book book = bookRepository.findWithLockingById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

        if (book.getAvailableCopies() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot borrow: no copies available");
        }

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        Borrow borrow = new Borrow();
        borrow.setUser(user);
        borrow.setBook(book);
        borrow.setBorrowDate(LocalDate.now());
        borrow.setLatePenalty(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));

        return borrowRepository.save(borrow);
    }

    @Transactional
    public Borrow returnBook(Long borrowId, String requesterEmail, boolean isEmployee) {
        String normalizedEmail = normalizeEmail(requesterEmail);
        Borrow borrow = borrowRepository.findByIdAndReturnDateIsNull(borrowId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Active borrow record not found"));

        if (!isEmployee && !borrow.getUser().getEmail().equalsIgnoreCase(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only return your own borrowed books");
        }

        LocalDate returnDate = LocalDate.now();
        borrow.setReturnDate(returnDate);

        long lateDays = Math.max(0, ChronoUnit.DAYS.between(borrow.getBorrowDate().plusDays(LOAN_PERIOD_DAYS), returnDate));
        BigDecimal penalty = PENALTY_PER_LATE_DAY.multiply(BigDecimal.valueOf(lateDays));
        borrow.setLatePenalty(penalty.setScale(2, RoundingMode.HALF_UP));

        Book book = borrow.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        return borrowRepository.save(borrow);
    }

    @Transactional(readOnly = true)
    public List<Borrow> getBorrowedBooks(String requesterEmail, boolean isEmployee) {
        if (isEmployee) {
            return borrowRepository.findByReturnDateIsNull();
        }
        return borrowRepository.findByUserEmailIgnoreCaseAndReturnDateIsNull(normalizeEmail(requesterEmail));
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return email.trim().toLowerCase();
    }
}
