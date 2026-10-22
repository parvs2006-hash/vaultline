package com.edutrack.model;

import java.util.ArrayList;
import java.util.List;

/**
 * StudentAssessment DTO (Data Transfer Object)
 * Encapsulates evaluated academic standing, derived indicators, risk reasons,
 * and rule-based guidance for a student.
 * 
 * Demonstrates: Encapsulation
 */
public class StudentAssessment {

    // Demonstrates: Encapsulation
    private Student student;
    private double attendancePercentage; // -1 if no attendance marked
    private int totalAttendanceDays;
    private int presentDays;
    private double averageMarks;         // -1 if no marks entered
    private int totalMarks;
    private String grade;
    private String status;               // "Good", "Needs Attention", "At Risk", "No data yet"
    private List<String> riskReasons = new ArrayList<>();
    private List<String> suggestions = new ArrayList<>();
    private int rank;

    /**
     * Default constructor.
     */
    public StudentAssessment() {
        this.attendancePercentage = -1.0;
        this.averageMarks = -1.0;
        this.grade = "N/A";
        this.status = "No data yet";
    }

    /**
     * Parameterized constructor.
     * 
     * @param student The underlying Student entity.
     */
    public StudentAssessment(Student student) {
        this.student = student;
        this.attendancePercentage = -1.0;
        this.averageMarks = -1.0;
        this.grade = "N/A";
        this.status = "No data yet";
    }

    // Getters and Setters

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public double getAttendancePercentage() {
        return attendancePercentage;
    }

    public void setAttendancePercentage(double attendancePercentage) {
        this.attendancePercentage = attendancePercentage;
    }

    public int getTotalAttendanceDays() {
        return totalAttendanceDays;
    }

    public void setTotalAttendanceDays(int totalAttendanceDays) {
        this.totalAttendanceDays = totalAttendanceDays;
    }

    public int getPresentDays() {
        return presentDays;
    }

    public void setPresentDays(int presentDays) {
        this.presentDays = presentDays;
    }

    public double getAverageMarks() {
        return averageMarks;
    }

    public void setAverageMarks(double averageMarks) {
        this.averageMarks = averageMarks;
    }

    public int getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(int totalMarks) {
        this.totalMarks = totalMarks;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getRiskReasons() {
        return riskReasons;
    }

    public void setRiskReasons(List<String> riskReasons) {
        this.riskReasons = riskReasons != null ? riskReasons : new ArrayList<>();
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions != null ? suggestions : new ArrayList<>();
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    /**
     * Helper to retrieve status CSS badge class name.
     * 
     * @return CSS badge class suffix.
     */
    public String getBadgeClass() {
        if ("Good".equalsIgnoreCase(status)) {
            return "badge-good";
        } else if ("Needs Attention".equalsIgnoreCase(status)) {
            return "badge-warning";
        } else if ("At Risk".equalsIgnoreCase(status)) {
            return "badge-risk";
        } else {
            return "badge-nodata";
        }
    }
}
