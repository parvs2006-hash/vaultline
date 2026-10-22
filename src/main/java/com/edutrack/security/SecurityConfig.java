package com.edutrack.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig
 * Central Spring Security 6 configuration class.
 * Enforces server-side role-based authorization, form login routing,
 * account lockout handling, and setup filter orchestration.
 * 
 * Demonstrates: Encapsulation
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final SetupFilter setupFilter;
    private final CustomAuthenticationSuccessHandler successHandler;
    private final CustomAuthenticationFailureHandler failureHandler;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SecurityConfig(SetupFilter setupFilter,
                          CustomAuthenticationSuccessHandler successHandler,
                          CustomAuthenticationFailureHandler failureHandler,
                          CustomAccessDeniedHandler accessDeniedHandler,
                          com.edutrack.service.UserService userService) {
        this.setupFilter = setupFilter;
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;
        this.accessDeniedHandler = accessDeniedHandler;
        this.passwordEncoder = userService.getPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disabled for simplicity in microproject; standard form post
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/setup", "/setup/**").permitAll()
                .requestMatchers("/login", "/login/**", "/access-denied").permitAll()
                .requestMatchers("/about", "/sdg4").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                .requestMatchers("/teacher/**").hasRole("TEACHER")
                .requestMatchers("/student/**").hasRole("STUDENT")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(successHandler)
                .failureHandler(failureHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedHandler(accessDeniedHandler)
            )
            .addFilterBefore(setupFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
