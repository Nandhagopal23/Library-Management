package com.example.library.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.library.entity.Borrow;

public interface BorrowRepository extends JpaRepository<Borrow, Long> {

    long countByUserEmailIgnoreCaseAndReturnDateIsNull(String email);

    Optional<Borrow> findByIdAndReturnDateIsNull(Long id);

    List<Borrow> findByReturnDateIsNull();

    Page<Borrow> findByReturnDateIsNull(Pageable pageable);

    Page<Borrow> findByReturnDateIsNullAndBorrowDateBefore(LocalDate date, Pageable pageable);

    List<Borrow> findByUserEmailIgnoreCaseAndReturnDateIsNull(String email);

    boolean existsByBookIdAndReturnDateIsNull(Long bookId);
}
