package com.example.personalfinancetracker.controller;

import com.example.personalfinancetracker.dto.UserRegistrationDTO;
import com.example.personalfinancetracker.model.User;
import com.example.personalfinancetracker.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid; // NEW IMPORT
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult; // NEW IMPORT
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/") // Base path for authentication
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // Displays the registration form
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        // Ensure 'user' object is always added for the form, even on GET requests
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new UserRegistrationDTO());
        }
        return "register"; // Refers to src/main/resources/templates/register.html
    }

    // Handles user registration submission
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDTO registrationDTO, // ADD @Valid
                               BindingResult result, // ADD BindingResult
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            // If there are validation errors, return to the form with errors
            model.addAttribute("errorMessage", "Please correct the form errors.");
            return "register"; // Stay on registration page to show errors
        }

        try {
            User registeredUser = userService.registerUser(registrationDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please log in.");
            return "redirect:/login"; // Redirect to login page after successful registration
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "register"; // Stay on registration page to show error
        }
    }

    // Displays the login form
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("username", ""); // For pre-filling username if needed
        model.addAttribute("password", ""); // Not really used for mock login
        return "login"; // Refers to src/main/resources/templates/login.html
    }

    // Handles mock login submission
    // IMPORTANT: This is a simplified mock login. In a real app, use Spring Security.
    @PostMapping("/login")
    public String loginUser(@ModelAttribute("username") String username,
                            @ModelAttribute("password") String password, // Password not checked for mock login
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        Optional<User> userOptional = userService.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // In a real app: check passwordEncoder.matches(password, user.getPassword())
            // For mock: any existing username is 'logged in'
            session.setAttribute("loggedInUserId", user.getId());
            session.setAttribute("loggedInUsername", user.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "Welcome, " + user.getUsername() + "!");
            return "redirect:/dashboard"; // Redirect to dashboard on success
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid username or password.");
            return "redirect:/login"; // Redirect back to login with error
        }
    }

    // Handles user logout
    @GetMapping("/logout")
    public String logoutUser(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate(); // Invalidate the session
        redirectAttributes.addFlashAttribute("successMessage", "You have been logged out.");
        return "redirect:/login"; // Redirect to login page
    }
}
