package com.example.personalfinancetracker; // Ensure this matches your root package

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // Marks this class as a source of bean definitions and configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configures view controllers to provide direct mapping between URLs and view names.
     * This is useful for simple, static views that don't require specific controller logic.
     * It also sets a default welcome page redirect.
     *
     * @param registry The ViewControllerRegistry to add view controllers to.
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Redirect the root URL "/" to the "/register" endpoint.
        // When a user accesses http://localhost:8080/, they will be redirected to /register.
        registry.addRedirectViewController("/", "/register");

        // Directly map "/register" URL to the "register" Thymeleaf template.
        // This explicitly ensures the register page is found.
        // Although AuthController handles it, this provides a fallback and direct mapping.
        registry.addViewController("/register").setViewName("register");

        // Directly map "/login" URL to the "login" Thymeleaf template.
        // This explicitly ensures the login page is found.
        registry.addViewController("/login").setViewName("login");

        // Note: For other dynamic paths handled by controllers (like /accounts, /transactions),
        // the controllers will still handle the view resolution.
    }
}
