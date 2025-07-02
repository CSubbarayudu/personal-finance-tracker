package com.example.personalfinancetracker.service;

import com.example.personalfinancetracker.model.Budget;
import com.example.personalfinancetracker.model.BudgetPK;
import com.example.personalfinancetracker.model.Category;
import com.example.personalfinancetracker.model.User;
import com.example.personalfinancetracker.repository.BudgetRepository;
import com.example.personalfinancetracker.repository.CategoryRepository;
import com.example.personalfinancetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public BudgetService(BudgetRepository budgetRepository, UserRepository userRepository, CategoryRepository categoryRepository) {
        this.budgetRepository = budgetRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public Budget createOrUpdateBudget(Long userId, Long categoryId, LocalDate budgetMonth, BigDecimal amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found or does not belong to user."));

        // Ensure budgetMonth is always the first day of the month
        LocalDate normalizedBudgetMonth = budgetMonth.withDayOfMonth(1);

        Optional<Budget> existingBudget = budgetRepository.findByUserIdAndCategoryIdAndBudgetMonth(
                userId, categoryId, normalizedBudgetMonth);

        Budget budget;
        if (existingBudget.isPresent()) {
            budget = existingBudget.get();
            budget.setAmount(amount);
        } else {
            budget = new Budget();
            budget.setUser(user);
            budget.setCategory(category);
            budget.setBudgetMonth(normalizedBudgetMonth);
            budget.setAmount(amount);
        }
        return budgetRepository.save(budget);
    }

    public List<Budget> getBudgetsByUserIdAndMonth(Long userId, LocalDate month) {
        LocalDate normalizedMonth = month.withDayOfMonth(1);
        return budgetRepository.findByUserIdAndBudgetMonth(userId, normalizedMonth);
    }

    public Optional<Budget> getBudgetByIds(Long userId, Long categoryId, LocalDate month) {
        LocalDate normalizedMonth = month.withDayOfMonth(1);
        return budgetRepository.findByUserIdAndCategoryIdAndBudgetMonth(userId, categoryId, normalizedMonth);
    }

    @Transactional
    public void deleteBudget(Long userId, Long categoryId, LocalDate month) {
        LocalDate normalizedMonth = month.withDayOfMonth(1);
        BudgetPK pk = new BudgetPK(userId, categoryId, normalizedMonth);
        if (!budgetRepository.existsById(pk)) {
            throw new IllegalArgumentException("Budget not found or does not belong to user.");
        }
        budgetRepository.deleteById(pk);
    }
}
