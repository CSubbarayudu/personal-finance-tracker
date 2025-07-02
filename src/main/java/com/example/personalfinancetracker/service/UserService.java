package com.example.personalfinancetracker.service;

import com.example.personalfinancetracker.dto.UserRegistrationDTO;
import com.example.personalfinancetracker.model.User;
import com.example.personalfinancetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service // Marks this class as a Spring service component
public class UserService {

    private final UserRepository userRepository;

    @Autowired // Injects an instance of UserRepository
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional // Ensures the entire method runs within a single database transaction
    public User registerUser(UserRegistrationDTO registrationDTO) {
        // In a real application, you would encrypt the password here
        // For example: passwordEncoder.encode(registrationDTO.getPassword())

        if (userRepository.findByUsername(registrationDTO.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists.");
        }
        if (userRepository.findByEmail(registrationDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists.");
        }

        User user = new User();
        user.setUsername(registrationDTO.getUsername());
        user.setPassword(registrationDTO.getPassword()); // Remember to hash this in a real app!
        user.setEmail(registrationDTO.getEmail());

        return userRepository.save(user);
    }

    // You might add a method to find a user by ID or username for authentication/current user retrieval
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }
}
