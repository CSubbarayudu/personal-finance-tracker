package com.example.personalfinancetracker.repository;

import com.example.personalfinancetracker.model.Category;
import com.example.personalfinancetracker.model.TransactionType; // NEW IMPORT
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserId(Long userId);
    Optional<Category> findByIdAndUserId(Long id, Long userId);
    // NEW METHOD: Find categories by user ID and category type (enum)
    List<Category> findByUserIdAndCategoryType(Long userId, TransactionType categoryType);
}
