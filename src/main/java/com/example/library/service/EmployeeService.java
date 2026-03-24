package com.example.library.service;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.library.entity.Book;
import com.example.library.entity.Borrow;
import com.example.library.repository.BookRepository;
import com.example.library.repository.BorrowRepository;

@Service
public class EmployeeService {

    private static final long LOAN_PERIOD_DAYS = 14;

    private final BookRepository bookRepository;
    private final BorrowRepository borrowRepository;

    public EmployeeService(BookRepository bookRepository, BorrowRepository borrowRepository) {
        this.bookRepository = bookRepository;
        this.borrowRepository = borrowRepository;
    }

    @Transactional
    public Book createBook(String title, String author, int availableCopies) {
        Book book = new Book();
        book.setTitle(title.trim());
        book.setAuthor(author.trim());
        book.setAvailableCopies(availableCopies);
        return bookRepository.save(book);
    }

    @Transactional
    public Book updateBook(Long id, String title, String author, int availableCopies) {
        Book book = findBookOrThrow(id);
        book.setTitle(title.trim());
        book.setAuthor(author.trim());
        book.setAvailableCopies(availableCopies);
        return bookRepository.save(book);
    }

    @Transactional
    public Book adjustStock(Long id, int availableCopies) {
        Book book = findBookOrThrow(id);
        book.setAvailableCopies(availableCopies);
        return bookRepository.save(book);
    }

    @Transactional
    public void deleteBook(Long id) {
        Book book = findBookOrThrow(id);
        if (borrowRepository.existsByBookIdAndReturnDateIsNull(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete a book with active borrows");
        }
        bookRepository.delete(book);
    }

    @Transactional(readOnly = true)
    public Page<Book> getBooks(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return bookRepository.findAll(pageable);
        }
        String term = query.trim();
        return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(term, term, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Borrow> getActiveBorrows(Pageable pageable) {
        return borrowRepository.findByReturnDateIsNull(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Borrow> getOverdueBorrows(Pageable pageable) {
        LocalDate cutoffDate = LocalDate.now().minusDays(LOAN_PERIOD_DAYS);
        return borrowRepository.findByReturnDateIsNullAndBorrowDateBefore(cutoffDate, pageable);
    }

    private Book findBookOrThrow(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
    }
}
