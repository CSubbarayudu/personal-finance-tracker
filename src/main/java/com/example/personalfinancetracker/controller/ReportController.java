package com.example.personalfinancetracker.controller;

import com.example.personalfinancetracker.dto.ChartDataDTO;
import com.example.personalfinancetracker.service.TransactionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final TransactionService transactionService;

    @Autowired
    public ReportController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // Helper method to get logged-in user ID, similar to other controllers
    private Long getLoggedInUserId(HttpSession session) {
        Long userId = (Long) session.getAttribute("loggedInUserId");
        if (userId == null) {
            throw new IllegalStateException("User not logged in.");
        }
        return userId;
    }

    /**
     * Displays the reports page with expense distribution by category and overall transaction type distribution.
     * Fetches data from the TransactionService and adds it to the model for Thymeleaf.
     *
     * @param model The Spring Model to add attributes to.
     * @param session The HttpSession to retrieve the logged-in user ID.
     * @return The name of the Thymeleaf template to render (e.g., "reports").
     */
    @GetMapping
    public String showReports(Model model, HttpSession session) {
        try {
            Long userId = getLoggedInUserId(session);

            // Fetch expense distribution data for the pie chart (by category)
            List<ChartDataDTO> expenseData = transactionService.getExpenseDistributionByCategory(userId);
            model.addAttribute("expenseData", expenseData); // Pass this data to the HTML template

            // NEW: Fetch overall transaction type distribution (Income vs Expense)
            List<ChartDataDTO> transactionTypeData = transactionService.getTransactionTypeDistribution(userId);
            model.addAttribute("transactionTypeData", transactionTypeData); // Pass this new data

            return "reports"; // Refers to src/main/resources/templates/reports.html
        } catch (IllegalStateException e) {
            // Redirect to login if user is not logged in
            return "redirect:/login";
        } catch (Exception e) {
            // Log the error and show a generic error message or redirect
            System.err.println("Error fetching report data: " + e.getMessage());
            model.addAttribute("errorMessage", "Could not load reports. Please try again.");
            return "reports"; // Stay on the reports page but show an error
        }
    }
}
