package com.example.personalfinancetracker.service;

import com.example.personalfinancetracker.dto.CategoryDTO;
import com.example.personalfinancetracker.model.Category;
import com.example.personalfinancetracker.model.User;
import com.example.personalfinancetracker.model.TransactionType; // NEW IMPORT for enum
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

        Category category = new Category();
        category.setUser(user);
        category.setCategoryName(categoryDTO.getCategoryName());
        category.setCategoryType(categoryDTO.getCategoryType()); // Uses enum
        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Long userId, Long categoryId, CategoryDTO categoryDTO) {
        Category existingCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found."));

        if (!existingCategory.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Category does not belong to the authenticated user.");
        }

        existingCategory.setCategoryName(categoryDTO.getCategoryName());
        existingCategory.setCategoryType(categoryDTO.getCategoryType()); // Uses enum
        return categoryRepository.save(existingCategory);
    }

    public void deleteCategory(Long userId, Long categoryId) {
        Category categoryToDelete = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found."));

        if (!categoryToDelete.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Category does not belong to the authenticated user.");
        }
        categoryRepository.delete(categoryToDelete);
    }

    public List<CategoryDTO> getCategoriesByUserId(Long userId) {
        return categoryRepository.findByUserId(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<CategoryDTO> getCategoryByIdAndUserId(Long categoryId, Long userId) {
        return categoryRepository.findById(categoryId)
                .filter(c -> c.getUser().getId().equals(userId))
                .map(this::convertToDto);
    }

    // New method to get categories by user and type (e.g., for filtering in forms)
    public List<Category> getCategoriesByUserIdAndType(Long userId, TransactionType type) {
        return categoryRepository.findByUserIdAndCategoryType(userId, type);
    }

    private CategoryDTO convertToDto(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setCategoryName(category.getCategoryName());
        dto.setCategoryType(category.getCategoryType()); // Uses enum
        return dto;
    }
}
