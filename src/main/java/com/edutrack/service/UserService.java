package com.edutrack.service;

import com.edutrack.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.Vector;

/**
 * UserService
 * Manages user accounts, BCrypt password hashing, role provisioning,
 * student auto-credentials generation, and security lockout policies.
 * 
 * Demonstrates: Vector
 * Demonstrates: Encapsulation
 * Demonstrates: Control structures (if-else and loops)
 */
@Service
public class UserService {

    private final DataStorageService dataStorageService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SecureRandom random = new SecureRandom();

    private static final String TEMP_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    @Autowired
    public UserService(DataStorageService dataStorageService) {
        this.dataStorageService = dataStorageService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public BCryptPasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    /**
     * Checks whether at least one teacher account exists.
     * Used by first-time setup redirects.
     * 
     * @return true if one or more teachers exist.
     */
    public boolean hasAnyTeacher() {
        for (User u : dataStorageService.getUsers()) {
            if ("TEACHER".equalsIgnoreCase(u.getRole())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds a user account by unique username (email for teachers, roll number for students).
     * 
     * @param username Login username.
     * @return User object or null if not found.
     */
    public User getUserByUsername(String username) {
        if (username == null) return null;
        String clean = username.trim();
        for (User u : dataStorageService.getUsers()) {
            if (u.getUsername().equalsIgnoreCase(clean)) {
                return u;
            }
        }
        return null;
    }

    /**
     * Registers a new teacher account.
     * 
     * @param name Teacher full name.
     * @param email Official email address (serves as username).
     * @param plainPassword Plain-text password to hash.
     * @return Created User.
     */
    public synchronized User registerTeacher(String name, String email, String plainPassword) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty.");
        }
        if (plainPassword == null || plainPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }
        if (getUserByUsername(email) != null) {
            throw new IllegalArgumentException("An account with email '" + email + "' already exists.");
        }

        String hashedPassword = passwordEncoder.encode(plainPassword);
        User teacher = new User(
                UUID.randomUUID().toString(),
                email.trim().toLowerCase(),
                hashedPassword,
                "TEACHER",
                name != null ? name.trim() : "Faculty Member",
                email.trim().toLowerCase(),
                false
        );

        dataStorageService.getUsers().add(teacher);
        dataStorageService.saveUsers();
        return teacher;
    }

    /**
     * Auto-creates a student user account upon enrollment.
     * Generates a temporary random password and flags account for mandatory password change.
     * 
     * @param rollNumber Student's roll number (serves as login username).
     * @param name Student's full name.
     * @param email Student's email.
     * @return The temporary plain-text password to display ONCE to the teacher.
     */
    public synchronized String registerStudentUser(String rollNumber, String name, String email) {
        String cleanRoll = rollNumber.trim();
        User existing = getUserByUsername(cleanRoll);
        if (existing != null) {
            throw new IllegalArgumentException("User account for roll number '" + cleanRoll + "' already exists.");
        }

        String temporaryPassword = generateTemporaryPassword();
        String hashedPassword = passwordEncoder.encode(temporaryPassword);

        User studentUser = new User(
                UUID.randomUUID().toString(),
                cleanRoll,
                hashedPassword,
                "STUDENT",
                name != null ? name.trim() : cleanRoll,
                email != null ? email.trim() : "",
                true // Must change password on first login
        );

        dataStorageService.getUsers().add(studentUser);
        dataStorageService.saveUsers();
        return temporaryPassword;
    }

    /**
     * Resets a student's password to a fresh temporary password.
     * 
     * @param rollNumber Student's roll number.
     * @return New temporary password.
     */
    public synchronized String resetStudentPassword(String rollNumber) {
        User u = getUserByUsername(rollNumber);
        if (u == null) {
            throw new IllegalArgumentException("Student account not found for roll number: " + rollNumber);
        }

        String tempPass = generateTemporaryPassword();
        u.setPassword(passwordEncoder.encode(tempPass));
        u.setMustChangePassword(true);
        u.setFailedAttempts(0);
        u.setLockoutUntil(0L);

        dataStorageService.saveUsers();
        return tempPass;
    }

    /**
     * Changes user password and marks first-time change requirement fulfilled.
     * 
     * @param username Login username.
     * @param newPlainPassword New password.
     */
    public synchronized void changePassword(String username, String newPlainPassword) {
        if (newPlainPassword == null || newPlainPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }
        User u = getUserByUsername(username);
        if (u == null) {
            throw new IllegalArgumentException("User not found.");
        }

        u.setPassword(passwordEncoder.encode(newPlainPassword));
        u.setMustChangePassword(false);
        dataStorageService.saveUsers();
    }

    /**
     * Tracks failed login attempts and triggers a 5-minute lockout after 5 consecutive failures.
     * 
     * @param username Attempted username.
     */
    public synchronized void recordLoginFailure(String username) {
        User u = getUserByUsername(username);
        if (u == null) return;

        int currentAttempts = u.getFailedAttempts() + 1;
        u.setFailedAttempts(currentAttempts);

        // 5-minute lockout after 5 consecutive failed attempts
        if (currentAttempts >= 5) {
            u.setLockoutUntil(System.currentTimeMillis() + (5 * 60 * 1000));
        }
        dataStorageService.saveUsers();
    }

    /**
     * Resets failed login counters upon successful authentication.
     * 
     * @param username Authenticated username.
     */
    public synchronized void recordLoginSuccess(String username) {
        User u = getUserByUsername(username);
        if (u == null) return;

        u.setFailedAttempts(0);
        u.setLockoutUntil(0L);
        dataStorageService.saveUsers();
    }

    /**
     * Retrieves all registered teachers for the Manage Teachers page.
     * 
     * @return Vector of Teacher users.
     */
    public Vector<User> getAllTeachers() {
        Vector<User> teachers = new Vector<>();
        for (User u : dataStorageService.getUsers()) {
            if ("TEACHER".equalsIgnoreCase(u.getRole())) {
                teachers.add(u);
            }
        }
        return teachers;
    }

    /**
     * Removes a teacher account, preventing deletion of the last remaining teacher.
     * 
     * @param teacherId Teacher user ID.
     * @return true if deleted.
     */
    public synchronized boolean deleteTeacher(String teacherId) {
        Vector<User> teachers = getAllTeachers();
        if (teachers.size() <= 1) {
            throw new IllegalStateException("Cannot delete the last remaining teacher account.");
        }

        boolean removed = dataStorageService.getUsers().removeIf(u -> u.getId().equals(teacherId));
        if (removed) {
            dataStorageService.saveUsers();
        }
        return removed;
    }

    /**
     * Helper to generate a friendly, secure temporary password (e.g. Edu#8429).
     */
    private String generateTemporaryPassword() {
        StringBuilder sb = new StringBuilder("Edu#");
        for (int i = 0; i < 4; i++) {
            sb.append(TEMP_CHARS.charAt(random.nextInt(TEMP_CHARS.length())));
        }
        return sb.toString();
    }
}
