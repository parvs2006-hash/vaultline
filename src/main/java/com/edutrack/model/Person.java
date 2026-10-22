package com.edutrack.model;

/**
 * Person Abstract Base Class
 * Represents common attributes and behaviors of all human entities in the system.
 * Cannot be instantiated directly; must be extended by specialized subclasses.
 * 
 * Demonstrates: Abstract class Person
 */
public abstract class Person {

    // Demonstrates: Encapsulation
    private String id;
    private String name;
    private String email;

    /**
     * Default constructor for Person.
     */
    public Person() {
    }

    /**
     * Parameterized constructor to initialize common identity details.
     * 
     * @param id Unique identifier.
     * @param name Full name of the person.
     * @param email Contact email address.
     */
    public Person(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    /**
     * Abstract method enforcing subclasses to define their specific role.
     * 
     * Demonstrates: Polymorphism
     * @return The role name (e.g., "TEACHER" or "STUDENT").
     */
    public abstract String getRole();

    // Demonstrates: Encapsulation (Getters and Setters)

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Polymorphic representation of a Person.
     * 
     * Demonstrates: Polymorphism
     */
    @Override
    public String toString() {
        return "Person [id=" + id + ", name=" + name + ", email=" + email + ", role=" + getRole() + "]";
    }
}
