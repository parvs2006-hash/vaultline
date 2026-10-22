package com.edutrack.security;

import com.edutrack.model.User;
import com.edutrack.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * CustomUserDetailsService
 * Bridges Spring Security authentication with EduTrack's in-memory Vector-backed user accounts.
 * Validates credentials and verifies account lockout expiration.
 * 
 * Demonstrates: Encapsulation
 * Demonstrates: Control structures (conditionals)
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Autowired
    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.trim().isEmpty()) {
            throw new UsernameNotFoundException("Username cannot be empty.");
        }

        User user = userService.getUserByUsername(username.trim());
        if (user == null) {
            throw new UsernameNotFoundException("No account found with username: " + username);
        }

        // Check 5-minute lockout policy
        if (user.isLocked()) {
            long remainingMillis = user.getLockoutUntil() - System.currentTimeMillis();
            long remainingMinutes = (remainingMillis / 60000) + 1;
            throw new LockedException("Account is temporarily locked due to 5 failed login attempts. Please try again in " + remainingMinutes + " minute(s).");
        }

        String roleName = "ROLE_" + user.getRole().toUpperCase();
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                true, // enabled
                true, // accountNonExpired
                true, // credentialsNonExpired
                !user.isLocked(), // accountNonLocked
                Collections.singletonList(new SimpleGrantedAuthority(roleName))
        );
    }
}
