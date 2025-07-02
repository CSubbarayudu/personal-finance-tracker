package com.example.personalfinancetracker.repository;

import com.example.personalfinancetracker.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    // Find all accounts belonging to a specific user
    List<Account> findByUserId(Long userId);

    // Find an account by its ID and ensuring it belongs to a specific user
    Optional<Account> findByIdAndUserId(Long id, Long userId);
}
