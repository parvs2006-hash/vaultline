package com.edutrack.model;

/**
 * Subject Entity
 * Represents an academic subject created dynamically by teachers.
 * Holds subject code, full subject name, and maximum possible marks.
 * 
 * Demonstrates: Encapsulation
 */
public class Subject {

    // Demonstrates: Encapsulation
    private String id;
    private String code;
    private String name;
    private int maxMarks;

    /**
     * Default constructor.
     */
    public Subject() {
        this.maxMarks = 100;
    }

    /**
     * Parameterized constructor for Subject.
     * 
     * @param id Unique identifier.
     * @param code Short code (e.g. "JAVA", "DBMS").
     * @param name Full course title.
     * @param maxMarks Maximum marks (default 100).
     */
    public Subject(String id, String code, String name, int maxMarks) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.maxMarks = maxMarks > 0 ? maxMarks : 100;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code != null ? code.trim().toUpperCase() : "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name.trim() : "";
    }

    public int getMaxMarks() {
        return maxMarks;
    }

    public void setMaxMarks(int maxMarks) {
        this.maxMarks = maxMarks;
    }

    @Override
    public String toString() {
        return code + " - " + name + " (" + maxMarks + " marks)";
    }
}
