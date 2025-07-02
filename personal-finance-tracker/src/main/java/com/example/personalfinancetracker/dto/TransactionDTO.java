package com.example.personalfinancetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private Long id; // Null for creation, populated for updates
    private Long accountId;
    private Long categoryId;

    @DateTimeFormat(pattern = "yyyy-MM-dd") // Format for date input from forms
    private LocalDate transactionDate;
    private BigDecimal amount;
    private String transactionType; // "Income" or "Expense"
    private String description;
    // user_id will be handled by the service
}
