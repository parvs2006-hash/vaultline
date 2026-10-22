package com.edutrack.model;

/**
 * AttendanceRecord Entity
 * Represents an attendance event for a student on a specific calendar date.
 * 
 * Demonstrates: Encapsulation
 */
public class AttendanceRecord {

    // Demonstrates: Encapsulation
    private String id;
    private String studentId;
    private String date; // Format: YYYY-MM-DD
    private boolean present;

    /**
     * Default constructor.
     */
    public AttendanceRecord() {
    }

    /**
     * Parameterized constructor.
     * 
     * @param id Unique record identifier.
     * @param studentId ID of the student.
     * @param date Date formatted as YYYY-MM-DD.
     * @param present True if present, false if absent.
     */
    public AttendanceRecord(String id, String studentId, String date, boolean present) {
        this.id = id;
        this.studentId = studentId;
        this.date = date;
        this.present = present;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }

    @Override
    public String toString() {
        return "AttendanceRecord [id=" + id + ", studentId=" + studentId + ", date=" + date + ", present=" + present + "]";
    }
}
