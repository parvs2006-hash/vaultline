package com.edutrack.security;

import com.edutrack.model.User;
import com.edutrack.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * CustomAuthenticationFailureHandler
 * Handles failed login events, tracks consecutive bad attempts,
 * and displays friendly error messages or account lockout alerts.
 * 
 * Demonstrates: Control structures (conditionals)
 */
@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final UserService userService;

    @Autowired
    public CustomAuthenticationFailureHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String username = request.getParameter("username");
        String errorMessage = "Invalid username or password.";

        if (username != null && !username.trim().isEmpty()) {
            userService.recordLoginFailure(username.trim());
            User user = userService.getUserByUsername(username.trim());

            if (user != null && user.isLocked()) {
                long remainingMillis = user.getLockoutUntil() - System.currentTimeMillis();
                long remainingMinutes = Math.max(1, (remainingMillis / 60000) + 1);
                errorMessage = "Account locked due to 5 consecutive failed login attempts. Please wait " + remainingMinutes + " minute(s) before trying again.";
            } else if (user != null && user.getFailedAttempts() > 0) {
                int attemptsLeft = 5 - user.getFailedAttempts();
                if (attemptsLeft > 0 && attemptsLeft < 5) {
                    errorMessage = "Invalid credentials. " + attemptsLeft + " attempt(s) remaining before a 5-minute security lockout.";
                }
            }
        }

        if (exception instanceof LockedException) {
            errorMessage = exception.getMessage();
        }

        String encodedMsg = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + "/login?error=true&msg=" + encodedMsg);
    }
}
