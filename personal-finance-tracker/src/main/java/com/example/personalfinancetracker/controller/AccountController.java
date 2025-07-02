package com.example.personalfinancetracker.controller;

import com.example.personalfinancetracker.dto.AccountDTO;
import com.example.personalfinancetracker.service.AccountService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    private Long getLoggedInUserId(HttpSession session) {
        Long userId = (Long) session.getAttribute("loggedInUserId");
        if (userId == null) {
            throw new IllegalStateException("User not logged in.");
        }
        return userId;
    }

    // Display all accounts for the logged-in user
    @GetMapping
    public String listAccounts(Model model, HttpSession session) {
        try {
            Long userId = getLoggedInUserId(session);
            List<AccountDTO> accounts = accountService.getAccountsByUserId(userId);
            model.addAttribute("accounts", accounts);
            model.addAttribute("accountDTO", new AccountDTO()); // For the add new account form
            return "accounts"; // Refers to src/main/resources/templates/accounts.html
        } catch (IllegalStateException e) {
            return "redirect:/login";
        }
    }

    // Display form to add a new account
    @GetMapping("/new")
    public String showAddAccountForm(Model model, HttpSession session) {
        try {
            getLoggedInUserId(session); // Just to check if logged in
            model.addAttribute("accountDTO", new AccountDTO());
            return "add-edit-account"; // A generic form for both add and edit
        } catch (IllegalStateException e) {
            return "redirect:/login";
        }
    }

    // Handle adding a new account
    @PostMapping
    public String addAccount(@ModelAttribute("accountDTO") AccountDTO accountDTO,
                             RedirectAttributes redirectAttributes,
                             HttpSession session) {
        try {
            Long userId = getLoggedInUserId(session);
            accountService.createAccount(userId, accountDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Account added successfully!");
            return "redirect:/accounts";
        } catch (IllegalStateException e) {
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/accounts/new"; // Redirect back to form with error
        }
    }

    // Display form to edit an existing account
    @GetMapping("/edit/{id}")
    public String showEditAccountForm(@PathVariable("id") Long id,
                                      Model model,
                                      RedirectAttributes redirectAttributes,
                                      HttpSession session) {
        try {
            Long userId = getLoggedInUserId(session);
            accountService.getAccountByIdAndUserId(id, userId).ifPresentOrElse(
                    accountDTO -> model.addAttribute("accountDTO", accountDTO),
                    () -> redirectAttributes.addFlashAttribute("errorMessage", "Account not found or not yours!")
            );
            return "add-edit-account";
        } catch (IllegalStateException e) {
            return "redirect:/login";
        }
    }

    // Handle updating an existing account
    @PostMapping("/update/{id}")
    public String updateAccount(@PathVariable("id") Long id,
                                @ModelAttribute("accountDTO") AccountDTO accountDTO,
                                RedirectAttributes redirectAttributes,
                                HttpSession session) {
        try {
            Long userId = getLoggedInUserId(session);
            accountDTO.setId(id); // Ensure the ID is set for update
            accountService.updateAccount(userId, id, accountDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Account updated successfully!");
            return "redirect:/accounts";
        } catch (IllegalStateException e) {
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/accounts/edit/" + id; // Redirect back to form with error
        }
    }

    // Handle deleting an account
    @GetMapping("/delete/{id}")
    public String deleteAccount(@PathVariable("id") Long id,
                                RedirectAttributes redirectAttributes,
                                HttpSession session) {
        try {
            Long userId = getLoggedInUserId(session);
            accountService.deleteAccount(userId, id);
            redirectAttributes.addFlashAttribute("successMessage", "Account deleted successfully!");
            return "redirect:/accounts";
        } catch (IllegalStateException e) {
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/accounts";
        }
    }
}
