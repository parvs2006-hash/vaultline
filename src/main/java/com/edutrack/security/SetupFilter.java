package com.edutrack.security;

import com.edutrack.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * SetupFilter
 * Intercepts requests to enforce the First-Time Setup workflow.
 * If zero teacher accounts exist in the system, all users are redirected to the setup wizard.
 * Once a teacher exists, access to the setup endpoint is permanently disabled and redirected to login.
 * 
 * Demonstrates: Control structures (conditionals)
 */
@Component
public class SetupFilter extends OncePerRequestFilter {

    private final UserService userService;

    @Autowired
    public SetupFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        // Allow static resources to pass through unhindered
        if (path.startsWith("/css/") || path.startsWith("/js/") || 
            path.startsWith("/images/") || path.equals("/favicon.ico")) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean hasTeacher = userService.hasAnyTeacher();

        if (!hasTeacher) {
            // No teacher exists yet: allow only setup endpoints
            if (!path.startsWith("/setup")) {
                response.sendRedirect(request.getContextPath() + "/setup");
                return;
            }
        } else {
            // Teacher exists: disable setup page completely
            if (path.startsWith("/setup")) {
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
