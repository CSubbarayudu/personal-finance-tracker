package com.example.personalfinancetracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many-to-One relationship with User: Multiple accounts can belong to one user
    // '@JoinColumn' specifies the foreign key column in the 'accounts' table that links to the 'users' table
    @ManyToOne(fetch = FetchType.LAZY) // LAZY fetch type means the associated User is loaded only when accessed
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "account_name", nullable = false)
    private String accountName;

    @Column(name = "account_type", nullable = false)
    private String accountType; // e.g., "Checking", "Savings", "Credit Card"

    @Column(nullable = false, precision = 19, scale = 2) // Precision and scale for DECIMAL type
    private BigDecimal balance;

    @Column(nullable = false)
    private String currency;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist // Set creation timestamp before persisting
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate // Set update timestamp before updating
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
