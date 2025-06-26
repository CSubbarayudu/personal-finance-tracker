package com.example.personalfinancetracker.service;

import com.example.personalfinancetracker.dto.CategoryDTO;
import com.example.personalfinancetracker.model.Category;
import com.example.personalfinancetracker.model.User;
import com.example.personalfinancetracker.repository.CategoryRepository;
import com.example.personalfinancetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Category createCategory(Long userId, CategoryDTO categoryDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // Prevent duplicate category names for the same user
        if (categoryRepository.findByCategoryNameAndUserId(categoryDTO.getCategoryName(), userId).isPresent()) {
            throw new IllegalArgumentException("Category with this name already exists for this user.");
        }

        Category category = new Category();
        category.setUser(user);
        category.setCategoryName(categoryDTO.getCategoryName());
        category.setCategoryType(categoryDTO.getCategoryType()); // "Income" or "Expense"

        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Long userId, Long categoryId, CategoryDTO categoryDTO) {
        Category existingCategory = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found or does not belong to user."));

        // Check for duplicate name if name is changed
        if (!existingCategory.getCategoryName().equals(categoryDTO.getCategoryName())) {
            if (categoryRepository.findByCategoryNameAndUserId(categoryDTO.getCategoryName(), userId).isPresent()) {
                throw new IllegalArgumentException("Another category with this name already exists for this user.");
            }
        }

        existingCategory.setCategoryName(categoryDTO.getCategoryName());
        existingCategory.setCategoryType(categoryDTO.getCategoryType());

        return categoryRepository.save(existingCategory);
    }

    public List<CategoryDTO> getCategoriesByUserId(Long userId) {
        return categoryRepository.findByUserId(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<CategoryDTO> getCategoryByIdAndUserId(Long categoryId, Long userId) {
        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .map(this::convertToDto);
    }

    @Transactional
    public void deleteCategory(Long userId, Long categoryId) {
        Category categoryToDelete = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found or does not belong to user."));
        // Note: Due to ON DELETE RESTRICT on transactions table, this will fail if category has associated transactions.
        categoryRepository.delete(categoryToDelete);
    }

    private CategoryDTO convertToDto(Category category) {
        return new CategoryDTO(
                category.getId(),
                category.getCategoryName(),
                category.getCategoryType()
        );
    }
}
