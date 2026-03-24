package com.example.library.repository;

import com.example.library.entity.Borrow;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BorrowRepository extends JpaRepository<Borrow, Long> {

    long countByUserIdAndReturnDateIsNull(String userId);

    Optional<Borrow> findByIdAndReturnDateIsNull(Long id);

    List<Borrow> findByReturnDateIsNull();

    List<Borrow> findByUserIdAndReturnDateIsNull(String userId);
}
