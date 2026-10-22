package com.edutrack.console;

import com.edutrack.model.ClassSummary;
import com.edutrack.model.Student;
import com.edutrack.model.StudentAssessment;
import com.edutrack.model.Subject;
import com.edutrack.service.AttendanceService;
import com.edutrack.service.DataStorageService;
import com.edutrack.service.StudentService;
import com.edutrack.service.SubjectService;
import com.edutrack.util.DuplicateRollNumberException;
import com.edutrack.util.InvalidMarksException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

/**
 * ConsoleApp
 * Interactive console interface for EduTrack using standard console I/O and Scanner.
 * Shares the exact same service classes and data storage files as the web application.
 * 
 * Demonstrates: Scanner and text menu
 * Demonstrates: Control structures (switch and while loops)
 * Demonstrates: Custom exceptions
 */
public class ConsoleApp {

    // Demonstrates: Scanner and text menu
    private static final Scanner scanner = new Scanner(System.in);
    private static DataStorageService dataStorageService;
    private static AttendanceService attendanceService;
    private static SubjectService subjectService;
    private static StudentService studentService;

    public static void main(String[] args) {
        // Initialize services directly for standalone console usage
        dataStorageService = new DataStorageService();
        dataStorageService.init();

        attendanceService = new AttendanceService(dataStorageService);
        subjectService = new SubjectService(dataStorageService);
        studentService = new StudentService(dataStorageService, attendanceService, subjectService);

        System.out.println("====================================================");
        System.out.println("  EduTrack Console: Attendance & Performance Tracker");
        System.out.println("  Supporting UN SDG 4: Quality Education            ");
        System.out.println("====================================================");

        boolean running = true;

        // Demonstrates: Control structures (while loop)
        while (running) {
            printMenu();
            System.out.print("Enter your choice (1-7): ");
            String choice = scanner.nextLine().trim();

            // Demonstrates: Control structures (switch statement for text menus)
            switch (choice) {
                case "1":
                    handleAddStudent();
                    break;
                case "2":
                    handleEnterMarks();
                    break;
                case "3":
                    handleViewAllStudents();
                    break;
                case "4":
                    handleShowAtRisk();
                    break;
                case "5":
                    handleClassReport();
                    break;
                case "6":
                    handleMarkAttendance();
                    break;
                case "7":
                    running = false;
                    System.out.println("\nExiting EduTrack Console. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please choose between 1 and 7.");
            }
            System.out.println();
        }
    }

    private static void printMenu() {
        System.out.println("\n----------------- MAIN MENU -----------------");
        System.out.println("1. Add New Student");
        System.out.println("2. Enter / Update Student Marks");
        System.out.println("3. View All Students Roster");
        System.out.println("4. Show At-Risk & Needs Attention Students");
        System.out.println("5. Print Formal Class Summary Report");
        System.out.println("6. Mark Attendance for a Date");
        System.out.println("7. Exit Console");
        System.out.println("---------------------------------------------");
    }

    private static void handleAddStudent() {
        System.out.println("\n--- [1] Add New Student ---");
        System.out.print("Enter Roll Number : ");
        String roll = scanner.nextLine().trim();
        System.out.print("Enter Full Name   : ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter Class/Div   : ");
        String className = scanner.nextLine().trim();
        System.out.print("Enter Email       : ");
        String email = scanner.nextLine().trim();

        try {
            // Demonstrates: Custom exceptions handling
            Student s = studentService.addStudent(name, roll, className, email);
            System.out.println("Successfully added student: " + s.getName() + " (Roll: " + s.getRollNumber() + ")");
        } catch (DuplicateRollNumberException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Validation Error: " + e.getMessage());
        }
    }

    private static void handleEnterMarks() {
        System.out.println("\n--- [2] Enter Marks ---");
        System.out.print("Enter Student Roll Number: ");
        String roll = scanner.nextLine().trim();
        Student s = studentService.getStudentByRollNumber(roll);
        if (s == null) {
            System.out.println("Student with roll number '" + roll + "' was not found.");
            return;
        }

        Vector<Subject> subjects = subjectService.getAllSubjects();
        if (subjects.isEmpty()) {
            System.out.println("No subjects configured yet. Adding default subject 'JAVA'...");
            subjectService.addSubject("JAVA", "Java Fundamentals", 100);
            subjects = subjectService.getAllSubjects();
        }

        Map<String, Integer> marksMap = new HashMap<>(s.getSubjectMarks());
        System.out.println("Entering marks out of 100 for " + s.getName() + ":");

        // Demonstrates: Control structures (enhanced-for loop)
        for (Subject sub : subjects) {
            Integer existing = marksMap.get(sub.getCode());
            System.out.print("  " + sub.getCode() + " (" + sub.getName() + ") [" + (existing != null ? existing : "-") + "]: ");
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                try {
                    int mark = Integer.parseInt(input);
                    if (mark < 0 || mark > 100) {
                        System.out.println("  Invalid mark: must be between 0 and 100. Skipping.");
                    } else {
                        marksMap.put(sub.getCode(), mark);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("  Invalid number format. Skipping.");
                }
            }
        }

        try {
            // Demonstrates: Custom exceptions
            studentService.updateStudentMarks(s.getId(), marksMap);
            System.out.println("Marks updated successfully! Overall Average: " + 
                    String.format("%.2f%%", s.calculateAverage()) + ", Grade: " + Student.calculateGrade(s.calculateAverage()));
        } catch (InvalidMarksException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void handleViewAllStudents() {
        System.out.println("\n--- [3] All Students Roster ---");
        List<StudentAssessment> assessments = studentService.getAllStudentAssessments();
        if (assessments.isEmpty()) {
            System.out.println("No students enrolled yet. Use Option 1 to add your first student.");
            return;
        }

        System.out.printf("%-10s %-20s %-10s %-12s %-8s %-16s %-6s\n", 
                "Roll No", "Name", "Class", "Average", "Grade", "Status", "Att %");
        System.out.println("---------------------------------------------------------------------------------");
        for (StudentAssessment sa : assessments) {
            Student s = sa.getStudent();
            String avgStr = sa.getAverageMarks() >= 0 ? String.format("%.1f%%", sa.getAverageMarks()) : "N/A";
            String attStr = sa.getAttendancePercentage() >= 0 ? String.format("%.1f%%", sa.getAttendancePercentage()) : "N/A";
            System.out.printf("%-10s %-20s %-10s %-12s %-8s %-16s %-6s\n",
                    s.getRollNumber(),
                    s.getName().length() > 19 ? s.getName().substring(0, 19) : s.getName(),
                    s.getClassName(),
                    avgStr,
                    sa.getGrade(),
                    sa.getStatus(),
                    attStr);
        }
    }

    private static void handleShowAtRisk() {
        System.out.println("\n--- [4] At-Risk & Needs Attention Students (UN SDG 4 Early Warning) ---");
        List<StudentAssessment> list = studentService.getAllStudentAssessments();
        int count = 0;

        for (StudentAssessment sa : list) {
            if ("At Risk".equals(sa.getStatus()) || "Needs Attention".equals(sa.getStatus())) {
                count++;
                System.out.println("\nStudent: " + sa.getStudent().getName() + " (Roll: " + sa.getStudent().getRollNumber() + ")");
                System.out.println("  Status : [" + sa.getStatus() + "]");
                System.out.println("  Grade  : " + sa.getGrade() + " | Average: " + 
                        (sa.getAverageMarks() >= 0 ? String.format("%.1f%%", sa.getAverageMarks()) : "N/A"));
                System.out.println("  Attendance: " + 
                        (sa.getAttendancePercentage() >= 0 ? String.format("%.1f%%", sa.getAttendancePercentage()) : "N/A"));
                System.out.println("  Reasons for concern:");
                for (String reason : sa.getRiskReasons()) {
                    System.out.println("    * " + reason);
                }
            }
        }

        if (count == 0) {
            System.out.println("Great news! No students are currently flagged as At Risk or Needing Attention.");
        }
    }

    private static void handleClassReport() {
        System.out.println("\n--- [5] Formal Class Summary Report ---");
        ClassSummary summary = studentService.generateClassSummary();
        // Demonstrates: Interface Reportable method invocation
        System.out.println(summary.generateReport());
    }

    private static void handleMarkAttendance() {
        System.out.println("\n--- [6] Mark Attendance ---");
        Vector<Student> students = studentService.getAllStudents();
        if (students.isEmpty()) {
            System.out.println("No students enrolled yet.");
            return;
        }

        System.out.print("Enter Date (YYYY-MM-DD) [e.g. 2026-10-22]: ");
        String date = scanner.nextLine().trim();
        if (date.isEmpty()) {
            System.out.println("Date cannot be blank.");
            return;
        }

        System.out.println("Mark attendance for each student (P for Present, A for Absent):");
        for (Student s : students) {
            System.out.print("  " + s.getRollNumber() + " - " + s.getName() + " [P/A]: ");
            String status = scanner.nextLine().trim().toUpperCase();
            boolean present = !"A".equals(status);
            attendanceService.markAttendance(s.getId(), date, present);
        }
        System.out.println("Attendance recorded successfully for date: " + date);
    }
}
