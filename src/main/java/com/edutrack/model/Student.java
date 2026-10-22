package com.edutrack.model;

import com.edutrack.util.InvalidMarksException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Student Entity
 * Represents an enrolled student in the institution.
 * Inherits identity details from Person and implements Reportable for formatting academic summaries.
 * 
 * Demonstrates: Inheritance
 * Demonstrates: Interface
 */
public class Student extends Person implements Reportable {

    // Demonstrates: Static members
    private static int studentCounter = 0;

    // Demonstrates: Encapsulation
    private String rollNumber;
    private String className;

    // Demonstrates: Arrays
    private int[] marks;

    // Map to preserve subject-to-mark associations (Subject ID -> Mark)
    private Map<String, Integer> subjectMarks = new LinkedHashMap<>();

    // Demonstrates: Constructor overloading (Constructor 1: Default)
    /**
     * Default constructor.
     * Increments the static student counter.
     */
    public Student() {
        super();
        this.marks = new int[0];
        studentCounter++;
    }

    // Demonstrates: Constructor overloading (Constructor 2: Basic info)
    /**
     * Parameterized constructor without initial ID (used when creating a new record).
     * 
     * @param name Full name.
     * @param rollNumber Roll number / Student ID.
     * @param className Division / class name.
     * @param email Email address.
     */
    public Student(String name, String rollNumber, String className, String email) {
        super(null, name, email);
        this.rollNumber = rollNumber;
        this.className = className;
        this.marks = new int[0];
        studentCounter++;
    }

    // Demonstrates: Constructor overloading (Constructor 3: Standard identity)
    /**
     * Parameterized constructor with unique ID and roll number.
     * 
     * @param id System unique identifier.
     * @param name Full name.
     * @param email Contact email.
     * @param rollNumber Unique roll number.
     * @param className Class / division.
     */
    public Student(String id, String name, String email, String rollNumber, String className) {
        super(id, name, email);
        this.rollNumber = rollNumber;
        this.className = className;
        this.marks = new int[0];
        studentCounter++;
    }

    // Demonstrates: Constructor overloading (Constructor 4: Full initialization including marks array)
    /**
     * Parameterized constructor initializing all fields including marks array.
     * 
     * @param id System unique identifier.
     * @param name Full name.
     * @param email Contact email.
     * @param rollNumber Unique roll number.
     * @param className Class / division.
     * @param marks Initial array of marks.
     * @throws InvalidMarksException if any mark is invalid (< 0 or > 100).
     */
    public Student(String id, String name, String email, String rollNumber, String className, int[] marks) throws InvalidMarksException {
        super(id, name, email);
        this.rollNumber = rollNumber;
        this.className = className;
        setMarks(marks);
        studentCounter++;
    }

    // Demonstrates: Static members
    public static int getStudentCounter() {
        return studentCounter;
    }

    public static void setStudentCounter(int counter) {
        studentCounter = counter;
    }

    /**
     * Determines the letter grade from a numerical average using a switch statement.
     * Grade scale: 90+ A+, 80-89 A, 70-79 B, 60-69 C, 40-59 D, below 40 F
     * 
     * Demonstrates: Static members
     * Demonstrates: Control structures (switch)
     * 
     * @param average Numerical marks average (0-100).
     * @return Letter grade as String.
     */
    public static String calculateGrade(double average) {
        if (average < 0) {
            return "N/A";
        }
        int bracket = (int) average / 10;
        switch (bracket) {
            case 10:
            case 9:
                return "A+";
            case 8:
                return "A";
            case 7:
                return "B";
            case 6:
                return "C";
            case 5:
            case 4:
                return "D";
            default:
                return "F";
        }
    }

    // Demonstrates: Encapsulation (Validated Getters and Setters)

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber != null ? rollNumber.trim() : "";
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className != null ? className.trim() : "";
    }

    // Demonstrates: Arrays
    public int[] getMarks() {
        return marks != null ? marks.clone() : new int[0];
    }

    /**
     * Sets the marks array with validation.
     * 
     * Demonstrates: Encapsulation
     * Demonstrates: Arrays
     * Demonstrates: Custom exceptions
     * 
     * @param marks Array of marks.
     * @throws InvalidMarksException if any mark is less than 0 or greater than 100.
     */
    public void setMarks(int[] marks) throws InvalidMarksException {
        if (marks != null) {
            // Demonstrates: Control structures (enhanced-for loop)
            for (int mark : marks) {
                if (mark < 0 || mark > 100) {
                    throw new InvalidMarksException("Mark value " + mark + " is invalid. Marks must be between 0 and 100.");
                }
            }
            this.marks = marks.clone();
        } else {
            this.marks = new int[0];
        }
    }

    public Map<String, Integer> getSubjectMarks() {
        return subjectMarks;
    }

    public void setSubjectMarks(Map<String, Integer> subjectMarks) throws InvalidMarksException {
        this.subjectMarks = subjectMarks != null ? new LinkedHashMap<>(subjectMarks) : new LinkedHashMap<>();
        // Synchronize with internal marks array
        int[] arr = new int[this.subjectMarks.size()];
        int idx = 0;
        for (Integer val : this.subjectMarks.values()) {
            int score = (val != null) ? val : 0;
            if (score < 0 || score > 100) {
                throw new InvalidMarksException("Mark value " + score + " is invalid. Marks must be between 0 and 100.");
            }
            arr[idx++] = score;
        }
        this.marks = arr;
    }

    /**
     * Sets or updates mark for a specific subject.
     * 
     * @param subjectId ID or Code of subject.
     * @param mark Numerical mark (0-100).
     * @throws InvalidMarksException if mark is outside 0-100.
     */
    public void setSubjectMark(String subjectId, int mark) throws InvalidMarksException {
        if (mark < 0 || mark > 100) {
            throw new InvalidMarksException("Mark value " + mark + " is invalid for subject " + subjectId + ". Marks must be between 0 and 100.");
        }
        this.subjectMarks.put(subjectId, mark);
        // Refresh array
        int[] arr = new int[this.subjectMarks.size()];
        int idx = 0;
        for (Integer val : this.subjectMarks.values()) {
            arr[idx++] = (val != null) ? val : 0;
        }
        this.marks = arr;
    }

    public Integer getSubjectMark(String subjectId) {
        return this.subjectMarks.get(subjectId);
    }

    // Demonstrates: Method overloading (Version 1: Overall Average calculation)
    /**
     * Computes the arithmetic average of all marks stored in the marks array.
     * 
     * Demonstrates: Method overloading
     * Demonstrates: Control structures (for loop)
     * 
     * @return Average score as double, or -1.0 if no marks have been recorded.
     */
    public double calculateAverage() {
        if (marks == null || marks.length == 0) {
            return -1.0; // Represents "No data"
        }
        int total = 0;
        // Demonstrates: Control structures (for loop)
        for (int i = 0; i < marks.length; i++) {
            total += marks[i];
        }
        return (double) total / marks.length;
    }

    // Demonstrates: Method overloading (Version 2: Single Subject Mark / Average)
    /**
     * Overloaded method returning the score for a specific subject as double.
     * 
     * Demonstrates: Method overloading
     * 
     * @param subjectKey Subject identifier or code.
     * @return The mark obtained in this subject, or -1.0 if not found.
     */
    public double calculateAverage(String subjectKey) {
        if (subjectKey == null || !subjectMarks.containsKey(subjectKey)) {
            return -1.0;
        }
        return subjectMarks.get(subjectKey);
    }

    /**
     * Calculates total marks accumulated across all subjects.
     * 
     * @return Total marks, or 0 if empty.
     */
    public int calculateTotalMarks() {
        if (marks == null || marks.length == 0) {
            return 0;
        }
        int total = 0;
        for (int m : marks) {
            total += m;
        }
        return total;
    }

    // Demonstrates: Polymorphism (overriding abstract method from Person)
    @Override
    public String getRole() {
        return "STUDENT";
    }

    // Demonstrates: Interface (implementation of Reportable)
    // Demonstrates: Strings and StringBuffer
    @Override
    public String generateReport() {
        StringBuffer sb = new StringBuffer();
        sb.append("==================================================\n");
        sb.append("         STUDENT ACADEMIC REPORT CARD             \n");
        sb.append("==================================================\n");
        sb.append("Roll Number : ").append(rollNumber).append("\n");
        sb.append("Name        : ").append(getName()).append("\n");
        sb.append("Class       : ").append(className).append("\n");
        sb.append("Email       : ").append(getEmail()).append("\n");
        sb.append("--------------------------------------------------\n");
        sb.append("SUBJECT SCORES:\n");
        if (subjectMarks.isEmpty()) {
            sb.append("  (No marks entered yet)\n");
        } else {
            for (Map.Entry<String, Integer> entry : subjectMarks.entrySet()) {
                sb.append("  - ").append(entry.getKey()).append(": ").append(entry.getValue()).append("/100\n");
            }
        }
        sb.append("--------------------------------------------------\n");
        double avg = calculateAverage();
        if (avg >= 0) {
            sb.append(String.format("Total Marks : %d\n", calculateTotalMarks()));
            sb.append(String.format("Average     : %.2f%%\n", avg));
            sb.append("Grade       : ").append(calculateGrade(avg)).append("\n");
        } else {
            sb.append("Average     : N/A\n");
            sb.append("Grade       : N/A\n");
        }
        sb.append("==================================================\n");
        return sb.toString();
    }

    // Demonstrates: Polymorphism (toString override)
    @Override
    public String toString() {
        return "Student [id=" + getId() + ", roll=" + rollNumber + ", name=" + getName() + 
               ", class=" + className + ", avg=" + String.format("%.1f", calculateAverage()) + "]";
    }
}
