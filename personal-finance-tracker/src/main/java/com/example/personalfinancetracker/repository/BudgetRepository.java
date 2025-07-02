package com.example.personalfinancetracker.repository;

import com.example.personalfinancetracker.model.Budget;
import com.example.personalfinancetracker.model.BudgetPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, BudgetPK> {
    // Find all budgets for a specific user
    List<Budget> findByUserId(Long userId);

    // Find a specific budget by user, category and month
    Optional<Budget> findByUserIdAndCategoryIdAndBudgetMonth(Long userId, Long categoryId, LocalDate budgetMonth);

    // Find budgets for a user within a specific month
    List<Budget> findByUserIdAndBudgetMonth(Long userId, LocalDate budgetMonth);
}
