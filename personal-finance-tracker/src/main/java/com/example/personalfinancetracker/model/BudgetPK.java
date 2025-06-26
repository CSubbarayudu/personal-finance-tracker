package com.example.personalfinancetracker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetPK implements Serializable {
    private Long user; // Corresponds to the 'id' of the User entity
    private Long category; // Corresponds to the 'id' of the Category entity
    private LocalDate budgetMonth;
}
