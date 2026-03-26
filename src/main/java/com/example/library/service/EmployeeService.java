package com.example.library.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.library.entity.Book;
import com.example.library.entity.User;
import com.example.library.entity.UserRole;
import com.example.library.repository.BookRepository;
import com.example.library.repository.BorrowRepository;
import com.example.library.repository.UserRepository;

@Service
public class EmployeeService {

    private final BookRepository bookRepository;
    private final BorrowRepository borrowRepository;
    private final UserRepository userRepository;

    public EmployeeService(BookRepository bookRepository, BorrowRepository borrowRepository, UserRepository userRepository) {
        this.bookRepository = bookRepository;
        this.borrowRepository = borrowRepository;
        this.userRepository = userRepository;
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
    public Page<User> getUsers(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return userRepository.findAll(pageable);
        }
        String term = query.trim();
        return userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(term, term, pageable);
    }

    public User getUserById(Long userId) {
        return findUserOrThrow(userId);
    }

    @Transactional
    public User updateUserRole(Long userId, UserRole role) {
        User user = findUserOrThrow(userId);
        user.setRole(role);
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = findUserOrThrow(userId);
        if (borrowRepository.existsByUserIdAndReturnDateIsNull(userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete user with active borrows");
        }
        userRepository.delete(user);
    }

    private Book findBookOrThrow(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
