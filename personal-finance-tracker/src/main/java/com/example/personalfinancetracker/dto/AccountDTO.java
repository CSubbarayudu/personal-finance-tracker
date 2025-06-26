package com.example.personalfinancetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {
    private Long id; // Null for creation, populated for updates
    private String accountName;
    private String accountType;
    private BigDecimal balance;
    private String currency;
    // user_id will be handled by the service based on the logged-in user
}
