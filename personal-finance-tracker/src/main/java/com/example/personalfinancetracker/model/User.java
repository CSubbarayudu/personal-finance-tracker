package com.example.personalfinancetracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity // Marks this class as a JPA entity
@Table(name = "users") // Maps to the 'users' table in the database
@Data // Lombok annotation for getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok annotation for no-argument constructor
@AllArgsConstructor // Lombok annotation for all-argument constructor
public class User {

    @Id // Specifies the primary key of the entity
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Indicates that the primary key is auto-incremented by the database
    private Long id;

    @Column(nullable = false, unique = true) // Maps to a column, not null and unique
    private String username;

    @Column(nullable = false)
    private String password; // In a real app, this should be hashed (e.g., using Spring Security Bcrypt)

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "created_at", updatable = false) // 'updatable = false' means this column won't be updated on entity save
    private LocalDateTime createdAt;

    // One-to-Many relationship with Account: One user can have multiple accounts
    // 'mappedBy' indicates that the 'user' field in the Account entity is the owner of the relationship
    // 'cascade = CascadeType.ALL' means that if a User is deleted, all associated Accounts will also be deleted.
    // 'orphanRemoval = true' ensures that if an Account is removed from the user's accounts list, it's deleted from the DB.
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Account> accounts;

    // One-to-Many relationship with Category: One user can define multiple categories
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Category> categories;

    // One-to-Many relationship with Transaction: One user can have multiple transactions
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions;

    // One-to-Many relationship with Budget: One user can have multiple budgets
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Budget> budgets;

    @PrePersist // Callback method executed before the entity is first persisted (inserted)
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
