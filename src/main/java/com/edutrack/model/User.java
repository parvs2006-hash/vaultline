package com.edutrack.model;

/**
 * User Entity
 * Represents authentication credentials and security state for teachers and students.
 * Enforces role-based permissions, password hashing, and account lockout tracking.
 * 
 * Demonstrates: Encapsulation
 */
public class User {

    // Demonstrates: Encapsulation
    private String id;
    private String username; // Email for teachers, Roll Number for students
    private String password; // BCrypt hashed
    private String role;     // "TEACHER" or "STUDENT"
    private String fullName;
    private String email;
    private boolean mustChangePassword; // Forced on student first login
    private int failedAttempts;
    private long lockoutUntil; // Epoch millisecond timestamp

    /**
     * Default constructor.
     */
    public User() {
        this.failedAttempts = 0;
        this.lockoutUntil = 0L;
    }

    /**
     * Parameterized constructor.
     * 
     * @param id System unique identifier.
     * @param username Login identifier.
     * @param password BCrypt encoded password.
     * @param role User role ("TEACHER" or "STUDENT").
     * @param fullName Person's full name.
     * @param email Contact email address.
     * @param mustChangePassword Flag indicating whether password change is mandatory.
     */
    public User(String id, String username, String password, String role, 
                String fullName, String email, boolean mustChangePassword) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.email = email;
        this.mustChangePassword = mustChangePassword;
        this.failedAttempts = 0;
        this.lockoutUntil = 0L;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username != null ? username.trim() : "";
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public long getLockoutUntil() {
        return lockoutUntil;
    }

    public void setLockoutUntil(long lockoutUntil) {
        this.lockoutUntil = lockoutUntil;
    }

    /**
     * Checks if the user account is currently locked out.
     * 
     * @return true if currently within lockout period.
     */
    public boolean isLocked() {
        return System.currentTimeMillis() < this.lockoutUntil;
    }

    @Override
    public String toString() {
        return "User [username=" + username + ", role=" + role + ", fullName=" + fullName + "]";
    }
}
