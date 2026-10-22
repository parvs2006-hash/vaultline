package com.edutrack.model;

/**
 * Teacher Entity
 * Represents an instructor / faculty member in the system.
 * Inherits common person details and has administrative authority over academic records.
 * 
 * Demonstrates: Inheritance
 */
public class Teacher extends Person {

    // Demonstrates: Encapsulation
    private String department;

    /**
     * Default constructor for Teacher.
     */
    public Teacher() {
        super();
        this.department = "Computer Science & Engineering";
    }

    /**
     * Parameterized constructor for Teacher.
     * 
     * @param id System unique identifier.
     * @param name Full name of teacher.
     * @param email Official email address.
     * @param department Academic department.
     */
    public Teacher(String id, String name, String email, String department) {
        super(id, name, email);
        this.department = (department != null && !department.isBlank()) ? department.trim() : "Computer Science & Engineering";
    }

    // Demonstrates: Polymorphism (overriding abstract getRole from Person)
    @Override
    public String getRole() {
        return "TEACHER";
    }

    // Demonstrates: Encapsulation
    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    // Demonstrates: Polymorphism (toString override)
    @Override
    public String toString() {
        return "Teacher [id=" + getId() + ", name=" + getName() + ", email=" + getEmail() + ", dept=" + department + "]";
    }
}
