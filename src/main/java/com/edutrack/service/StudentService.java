package com.edutrack.service;

import com.edutrack.model.ClassSummary;
import com.edutrack.model.Settings;
import com.edutrack.model.Student;
import com.edutrack.model.StudentAssessment;
import com.edutrack.model.Subject;
import com.edutrack.util.DuplicateRollNumberException;
import com.edutrack.util.InvalidMarksException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

/**
 * StudentService
 * Core business service for student management, marks tracking, early risk detection (SDG 4),
 * rank calculation, and academic report generation.
 * 
 * Demonstrates: Vector
 * Demonstrates: Strings and StringBuffer
 * Demonstrates: Control structures (if-else, loops)
 * Demonstrates: Custom exceptions
 */
@Service
public class StudentService {

    private final DataStorageService dataStorageService;
    private final AttendanceService attendanceService;
    private final SubjectService subjectService;

    @Autowired
    public StudentService(DataStorageService dataStorageService,
                          AttendanceService attendanceService,
                          SubjectService subjectService) {
        this.dataStorageService = dataStorageService;
        this.attendanceService = attendanceService;
        this.subjectService = subjectService;
    }

    /**
     * Retrieves all enrolled students.
     * 
     * Demonstrates: Vector
     * @return Vector containing all students.
     */
    public Vector<Student> getAllStudents() {
        return dataStorageService.getStudents();
    }

    /**
     * Finds a student by unique system ID.
     * 
     * @param id System UUID.
     * @return Student or null if not found.
     */
    public Student getStudentById(String id) {
        if (id == null) return null;
        for (Student s : dataStorageService.getStudents()) {
            if (s.getId().equals(id)) {
                return s;
            }
        }
        return null;
    }

    /**
     * Finds a student by roll number.
     * 
     * Demonstrates: Strings and StringBuffer (trim and equalsIgnoreCase)
     * @param rollNumber Roll number string.
     * @return Student or null if not found.
     */
    public Student getStudentByRollNumber(String rollNumber) {
        if (rollNumber == null) return null;
        String cleanRoll = rollNumber.trim();
        for (Student s : dataStorageService.getStudents()) {
            if (s.getRollNumber().equalsIgnoreCase(cleanRoll)) {
                return s;
            }
        }
        return null;
    }

    /**
     * Enrolls a new student. Enforces uniqueness of roll numbers.
     * 
     * Demonstrates: Custom exceptions (DuplicateRollNumberException)
     * Demonstrates: Vector
     * 
     * @param name Student's full name.
     * @param rollNumber Unique institutional roll number.
     * @param className Class / division name.
     * @param email Contact email address.
     * @return Newly enrolled Student.
     * @throws DuplicateRollNumberException if roll number is already in use.
     */
    public synchronized Student addStudent(String name, String rollNumber, String className, String email) 
            throws DuplicateRollNumberException {
        if (rollNumber == null || rollNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Roll number cannot be empty.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Student name cannot be empty.");
        }

        if (getStudentByRollNumber(rollNumber) != null) {
            // Demonstrates: Custom exceptions
            throw new DuplicateRollNumberException("Student with Roll Number '" + rollNumber.trim() + "' already exists.");
        }

        Student student = new Student(
                UUID.randomUUID().toString(),
                name.trim(),
                email != null ? email.trim() : "",
                rollNumber.trim(),
                className != null ? className.trim() : ""
        );

        dataStorageService.getStudents().add(student);
        dataStorageService.saveStudents();
        return student;
    }

    /**
     * Updates profile details of an enrolled student.
     * 
     * @param id Student ID.
     * @param name Updated full name.
     * @param rollNumber Updated roll number.
     * @param className Updated class / division.
     * @param email Updated email address.
     * @return Updated Student.
     * @throws DuplicateRollNumberException if new roll number belongs to another student.
     */
    public synchronized Student updateStudent(String id, String name, String rollNumber, String className, String email) 
            throws DuplicateRollNumberException {
        Student s = getStudentById(id);
        if (s == null) {
            throw new IllegalArgumentException("Student not found.");
        }

        Student existingWithRoll = getStudentByRollNumber(rollNumber);
        if (existingWithRoll != null && !existingWithRoll.getId().equals(id)) {
            // Demonstrates: Custom exceptions
            throw new DuplicateRollNumberException("Roll Number '" + rollNumber.trim() + "' is already assigned to another student.");
        }

        s.setName(name);
        s.setRollNumber(rollNumber);
        s.setClassName(className);
        s.setEmail(email);

        dataStorageService.saveStudents();
        return s;
    }

    /**
     * Deletes a student, purging their attendance history and credentials.
     * 
     * @param id Student ID.
     * @return true if deleted.
     */
    public synchronized boolean deleteStudent(String id) {
        Student s = getStudentById(id);
        if (s == null) return false;

        // Remove student
        dataStorageService.getStudents().remove(s);
        dataStorageService.saveStudents();

        // Remove attendance records
        dataStorageService.getAttendanceRecords().removeIf(rec -> rec.getStudentId().equals(id));
        dataStorageService.saveAttendance();

        // Remove user credentials
        dataStorageService.getUsers().removeIf(u -> u.getUsername().equalsIgnoreCase(s.getRollNumber()));
        dataStorageService.saveUsers();

        return true;
    }

    /**
     * Updates marks for a student across subjects with strict boundary validation (0-100).
     * 
     * Demonstrates: Custom exceptions (InvalidMarksException)
     * Demonstrates: Control structures (loops)
     * 
     * @param studentId Student ID.
     * @param subjectMarks Map of Subject Code -> Mark (0-100).
     * @throws InvalidMarksException if any mark is invalid.
     */
    public synchronized void updateStudentMarks(String studentId, Map<String, Integer> subjectMarks) 
            throws InvalidMarksException {
        Student s = getStudentById(studentId);
        if (s == null) {
            throw new IllegalArgumentException("Student not found.");
        }

        if (subjectMarks != null) {
            for (Map.Entry<String, Integer> entry : subjectMarks.entrySet()) {
                Integer mark = entry.getValue();
                if (mark != null && (mark < 0 || mark > 100)) {
                    // Demonstrates: Custom exceptions
                    throw new InvalidMarksException("Mark value " + mark + " for subject '" + entry.getKey() + "' must be between 0 and 100.");
                }
            }
            s.setSubjectMarks(subjectMarks);
            dataStorageService.saveStudents();
        }
    }

    /**
     * Performs comprehensive academic evaluation for a student.
     * Calculates derived values dynamically: Attendance %, Average Marks, Grade, Status, Risk Reasons, Suggestions.
     * 
     * Demonstrates: Control structures (if-else branching)
     * 
     * @param student The Student entity.
     * @return StudentAssessment containing computed metrics and guidance.
     */
    public StudentAssessment assessStudent(Student student) {
        if (student == null) return new StudentAssessment();

        StudentAssessment assessment = new StudentAssessment(student);
        Settings settings = dataStorageService.getSettings();

        // Calculate attendance metrics
        double attendancePct = attendanceService.calculateAttendancePercentage(student.getId());
        assessment.setAttendancePercentage(attendancePct);
        assessment.setTotalAttendanceDays(attendanceService.getTotalDaysForStudent(student.getId()));
        assessment.setPresentDays(attendanceService.getPresentDaysForStudent(student.getId()));

        // Calculate marks metrics
        double avgMarks = student.calculateAverage();
        assessment.setAverageMarks(avgMarks);
        assessment.setTotalMarks(student.calculateTotalMarks());

        // Grade calculation (via static switch method)
        String grade = Student.calculateGrade(avgMarks);
        assessment.setGrade(grade);

        List<String> riskReasons = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();

        // Demonstrates: Control structures (if-else chain)
        boolean hasAttendanceData = (attendancePct >= 0);
        boolean hasMarksData = (avgMarks >= 0);

        if (!hasAttendanceData && !hasMarksData) {
            assessment.setStatus("No data yet");
            suggestions.add("No attendance or marks have been entered yet. Welcome to the new semester!");
        } else {
            // Check for failing subjects
            boolean hasFailingSubject = false;
            for (Map.Entry<String, Integer> entry : student.getSubjectMarks().entrySet()) {
                if (entry.getValue() != null && entry.getValue() < 40) {
                    hasFailingSubject = true;
                    riskReasons.add("Failing in " + entry.getKey() + " (" + entry.getValue() + "/100)");
                }
            }

            // Check Attendance Risk
            if (hasAttendanceData && attendancePct < settings.getAttendanceRiskThreshold()) {
                riskReasons.add(String.format("Low attendance (%.1f%%, below institutional minimum of %.0f%%)", 
                        attendancePct, settings.getAttendanceRiskThreshold()));
            }

            // Check Average Marks Risk
            if (hasMarksData && avgMarks < settings.getMarksRiskThreshold()) {
                riskReasons.add(String.format("Critical marks average (%.1f%%, below pass threshold of %.0f%%)", 
                        avgMarks, settings.getMarksRiskThreshold()));
            }

            // Determine Overall Academic Status (SDG 4 Early Warning System)
            // Demonstrates: Control structures (if-else)
            if ((hasAttendanceData && attendancePct < settings.getAttendanceRiskThreshold()) ||
                (hasMarksData && avgMarks < settings.getMarksRiskThreshold()) ||
                hasFailingSubject) {
                assessment.setStatus("At Risk");
            } else if ((hasAttendanceData && attendancePct <= settings.getAttendanceWarningThreshold()) ||
                       (hasMarksData && avgMarks <= settings.getMarksWarningThreshold())) {
                assessment.setStatus("Needs Attention");
                if (hasAttendanceData && attendancePct <= settings.getAttendanceWarningThreshold()) {
                    riskReasons.add(String.format("Attendance needs improvement (%.1f%%)", attendancePct));
                }
                if (hasMarksData && avgMarks <= settings.getMarksWarningThreshold()) {
                    riskReasons.add(String.format("Marks average borderline (%.1f%%)", avgMarks));
                }
            } else {
                assessment.setStatus("Good");
            }

            // Generate actionable, rule-based suggestions (SDG 4 Quality Education Intervention)
            if (hasAttendanceData && attendancePct < 75.0) {
                suggestions.add("Your attendance is below 75%. Please speak to your teacher and attend remedial sessions.");
            } else if (hasAttendanceData && attendancePct >= 90.0) {
                suggestions.add("Outstanding attendance consistency! Regular classroom engagement correlates strongly with academic success.");
            }

            if (hasFailingSubject) {
                suggestions.add("Immediate focus required: Schedule extra help sessions for subjects where score is below 40%.");
            } else if (hasMarksData && avgMarks >= 80.0) {
                suggestions.add("Excellent academic performance! Keep up your diligent study routine and consider assisting peers.");
            } else if (hasMarksData && avgMarks >= 50.0) {
                suggestions.add("Steady progress! Targeted revision in weaker topics can help you achieve higher letter grades.");
            }
        }

        assessment.setRiskReasons(riskReasons);
        assessment.setSuggestions(suggestions);
        return assessment;
    }

    /**
     * Evaluates all students and calculates class-wide rankings.
     * 
     * @return List of assessed students with rankings populated.
     */
    public List<StudentAssessment> getAllStudentAssessments() {
        List<StudentAssessment> list = new ArrayList<>();
        for (Student s : dataStorageService.getStudents()) {
            list.add(assessStudent(s));
        }

        // Sort descending by average marks for rank calculation
        list.sort((a, b) -> Double.compare(b.getAverageMarks(), a.getAverageMarks()));
        int rank = 1;
        for (StudentAssessment sa : list) {
            if (sa.getAverageMarks() >= 0) {
                sa.setRank(rank++);
            } else {
                sa.setRank(0); // Unranked due to no marks data
            }
        }
        return list;
    }

    /**
     * Filters and searches students dynamically.
     * 
     * Demonstrates: Strings and StringBuffer (toLowerCase, contains, trim, split)
     * Demonstrates: Control structures (loops and conditionals)
     * 
     * @param query Search text (name, roll, email).
     * @param className Class/division filter (or "ALL").
     * @param status Status filter ("ALL", "Good", "Needs Attention", "At Risk", "No data yet").
     * @param grade Grade filter ("ALL", "A+", etc.).
     * @return Filtered list of assessments.
     */
    public List<StudentAssessment> searchAndFilterStudents(String query, String className, String status, String grade) {
        List<StudentAssessment> all = getAllStudentAssessments();
        List<StudentAssessment> results = new ArrayList<>();

        String cleanQuery = (query != null) ? query.trim().toLowerCase() : "";

        // Demonstrates: Control structures (enhanced-for loop)
        for (StudentAssessment sa : all) {
            Student s = sa.getStudent();

            // Filter by search query
            if (!cleanQuery.isEmpty()) {
                // Demonstrates: Strings and StringBuffer (toLowerCase, contains, split)
                String nameLower = s.getName().toLowerCase();
                String rollLower = s.getRollNumber().toLowerCase();
                String emailLower = s.getEmail().toLowerCase();
                String classLower = s.getClassName().toLowerCase();

                boolean match = nameLower.contains(cleanQuery) ||
                                rollLower.contains(cleanQuery) ||
                                emailLower.contains(cleanQuery) ||
                                classLower.contains(cleanQuery);
                if (!match) continue;
            }

            // Filter by Class
            if (className != null && !className.equalsIgnoreCase("ALL") && !className.isBlank()) {
                if (!s.getClassName().equalsIgnoreCase(className.trim())) {
                    continue;
                }
            }

            // Filter by Status
            if (status != null && !status.equalsIgnoreCase("ALL") && !status.isBlank()) {
                if (!sa.getStatus().equalsIgnoreCase(status.trim())) {
                    continue;
                }
            }

            // Filter by Grade
            if (grade != null && !grade.equalsIgnoreCase("ALL") && !grade.isBlank()) {
                if (!sa.getGrade().equalsIgnoreCase(grade.trim())) {
                    continue;
                }
            }

            results.add(sa);
        }

        return results;
    }

    /**
     * Retrieves all unique class / division names currently in the student roster.
     * 
     * @return Sorted list of distinct class names.
     */
    public List<String> getAllClassNames() {
        List<String> list = new ArrayList<>();
        for (Student s : dataStorageService.getStudents()) {
            if (s.getClassName() != null && !s.getClassName().isBlank()) {
                String c = s.getClassName().trim();
                if (!list.contains(c)) {
                    list.add(c);
                }
            }
        }
        Collections.sort(list);
        return list;
    }

    /**
     * Generates aggregated ClassSummary metrics implementing Reportable.
     * 
     * @return ClassSummary object.
     */
    public ClassSummary generateClassSummary() {
        List<StudentAssessment> assessments = getAllStudentAssessments();
        int total = assessments.size();
        if (total == 0) {
            return new ClassSummary(0, 0, 0, 0, 0, 0, 0, 0, 0);
        }

        double sumAvg = 0;
        double highest = -1;
        double lowest = 101;
        int evaluatedStudents = 0;
        int passedStudents = 0;
        int atRisk = 0;
        int needsAttn = 0;
        int good = 0;

        for (StudentAssessment sa : assessments) {
            if ("At Risk".equals(sa.getStatus())) atRisk++;
            else if ("Needs Attention".equals(sa.getStatus())) needsAttn++;
            else if ("Good".equals(sa.getStatus())) good++;

            if (sa.getAverageMarks() >= 0) {
                evaluatedStudents++;
                double avg = sa.getAverageMarks();
                sumAvg += avg;
                if (avg > highest) highest = avg;
                if (avg < lowest) lowest = avg;
                if (avg >= 40.0) passedStudents++;
            }
        }

        double classAvg = evaluatedStudents > 0 ? (sumAvg / evaluatedStudents) : 0.0;
        double passPct = evaluatedStudents > 0 ? (((double) passedStudents / evaluatedStudents) * 100.0) : 0.0;
        double avgAtt = attendanceService.calculateClassAverageAttendance();

        return new ClassSummary(
                total,
                classAvg,
                highest >= 0 ? highest : 0.0,
                lowest <= 100 ? lowest : 0.0,
                passPct,
                avgAtt,
                atRisk,
                needsAttn,
                good
        );
    }

    /**
     * Retrieves the top 10 ranked students for the institution leaderboard.
     * 
     * @return Top students list.
     */
    public List<StudentAssessment> getLeaderboard() {
        List<StudentAssessment> list = getAllStudentAssessments();
        List<StudentAssessment> ranked = new ArrayList<>();
        for (StudentAssessment sa : list) {
            if (sa.getAverageMarks() >= 0) {
                ranked.add(sa);
            }
        }
        ranked.sort((a, b) -> Double.compare(b.getAverageMarks(), a.getAverageMarks()));
        if (ranked.size() > 10) {
            return ranked.subList(0, 10);
        }
        return ranked;
    }

    /**
     * Identifies the topper (highest scoring student) for each subject.
     * 
     * @return Map of Subject to top StudentAssessment (or null if no scores).
     */
    public Map<Subject, StudentAssessment> getSubjectToppers() {
        Map<Subject, StudentAssessment> topperMap = new LinkedHashMap<>();
        Vector<Subject> subjects = subjectService.getAllSubjects();
        List<StudentAssessment> assessments = getAllStudentAssessments();

        for (Subject sub : subjects) {
            StudentAssessment topper = null;
            int maxScore = -1;

            for (StudentAssessment sa : assessments) {
                Integer mark = sa.getStudent().getSubjectMark(sub.getCode());
                if (mark != null && mark > maxScore) {
                    maxScore = mark;
                    topper = sa;
                }
            }
            if (topper != null && maxScore >= 0) {
                topperMap.put(sub, topper);
            }
        }
        return topperMap;
    }

    /**
     * Builds a CSV string export of all students and their subject scores.
     * 
     * Demonstrates: Strings and StringBuffer
     * @return CSV formatted text string.
     */
    public String exportStudentsCsv() {
        StringBuffer sb = new StringBuffer();
        Vector<Subject> subs = subjectService.getAllSubjects();

        // Header
        sb.append("Roll Number,Name,Class,Email");
        for (Subject s : subs) {
            sb.append(",").append(s.getCode());
        }
        sb.append(",Average,Grade,Status\n");

        // Rows
        List<StudentAssessment> list = getAllStudentAssessments();
        for (StudentAssessment sa : list) {
            Student s = sa.getStudent();
            sb.append(s.getRollNumber()).append(",")
              .append(s.getName()).append(",")
              .append(s.getClassName()).append(",")
              .append(s.getEmail());

            for (Subject sub : subs) {
                Integer score = s.getSubjectMark(sub.getCode());
                sb.append(",").append(score != null ? score : "");
            }

            sb.append(",").append(sa.getAverageMarks() >= 0 ? String.format("%.2f", sa.getAverageMarks()) : "N/A")
              .append(",").append(sa.getGrade())
              .append(",").append(sa.getStatus()).append("\n");
        }
        return sb.toString();
    }
}
