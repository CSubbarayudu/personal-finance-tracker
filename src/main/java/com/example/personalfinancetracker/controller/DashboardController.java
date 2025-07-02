package com.example.personalfinancetracker.controller;

import com.example.personalfinancetracker.model.User;
import com.example.personalfinancetracker.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final UserService userService;

    @Autowired
    public DashboardController(UserService userService) {
        this.userService = userService;
    }

    // Interceptor or aspect would typically handle this, but for simplicity, check session here
    private Long getLoggedInUserId(HttpSession session) {
        Long userId = (Long) session.getAttribute("loggedInUserId");
        if (userId == null) {
            throw new IllegalStateException("User not logged in."); // Or redirect to login
        }
        return userId;
    }

    @GetMapping
    public String showDashboard(Model model, HttpSession session) {
        try {
            Long userId = getLoggedInUserId(session);
            String username = (String) session.getAttribute("loggedInUsername");
            model.addAttribute("username", username);

            // You can add more data here, e.g., summary of accounts, recent transactions etc.
            // For example:
            // List<AccountDTO> accounts = accountService.getAccountsByUserId(userId);
            // model.addAttribute("accounts", accounts);
            // List<TransactionDTO> recentTransactions = transactionService.getRecentTransactions(userId);
            // model.addAttribute("recentTransactions", recentTransactions);

            return "dashboard"; // Refers to src/main/resources/templates/dashboard.html
        } catch (IllegalStateException e) {
            return "redirect:/login"; // Redirect to login if not authenticated
        }
    }
}
