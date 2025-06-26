package com.example.personalfinancetracker.repository;

import com.example.personalfinancetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // Marks this interface as a Spring Data JPA repository component
public interface UserRepository extends JpaRepository<User, Long> {
    // JpaRepository<T, ID> provides CRUD operations for entity T with ID type ID.

    // Custom query method to find a user by username
    Optional<User> findByUsername(String username);

    // Custom query method to find a user by email
    Optional<User> findByEmail(String email);
}
