package com.example.personalfinancetracker.repository;

import com.example.personalfinancetracker.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Find all categories belonging to a specific user
    List<Category> findByUserId(Long userId);

    // Find a category by its ID and ensuring it belongs to a specific user
    Optional<Category> findByIdAndUserId(Long id, Long userId);

    // Find a category by name and user (to prevent duplicate categories for a user)
    Optional<Category> findByCategoryNameAndUserId(String categoryName, Long userId);
}