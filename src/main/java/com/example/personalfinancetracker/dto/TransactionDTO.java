package com.example.personalfinancetracker.dto;

import com.example.personalfinancetracker.model.TransactionType;
import jakarta.validation.constraints.DecimalMin; // NEW IMPORT
import jakarta.validation.constraints.NotNull; // NEW IMPORT
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private Long id;
    @NotNull(message = "Account is required")
    private Long accountId;
    @NotNull(message = "Category is required")
    private Long categoryId;
    @NotNull(message = "Transaction date is required")
    private LocalDate transactionDate;
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType; // This is now correctly an enum
    private String description;
}
