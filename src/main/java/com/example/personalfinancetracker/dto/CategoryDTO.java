package com.example.personalfinancetracker.dto;

import com.example.personalfinancetracker.model.TransactionType; // NEW IMPORT
import lombok.Data;

@Data
public class CategoryDTO {
    private Long id;
    private String categoryName;
    private TransactionType categoryType; // CHANGED TO ENUM
}
