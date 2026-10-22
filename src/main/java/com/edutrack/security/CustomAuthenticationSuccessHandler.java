package com.edutrack.security;

import com.edutrack.model.User;
import com.edutrack.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * CustomAuthenticationSuccessHandler
 * Manages post-authentication routing and reset of failed attempt counters.
 * Routes teachers to teacher dashboard and students to student dashboard or mandatory password change.
 * 
 * Demonstrates: Control structures (if-else and loops)
 */
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;

    @Autowired
    public CustomAuthenticationSuccessHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String username = authentication.getName();
        userService.recordLoginSuccess(username);

        User user = userService.getUserByUsername(username);

        for (GrantedAuthority auth : authentication.getAuthorities()) {
            if ("ROLE_TEACHER".equals(auth.getAuthority())) {
                response.sendRedirect(request.getContextPath() + "/teacher/dashboard");
                return;
            } else if ("ROLE_STUDENT".equals(auth.getAuthority())) {
                if (user != null && user.isMustChangePassword()) {
                    response.sendRedirect(request.getContextPath() + "/student/change-password?firstLogin=true");
                } else {
                    response.sendRedirect(request.getContextPath() + "/student/dashboard");
                }
                return;
            }
        }

        response.sendRedirect(request.getContextPath() + "/login");
    }
}
