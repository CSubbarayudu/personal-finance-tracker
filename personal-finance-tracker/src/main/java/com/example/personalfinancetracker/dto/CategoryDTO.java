package com.example.personalfinancetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private Long id; // Null for creation, populated for updates
    private String categoryName;
    private String categoryType; // "Income" or "Expense"
    // user_id will be handled by the service
}
