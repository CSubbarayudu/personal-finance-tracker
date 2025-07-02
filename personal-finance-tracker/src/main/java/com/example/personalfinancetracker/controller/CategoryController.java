package com.example.personalfinancetracker.controller;

import com.example.personalfinancetracker.dto.CategoryDTO;
import com.example.personalfinancetracker.service.CategoryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

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
            model.addAttribute("categoryDTO", new CategoryDTO()); // For the add new category form
            return "categories";
        } catch (IllegalStateException e) {
            return "redirect:/login";
        }
    }

    @GetMapping("/new")
    public String showAddCategoryForm(Model model, HttpSession session) {
        try {
            getLoggedInUserId(session);
            model.addAttribute("categoryDTO", new CategoryDTO());
            return "add-edit-category";
        } catch (IllegalStateException e) {
            return "redirect:/login";
        }
    }

    @PostMapping
    public String addCategory(@ModelAttribute("categoryDTO") CategoryDTO categoryDTO,
                              RedirectAttributes redirectAttributes,
                              HttpSession session) {
        try {
            Long userId = getLoggedInUserId(session);
            categoryService.createCategory(userId, categoryDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Category added successfully!");
            return "redirect:/categories";
        } catch (IllegalStateException e) {
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/categories/new";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditCategoryForm(@PathVariable("id") Long id,
                                       Model model,
                                       RedirectAttributes redirectAttributes,
                                       HttpSession session) {
        try {
            Long userId = getLoggedInUserId(session);
            categoryService.getCategoryByIdAndUserId(id, userId).ifPresentOrElse(
                    categoryDTO -> model.addAttribute("categoryDTO", categoryDTO),
                    () -> redirectAttributes.addFlashAttribute("errorMessage", "Category not found or not yours!")
            );
            return "add-edit-category";
        } catch (IllegalStateException e) {
            return "redirect:/login";
        }
    }

    @PostMapping("/update/{id}")
    public String updateCategory(@PathVariable("id") Long id,
                                 @ModelAttribute("categoryDTO") CategoryDTO categoryDTO,
                                 RedirectAttributes redirectAttributes,
                                 HttpSession session) {
        try {
            Long userId = getLoggedInUserId(session);
            categoryDTO.setId(id);
            categoryService.updateCategory(userId, id, categoryDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Category updated successfully!");
            return "redirect:/categories";
        } catch (IllegalStateException e) {
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/categories/edit/" + id;
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id,
                                 RedirectAttributes redirectAttributes,
                                 HttpSession session) {
        try {
            Long userId = getLoggedInUserId(session);
            categoryService.deleteCategory(userId, id);
            redirectAttributes.addFlashAttribute("successMessage", "Category deleted successfully!");
            return "redirect:/categories";
        } catch (IllegalStateException e) {
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/categories";
        }
    }
}
