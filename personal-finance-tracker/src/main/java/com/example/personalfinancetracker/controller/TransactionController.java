package com.example.personalfinancetracker.controller;

import com.example.personalfinancetracker.dto.TransactionDTO;
import com.example.personalfinancetracker.service.AccountService;
import com.example.personalfinancetracker.service.CategoryService;
import com.example.personalfinancetracker.service.TransactionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

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
            // For the form to add new transaction
            model.addAttribute("transactionDTO", new TransactionDTO());
            model.addAttribute("accounts", accountService.getAccountsByUserId(userId));
            model.addAttribute("categories", categoryService.getCategoriesByUserId(userId));
            // Pre-fill today's date for new transaction form
            model.addAttribute("currentDate", LocalDate.now());

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
            return "add-edit-transaction";
        } catch (IllegalStateException e) {
            return "redirect:/login";
        }
    }

    @PostMapping
    public String addTransaction(@ModelAttribute("transactionDTO") TransactionDTO transactionDTO,
                                 RedirectAttributes redirectAttributes,
                                 HttpSession session) {
        try {
            Long userId = getLoggedInUserId(session);
            transactionService.createTransaction(userId, transactionDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Transaction added successfully!");
            return "redirect:/transactions";
        } catch (IllegalStateException e) {
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/transactions/new";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditTransactionForm(@PathVariable("id") Long id,
                                          Model model,
                                          RedirectAttributes redirectAttributes,
                                          HttpSession session) {
        try {
            Long userId = getLoggedInUserId(session);
            transactionService.getTransactionByIdAndUserId(id, userId).ifPresentOrElse(
                    transactionDTO -> model.addAttribute("transactionDTO", transactionDTO),
                    () -> redirectAttributes.addFlashAttribute("errorMessage", "Transaction not found or not yours!")
            );
            model.addAttribute("accounts", accountService.getAccountsByUserId(userId));
            model.addAttribute("categories", categoryService.getCategoriesByUserId(userId));
            return "add-edit-transaction";
        } catch (IllegalStateException e) {
            return "redirect:/login";
        }
    }

    @PostMapping("/update/{id}")
    public String updateTransaction(@PathVariable("id") Long id,
                                    @ModelAttribute("transactionDTO") TransactionDTO transactionDTO,
                                    RedirectAttributes redirectAttributes,
                                    HttpSession session) {
        try {
            Long userId = getLoggedInUserId(session);
            transactionDTO.setId(id);
            transactionService.updateTransaction(userId, id, transactionDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Transaction updated successfully!");
            return "redirect:/transactions";
        } catch (IllegalStateException e) {
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/transactions/edit/" + id;
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