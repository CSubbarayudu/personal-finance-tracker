package com.example.personalfinancetracker.repository;

import com.example.personalfinancetracker.model.Category; // Ensure this import is present if Category is used
import com.example.personalfinancetracker.model.Transaction;
import com.example.personalfinancetracker.model.TransactionType; // IMPORTANT: Ensure this import is present
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // Find all transactions for a specific user, ordered by date descending
    List<Transaction> findByUserIdOrderByTransactionDateDesc(Long userId);

    // Find all transactions for a specific user
    List<Transaction> findByUserId(Long userId);

    // Find transactions for a specific account and user
    List<Transaction> findByAccountIdAndUserIdOrderByTransactionDateDesc(Long accountId, Long userId);

    // Find transactions for a specific category and user
    List<Transaction> findByCategoryIdAndUserIdOrderByTransactionDateDesc(Long categoryId, Long userId);

    // Find transactions within a date range for a user
    List<Transaction> findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(Long userId, LocalDate startDate, LocalDate endDate);

    // THIS IS THE CRUCIAL CHANGE: Parameter type changed from String to TransactionType enum
    List<Transaction> findByUserIdAndTransactionType(Long userId, TransactionType transactionType);
}
