package com.example.personalfinancetracker.controller;

import com.example.personalfinancetracker.dto.CategoryDTO;
import com.example.personalfinancetracker.model.Category;
import com.example.personalfinancetracker.model.TransactionType; // NEW IMPORT
import com.example.personalfinancetracker.service.CategoryService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays; // Needed for Arrays.asList()
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    private Long getLoggedInUserId(HttpSession session) {
        Long userId = (Long) session.getAttribute("loggedInUserId");
        if (userId == null) {
            throw new IllegalStateException("User not logged in.");
        }
        return userId;
    }

    @GetMapping
    public String listCategories(Model model, HttpSession session) {
        try {
            Long userId = getLoggedInUserId(session);
            List<CategoryDTO> categories = categoryService.getCategoriesByUserId(userId);
            model.addAttribute("categories", categories);
            return "categories"; // Refers to categories.html
        } catch (IllegalStateException e) {
            return "redirect:/login";
        }
    }

    @GetMapping("/new")
    public String showAddEditCategoryForm(@RequestParam(value = "id", required = false) Long categoryId, Model model, HttpSession session) {
        Long userId = getLoggedInUserId(session);
        CategoryDTO categoryDTO = new CategoryDTO();
        String formTitle = "Add New Category";

        if (categoryId != null) {
            Optional<CategoryDTO> existingCategory = categoryService.getCategoryByIdAndUserId(categoryId, userId);
            if (existingCategory.isPresent()) {
                categoryDTO = existingCategory.get();
                formTitle = "Edit Category";
            } else {
                model.addAttribute("errorMessage", "Category not found or unauthorized.");
                categoryId = null; // Treat as new if not found/authorized
            }
        }

        model.addAttribute("categoryDTO", categoryDTO);
        // NEW: Pass enum values to the form for dropdown
        model.addAttribute("categoryTypes", Arrays.asList(TransactionType.values()));
        model.addAttribute("formTitle", formTitle);
        model.addAttribute("isEdit", categoryId != null);
        return "add-edit-category";
    }

    @PostMapping("/save")
    public String processAddCategory(@Valid @ModelAttribute("categoryDTO") CategoryDTO categoryDTO,
                                     BindingResult result,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        Long userId = getLoggedInUserId(session);

        if (result.hasErrors()) {
            // If there are validation errors, return to the form with errors
            redirectAttributes.addFlashAttribute("categoryTypes", Arrays.asList(TransactionType.values())); // Re-add types on error
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.categoryDTO", result);
            redirectAttributes.addFlashAttribute("categoryDTO", categoryDTO);
            redirectAttributes.addFlashAttribute("errorMessage", "Please correct the form errors.");
            return "redirect:/categories/new?id=" + (categoryDTO.getId() != null ? categoryDTO.getId() : "");
        }

        try {
            if (categoryDTO.getId() == null) {
                categoryService.createCategory(userId, categoryDTO);
                redirectAttributes.addFlashAttribute("successMessage", "Category added successfully!");
            } else {
                categoryService.updateCategory(userId, categoryDTO.getId(), categoryDTO);
                redirectAttributes.addFlashAttribute("successMessage", "Category updated successfully!");
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/categories/new?id=" + (categoryDTO.getId() != null ? categoryDTO.getId() : "");
        }
        return "redirect:/categories";
    }

    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            Long userId = getLoggedInUserId(session);
            categoryService.deleteCategory(userId, id);
            redirectAttributes.addFlashAttribute("successMessage", "Category deleted successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/categories";
    }
}
