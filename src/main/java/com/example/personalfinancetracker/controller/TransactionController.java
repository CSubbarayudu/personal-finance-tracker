package com.example.personalfinancetracker.controller;

import com.example.personalfinancetracker.dto.TransactionDTO;
import com.example.personalfinancetracker.model.Account; // Ensure Account is imported
import com.example.personalfinancetracker.model.Category; // Ensure Category is imported
import com.example.personalfinancetracker.model.TransactionType; // NEW IMPORT for enum
import com.example.personalfinancetracker.service.AccountService;
import com.example.personalfinancetracker.service.CategoryService;
import com.example.personalfinancetracker.service.TransactionService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid; // NEW IMPORT
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult; // NEW IMPORT
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Arrays; // Needed for Arrays.asList()
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final AccountService accountService;
    private final CategoryService categoryService;

    @Autowired
    public TransactionController(TransactionService transactionService,
                                 AccountService accountService,
                                 CategoryService categoryService) {
        this.transactionService = transactionService;
        this.accountService = accountService;
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
    public String listTransactions(Model model, HttpSession session) {
        try {
            Long userId = getLoggedInUserId(session);
            model.addAttribute("transactions", transactionService.getTransactionsByUserId(userId));
            // For the form to add new transaction - these are for the main transactions page, not add-edit form
            model.addAttribute("transactionDTO", new TransactionDTO());
            model.addAttribute("accounts", accountService.getAccountsByUserId(userId));
            model.addAttribute("categories", categoryService.getCategoriesByUserId(userId));
            model.addAttribute("currentDate", LocalDate.now());
            // Pass transaction types to the main list page if needed for filtering, etc.
            model.addAttribute("transactionTypes", Arrays.asList(TransactionType.values()));

            return "transactions";
        } catch (IllegalStateException e) {
            return "redirect:/login";
        }
    }

    @GetMapping("/new")
    public String showAddTransactionForm(Model model, HttpSession session) {
        try {
            Long userId = getLoggedInUserId(session);
            model.addAttribute("transactionDTO", new TransactionDTO());
            model.addAttribute("accounts", accountService.getAccountsByUserId(userId));
            model.addAttribute("categories", categoryService.getCategoriesByUserId(userId));
            model.addAttribute("currentDate", LocalDate.now());
            // IMPORTANT: Pass enum values to the form for dropdown
            model.addAttribute("transactionTypes", Arrays.asList(TransactionType.values()));
            return "add-edit-transaction";
        } catch (IllegalStateException e) {
            return "redirect:/login";
        }
    }

    // IMPORTANT CHANGE: Mapped to /save to match HTML form action
    @PostMapping("/save")
    public String addTransaction(@Valid @ModelAttribute("transactionDTO") TransactionDTO transactionDTO, // ADD @Valid
                                 BindingResult result, // ADD BindingResult
                                 RedirectAttributes redirectAttributes,
                                 HttpSession session) {
        Long userId = getLoggedInUserId(session);

        if (result.hasErrors()) {
            // If there are validation errors, return to the form with errors
            // Need to re-add accounts, categories, and transaction types to the model
            redirectAttributes.addFlashAttribute("accounts", accountService.getAccountsByUserId(userId));
            redirectAttributes.addFlashAttribute("categories", categoryService.getCategoriesByUserId(userId));
            redirectAttributes.addFlashAttribute("transactionTypes", Arrays.asList(TransactionType.values()));
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.transactionDTO", result); // For Thymeleaf to show errors
            redirectAttributes.addFlashAttribute("transactionDTO", transactionDTO); // To retain form data
            redirectAttributes.addFlashAttribute("errorMessage", "Please correct the form errors.");
            return "redirect:/transactions/new"; // Redirect back to the new transaction form
        }

        try {
            transactionService.createTransaction(userId, transactionDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Transaction added successfully!");
            return "redirect:/transactions";
        } catch (IllegalStateException e) {
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/transactions/new"; // Redirect back to form with error
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditTransactionForm(@PathVariable("id") Long id,
                                          Model model,
                                          RedirectAttributes redirectAttributes,
                                          HttpSession session) {
        try {
            Long userId = getLoggedInUserId(session);
            Optional<TransactionDTO> existingTransaction = transactionService.getTransactionByIdAndUserId(id, userId);
            if (existingTransaction.isPresent()) {
                model.addAttribute("transactionDTO", existingTransaction.get());
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Transaction not found or not yours!");
                return "redirect:/transactions"; // Redirect to list if not found
            }
            model.addAttribute("accounts", accountService.getAccountsByUserId(userId));
            model.addAttribute("categories", categoryService.getCategoriesByUserId(userId));
            // IMPORTANT: Pass enum values to the form for dropdown
            model.addAttribute("transactionTypes", Arrays.asList(TransactionType.values()));
            return "add-edit-transaction";
        } catch (IllegalStateException e) {
            return "redirect:/login";
        }
    }

    @PostMapping("/update/{id}")
    public String updateTransaction(@PathVariable("id") Long id,
                                    @Valid @ModelAttribute("transactionDTO") TransactionDTO transactionDTO, // ADD @Valid
                                    BindingResult result, // ADD BindingResult
                                    RedirectAttributes redirectAttributes,
                                    HttpSession session) {
        Long userId = getLoggedInUserId(session);

        if (result.hasErrors()) {
            // If there are validation errors, return to the form with errors
            // Need to re-add accounts, categories, and transaction types to the model
            redirectAttributes.addFlashAttribute("accounts", accountService.getAccountsByUserId(userId));
            redirectAttributes.addFlashAttribute("categories", categoryService.getCategoriesByUserId(userId));
            redirectAttributes.addFlashAttribute("transactionTypes", Arrays.asList(TransactionType.values()));
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.transactionDTO", result);
            redirectAttributes.addFlashAttribute("transactionDTO", transactionDTO);
            redirectAttributes.addFlashAttribute("errorMessage", "Please correct the form errors.");
            return "redirect:/transactions/edit/" + id; // Redirect back to the edit form
        }

        try {
            transactionDTO.setId(id);
            transactionService.updateTransaction(userId, id, transactionDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Transaction updated successfully!");
            return "redirect:/transactions";
        } catch (IllegalStateException e) {
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/transactions/edit/" + id; // Redirect back to form with error
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteTransaction(@PathVariable("id") Long id,
                                    RedirectAttributes redirectAttributes,
                                    HttpSession session) {
        try {
            Long userId = getLoggedInUserId(session);
            transactionService.deleteTransaction(userId, id);
            redirectAttributes.addFlashAttribute("successMessage", "Transaction deleted successfully!");
            return "redirect:/transactions";
        } catch (IllegalStateException e) {
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/transactions";
        }
    }
}
