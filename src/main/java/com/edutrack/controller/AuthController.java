package com.edutrack.controller;

import com.edutrack.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * AuthController
 * Manages user authentication views, one-time administrator onboarding (setup),
 * and security denial error pages.
 * 
 * Demonstrates: Control structures (conditionals)
 */
@Controller
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Renders the one-time administrator setup wizard if no teachers exist.
     */
    @GetMapping("/setup")
    public String showSetupPage(Model model) {
        if (userService.hasAnyTeacher()) {
            return "redirect:/login";
        }
        return "setup";
    }

    /**
     * Processes initial teacher account creation.
     */
    @PostMapping("/setup")
    public String processSetup(@RequestParam("name") String name,
                               @RequestParam("email") String email,
                               @RequestParam("password") String password,
                               @RequestParam("confirmPassword") String confirmPassword,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (userService.hasAnyTeacher()) {
            return "redirect:/login";
        }

        if (name == null || name.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            model.addAttribute("error", "All fields are required.");
            return "setup";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            return "setup";
        }

        if (password.length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters long.");
            return "setup";
        }

        try {
            userService.registerTeacher(name, email, password);
            redirectAttributes.addFlashAttribute("success", "Teacher account successfully created! Please log in.");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "setup";
        }
    }

    /**
     * Renders the unified login page with role switcher and security alerts.
     */
    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "msg", required = false) String msg,
                                @RequestParam(value = "logout", required = false) String logout,
                                Model model) {
        if (!userService.hasAnyTeacher()) {
            return "redirect:/setup";
        }

        if (error != null) {
            model.addAttribute("errorMessage", (msg != null && !msg.isBlank()) ? msg : "Invalid credentials.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "You have been logged out securely.");
        }
        return "login";
    }

    /**
     * Renders 403 Access Denied view when student attempts to access unauthorized teacher routes.
     */
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "403";
    }
}
