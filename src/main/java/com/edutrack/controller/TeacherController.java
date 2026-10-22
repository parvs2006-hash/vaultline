package com.edutrack.controller;

import com.edutrack.model.AttendanceRecord;
import com.edutrack.model.ClassSummary;
import com.edutrack.model.Settings;
import com.edutrack.model.Student;
import com.edutrack.model.StudentAssessment;
import com.edutrack.model.Subject;
import com.edutrack.model.User;
import com.edutrack.service.AttendanceService;
import com.edutrack.service.DataStorageService;
import com.edutrack.service.StudentService;
import com.edutrack.service.SubjectService;
import com.edutrack.service.UserService;
import com.edutrack.util.DuplicateRollNumberException;
import com.edutrack.util.InvalidMarksException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * TeacherController
 * Complete management hub for faculty members.
 * Enforces server-side TEACHER authorization.
 * 
 * Demonstrates: Control structures (loops, switch, conditionals)
 * Demonstrates: Custom exceptions handling
 * Demonstrates: Strings and StringBuffer
 */
@Controller
@RequestMapping("/teacher")
public class TeacherController {

    private final StudentService studentService;
    private final AttendanceService attendanceService;
    private final SubjectService subjectService;
    private final UserService userService;
    private final DataStorageService dataStorageService;

    @Autowired
    public TeacherController(StudentService studentService,
                             AttendanceService attendanceService,
                             SubjectService subjectService,
                             UserService userService,
                             DataStorageService dataStorageService) {
        this.studentService = studentService;
        this.attendanceService = attendanceService;
        this.subjectService = subjectService;
        this.userService = userService;
        this.dataStorageService = dataStorageService;
    }

    // ==========================================
    // 1. DASHBOARD
    // ==========================================
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        ClassSummary summary = studentService.generateClassSummary();
        List<StudentAssessment> allAssessments = studentService.getAllStudentAssessments();

        // Needing help students (At Risk or Needs Attention)
        List<StudentAssessment> needingHelp = new ArrayList<>();
        for (StudentAssessment sa : allAssessments) {
            if ("At Risk".equals(sa.getStatus()) || "Needs Attention".equals(sa.getStatus())) {
                needingHelp.add(sa);
            }
        }

        // Prepare Chart Data
        Vector<Subject> subjects = subjectService.getAllSubjects();
        List<String> subjectLabels = new ArrayList<>();
        List<Double> subjectAverages = new ArrayList<>();

        for (Subject sub : subjects) {
            double sum = 0;
            int count = 0;
            for (StudentAssessment sa : allAssessments) {
                Integer m = sa.getStudent().getSubjectMark(sub.getCode());
                if (m != null) {
                    sum += m;
                    count++;
                }
            }
            if (count > 0) {
                subjectLabels.add(sub.getCode());
                subjectAverages.add(sum / count);
            }
        }

        // Attendance Trend Data (by Date)
        List<String> recordedDates = attendanceService.getAllRecordedDates();
        List<Double> attendanceTrend = new ArrayList<>();
        for (String date : recordedDates) {
            Map<String, Boolean> map = attendanceService.getAttendanceForDate(date);
            if (!map.isEmpty()) {
                int pres = 0;
                for (boolean p : map.values()) {
                    if (p) pres++;
                }
                attendanceTrend.add(((double) pres / map.size()) * 100.0);
            }
        }

        // Grade Distribution (A+, A, B, C, D, F)
        int countAPlus = 0, countA = 0, countB = 0, countC = 0, countD = 0, countF = 0;
        for (StudentAssessment sa : allAssessments) {
            if (sa.getAverageMarks() >= 0) {
                switch (sa.getGrade()) {
                    case "A+": countAPlus++; break;
                    case "A": countA++; break;
                    case "B": countB++; break;
                    case "C": countC++; break;
                    case "D": countD++; break;
                    case "F": countF++; break;
                }
            }
        }

        model.addAttribute("summary", summary);
        model.addAttribute("needingHelp", needingHelp);
        model.addAttribute("hasStudents", !allAssessments.isEmpty());

        // Chart data
        model.addAttribute("subjectLabels", subjectLabels);
        model.addAttribute("subjectAverages", subjectAverages);
        model.addAttribute("recordedDates", recordedDates);
        model.addAttribute("attendanceTrend", attendanceTrend);
        model.addAttribute("goodCount", summary.getGoodCount());
        model.addAttribute("warningCount", summary.getNeedsAttentionCount());
        model.addAttribute("riskCount", summary.getAtRiskCount());
        model.addAttribute("gradeCounts", List.of(countAPlus, countA, countB, countC, countD, countF));

        return "teacher/dashboard";
    }

    // ==========================================
    // 2. STUDENTS ROSTER & MANAGEMENT
    // ==========================================
    @GetMapping("/students")
    public String studentsList(@RequestParam(value = "query", required = false) String query,
                               @RequestParam(value = "className", required = false) String className,
                               @RequestParam(value = "status", required = false) String status,
                               @RequestParam(value = "grade", required = false) String grade,
                               Model model) {
        List<StudentAssessment> students = studentService.searchAndFilterStudents(query, className, status, grade);
        List<String> classes = studentService.getAllClassNames();

        model.addAttribute("students", students);
        model.addAttribute("classes", classes);
        model.addAttribute("query", query != null ? query : "");
        model.addAttribute("selectedClass", className != null ? className : "ALL");
        model.addAttribute("selectedStatus", status != null ? status : "ALL");
        model.addAttribute("selectedGrade", grade != null ? grade : "ALL");
        model.addAttribute("totalCount", students.size());

        return "teacher/students";
    }

    @PostMapping("/students/add")
    public String addStudent(@RequestParam("name") String name,
                             @RequestParam("rollNumber") String rollNumber,
                             @RequestParam("className") String className,
                             @RequestParam("email") String email,
                             RedirectAttributes redirectAttributes) {
        try {
            // Add student record (validates duplicate roll number)
            Student s = studentService.addStudent(name, rollNumber, className, email);

            // Auto-create student login with temporary password
            String tempPassword = userService.registerStudentUser(s.getRollNumber(), s.getName(), s.getEmail());

            redirectAttributes.addFlashAttribute("newStudentRoll", s.getRollNumber());
            redirectAttributes.addFlashAttribute("newStudentName", s.getName());
            redirectAttributes.addFlashAttribute("newStudentPassword", tempPassword);
            redirectAttributes.addFlashAttribute("success", "Student enrolled successfully! Please share the temporary password.");
        } catch (DuplicateRollNumberException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating student: " + e.getMessage());
        }
        return "redirect:/teacher/students";
    }

    @PostMapping("/students/edit")
    public String editStudent(@RequestParam("id") String id,
                              @RequestParam("name") String name,
                              @RequestParam("rollNumber") String rollNumber,
                              @RequestParam("className") String className,
                              @RequestParam("email") String email,
                              RedirectAttributes redirectAttributes) {
        try {
            studentService.updateStudent(id, name, rollNumber, className, email);
            redirectAttributes.addFlashAttribute("success", "Student details updated successfully.");
        } catch (DuplicateRollNumberException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating student: " + e.getMessage());
        }
        return "redirect:/teacher/students";
    }

    @PostMapping("/students/delete/{id}")
    public String deleteStudent(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        boolean deleted = studentService.deleteStudent(id);
        if (deleted) {
            redirectAttributes.addFlashAttribute("success", "Student and associated records deleted successfully.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Student could not be found.");
        }
        return "redirect:/teacher/students";
    }

    @PostMapping("/students/reset-password/{rollNumber}")
    public String resetStudentPassword(@PathVariable("rollNumber") String rollNumber,
                                       RedirectAttributes redirectAttributes) {
        try {
            String newTempPassword = userService.resetStudentPassword(rollNumber);
            redirectAttributes.addFlashAttribute("resetRoll", rollNumber);
            redirectAttributes.addFlashAttribute("resetPassword", newTempPassword);
            redirectAttributes.addFlashAttribute("success", "Password reset successfully! Temporary password generated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/teacher/students";
    }

    // ==========================================
    // 3. STUDENT PROFILE & INTERVENTIONS
    // ==========================================
    @GetMapping("/students/{id}")
    public String studentProfile(@PathVariable("id") String id, Model model) {
        Student student = studentService.getStudentById(id);
        if (student == null) {
            return "redirect:/teacher/students";
        }

        StudentAssessment assessment = studentService.assessStudent(student);
        List<AttendanceRecord> attendanceHistory = attendanceService.getStudentAttendanceHistory(id);
        Vector<Subject> subjects = subjectService.getAllSubjects();

        // Calculate class average per subject for comparison
        Map<String, Double> classSubjectAverages = new HashMap<>();
        List<StudentAssessment> all = studentService.getAllStudentAssessments();
        for (Subject sub : subjects) {
            double sum = 0;
            int count = 0;
            for (StudentAssessment sa : all) {
                Integer m = sa.getStudent().getSubjectMark(sub.getCode());
                if (m != null) {
                    sum += m;
                    count++;
                }
            }
            if (count > 0) {
                classSubjectAverages.put(sub.getCode(), sum / count);
            }
        }

        model.addAttribute("assessment", assessment);
        model.addAttribute("student", student);
        model.addAttribute("attendanceHistory", attendanceHistory);
        model.addAttribute("subjects", subjects);
        model.addAttribute("classSubjectAverages", classSubjectAverages);

        return "teacher/student-profile";
    }

    // ==========================================
    // 4. CSV IMPORT & EXPORT
    // ==========================================
    @GetMapping("/students/export")
    public void exportStudentsCsv(HttpServletResponse response) throws IOException {
        String csvData = studentService.exportStudentsCsv();
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"students_export.csv\"");
        response.getOutputStream().write(csvData.getBytes(StandardCharsets.UTF_8));
        response.getOutputStream().flush();
    }

    @PostMapping("/students/import")
    public String importStudentsCsv(@RequestParam("file") MultipartFile file,
                                    RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a non-empty CSV file to upload.");
            return "redirect:/teacher/students";
        }

        int importedCount = 0;
        int errorCount = 0;
        List<String> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String header = reader.readLine(); // Header row
            String line;
            int lineNum = 1;

            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(",", -1);
                if (parts.length < 2) {
                    errors.add("Line " + lineNum + ": Missing required columns (Roll Number, Name).");
                    errorCount++;
                    continue;
                }

                String roll = parts[0].trim();
                String name = parts[1].trim();
                String className = (parts.length > 2) ? parts[2].trim() : "Default Class";
                String email = (parts.length > 3) ? parts[3].trim() : "";

                try {
                    Student s = studentService.addStudent(name, roll, className, email);
                    userService.registerStudentUser(s.getRollNumber(), s.getName(), s.getEmail());
                    importedCount++;
                } catch (DuplicateRollNumberException e) {
                    errors.add("Line " + lineNum + ": Duplicate roll number '" + roll + "'.");
                    errorCount++;
                } catch (Exception e) {
                    errors.add("Line " + lineNum + ": " + e.getMessage());
                    errorCount++;
                }
            }

            if (importedCount > 0) {
                redirectAttributes.addFlashAttribute("success", "Successfully imported " + importedCount + " student(s).");
            }
            if (errorCount > 0) {
                redirectAttributes.addFlashAttribute("error", "Encountered " + errorCount + " issue(s) during import: " + String.join("; ", errors));
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to process CSV file: " + e.getMessage());
        }

        return "redirect:/teacher/students";
    }

    // ==========================================
    // 5. ATTENDANCE MANAGEMENT
    // ==========================================
    @GetMapping("/attendance")
    public String attendancePage(@RequestParam(value = "date", required = false) String date, Model model) {
        String selectedDate = (date != null && !date.isBlank()) ? date.trim() : LocalDate.now().toString();
        Vector<Student> students = studentService.getAllStudents();
        Map<String, Boolean> attendanceMap = attendanceService.getAttendanceForDate(selectedDate);

        // Precompute attendance summaries per student
        Map<String, Double> studentPercentages = new HashMap<>();
        Map<String, Integer> studentTotalDays = new HashMap<>();
        Map<String, Integer> studentPresentDays = new HashMap<>();

        for (Student s : students) {
            studentPercentages.put(s.getId(), attendanceService.calculateAttendancePercentage(s.getId()));
            studentTotalDays.put(s.getId(), attendanceService.getTotalDaysForStudent(s.getId()));
            studentPresentDays.put(s.getId(), attendanceService.getPresentDaysForStudent(s.getId()));
        }

        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("students", students);
        model.addAttribute("attendanceMap", attendanceMap);
        model.addAttribute("studentPercentages", studentPercentages);
        model.addAttribute("studentTotalDays", studentTotalDays);
        model.addAttribute("studentPresentDays", studentPresentDays);
        model.addAttribute("hasStudents", !students.isEmpty());

        return "teacher/attendance";
    }

    @PostMapping("/attendance/save")
    public String saveAttendance(@RequestParam("date") String date,
                                 @RequestParam Map<String, String> formParams,
                                 RedirectAttributes redirectAttributes) {
        try {
            Vector<Student> students = studentService.getAllStudents();
            Map<String, Boolean> batchMap = new HashMap<>();

            for (Student s : students) {
                String key = "status_" + s.getId();
                String val = formParams.get(key);
                boolean isPresent = "true".equalsIgnoreCase(val) || "present".equalsIgnoreCase(val);
                batchMap.put(s.getId(), isPresent);
            }

            attendanceService.recordBatchAttendance(date, batchMap);
            redirectAttributes.addFlashAttribute("success", "Attendance for " + date + " saved successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error saving attendance: " + e.getMessage());
        }
        return "redirect:/teacher/attendance?date=" + date;
    }

    // ==========================================
    // 6. MARKS SPREADSHEET ENTRY
    // ==========================================
    @GetMapping("/marks")
    public String marksSpreadsheet(Model model) {
        Vector<Student> students = studentService.getAllStudents();
        Vector<Subject> subjects = subjectService.getAllSubjects();

        List<StudentAssessment> assessments = studentService.getAllStudentAssessments();
        Map<String, StudentAssessment> assessmentMap = new HashMap<>();
        for (StudentAssessment sa : assessments) {
            assessmentMap.put(sa.getStudent().getId(), sa);
        }

        model.addAttribute("students", students);
        model.addAttribute("subjects", subjects);
        model.addAttribute("assessmentMap", assessmentMap);
        model.addAttribute("hasStudents", !students.isEmpty());
        model.addAttribute("hasSubjects", !subjects.isEmpty());

        return "teacher/marks";
    }

    @PostMapping("/marks/save")
    public String saveMarks(@RequestParam Map<String, String> formParams,
                            RedirectAttributes redirectAttributes) {
        Vector<Student> students = studentService.getAllStudents();
        Vector<Subject> subjects = subjectService.getAllSubjects();
        int updatedCount = 0;

        try {
            for (Student s : students) {
                Map<String, Integer> marksMap = new HashMap<>();
                for (Subject sub : subjects) {
                    String paramKey = "mark_" + s.getId() + "_" + sub.getCode();
                    String val = formParams.get(paramKey);
                    if (val != null && !val.trim().isEmpty()) {
                        try {
                            int score = Integer.parseInt(val.trim());
                            if (score < 0 || score > 100) {
                                throw new InvalidMarksException("Mark value " + score + " for student " + s.getName() + " in " + sub.getCode() + " is outside 0-100.");
                            }
                            marksMap.put(sub.getCode(), score);
                        } catch (NumberFormatException e) {
                            throw new InvalidMarksException("Invalid numerical mark entered for student " + s.getName() + ": " + val);
                        }
                    }
                }
                studentService.updateStudentMarks(s.getId(), marksMap);
                updatedCount++;
            }
            redirectAttributes.addFlashAttribute("success", "Marks updated successfully for " + updatedCount + " student(s).");
        } catch (InvalidMarksException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error saving marks: " + e.getMessage());
        }

        return "redirect:/teacher/marks";
    }

    // ==========================================
    // 7. AT-RISK & INTERVENTION
    // ==========================================
    @GetMapping("/at-risk")
    public String atRiskPage(Model model) {
        List<StudentAssessment> all = studentService.getAllStudentAssessments();
        List<StudentAssessment> atRiskList = new ArrayList<>();
        List<StudentAssessment> needsAttentionList = new ArrayList<>();

        for (StudentAssessment sa : all) {
            if ("At Risk".equals(sa.getStatus())) {
                atRiskList.add(sa);
            } else if ("Needs Attention".equals(sa.getStatus())) {
                needsAttentionList.add(sa);
            }
        }

        Settings settings = dataStorageService.getSettings();

        model.addAttribute("atRiskList", atRiskList);
        model.addAttribute("needsAttentionList", needsAttentionList);
        model.addAttribute("settings", settings);
        model.addAttribute("totalAlertCount", atRiskList.size() + needsAttentionList.size());

        return "teacher/at-risk";
    }

    // ==========================================
    // 8. LEADERBOARD
    // ==========================================
    @GetMapping("/leaderboard")
    public String leaderboard(Model model) {
        List<StudentAssessment> topStudents = studentService.getLeaderboard();
        Map<Subject, StudentAssessment> subjectToppers = studentService.getSubjectToppers();

        model.addAttribute("topStudents", topStudents);
        model.addAttribute("subjectToppers", subjectToppers);
        model.addAttribute("hasData", !topStudents.isEmpty());

        return "teacher/leaderboard";
    }

    // ==========================================
    // 9. REPORTS
    // ==========================================
    @GetMapping("/reports")
    public String reports(Model model) {
        ClassSummary summary = studentService.generateClassSummary();
        List<StudentAssessment> assessments = studentService.getAllStudentAssessments();
        Vector<Subject> subjects = subjectService.getAllSubjects();

        model.addAttribute("summary", summary);
        model.addAttribute("assessments", assessments);
        model.addAttribute("subjects", subjects);
        model.addAttribute("hasStudents", !assessments.isEmpty());

        return "teacher/reports";
    }

    // ==========================================
    // 10. SUBJECTS MANAGEMENT
    // ==========================================
    @GetMapping("/subjects")
    public String subjectsPage(Model model) {
        Vector<Subject> subjects = subjectService.getAllSubjects();
        model.addAttribute("subjects", subjects);
        return "teacher/subjects";
    }

    @PostMapping("/subjects/add")
    public String addSubject(@RequestParam("code") String code,
                             @RequestParam("name") String name,
                             @RequestParam(value = "maxMarks", defaultValue = "100") int maxMarks,
                             RedirectAttributes redirectAttributes) {
        try {
            subjectService.addSubject(code, name, maxMarks);
            redirectAttributes.addFlashAttribute("success", "Subject '" + code.toUpperCase() + "' created successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/teacher/subjects";
    }

    @PostMapping("/subjects/edit")
    public String editSubject(@RequestParam("id") String id,
                              @RequestParam("code") String code,
                              @RequestParam("name") String name,
                              @RequestParam(value = "maxMarks", defaultValue = "100") int maxMarks,
                              RedirectAttributes redirectAttributes) {
        try {
            subjectService.updateSubject(id, code, name, maxMarks);
            redirectAttributes.addFlashAttribute("success", "Subject updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/teacher/subjects";
    }

    @PostMapping("/subjects/delete/{id}")
    public String deleteSubject(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        try {
            subjectService.deleteSubject(id);
            redirectAttributes.addFlashAttribute("success", "Subject deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/teacher/subjects";
    }

    // ==========================================
    // 11. SETTINGS
    // ==========================================
    @GetMapping("/settings")
    public String settingsPage(Model model) {
        Settings settings = dataStorageService.getSettings();
        model.addAttribute("settings", settings);
        return "teacher/settings";
    }

    @PostMapping("/settings/save")
    public String saveSettings(@RequestParam("attendanceRiskThreshold") double attendanceRisk,
                               @RequestParam("attendanceWarningThreshold") double attendanceWarn,
                               @RequestParam("marksRiskThreshold") double marksRisk,
                               @RequestParam("marksWarningThreshold") double marksWarn,
                               RedirectAttributes redirectAttributes) {
        try {
            Settings s = new Settings(attendanceRisk, attendanceWarn, marksRisk, marksWarn);
            dataStorageService.setSettings(s);
            redirectAttributes.addFlashAttribute("success", "Academic intervention thresholds saved successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error saving settings: " + e.getMessage());
        }
        return "redirect:/teacher/settings";
    }

    // ==========================================
    // 12. MANAGE TEACHERS
    // ==========================================
    @GetMapping("/manage-teachers")
    public String manageTeachers(Model model) {
        Vector<User> teachers = userService.getAllTeachers();
        model.addAttribute("teachers", teachers);
        return "teacher/manage-teachers";
    }

    @PostMapping("/manage-teachers/add")
    public String addTeacher(@RequestParam("name") String name,
                             @RequestParam("email") String email,
                             @RequestParam("password") String password,
                             @RequestParam("confirmPassword") String confirmPassword,
                             RedirectAttributes redirectAttributes) {
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match.");
            return "redirect:/teacher/manage-teachers";
        }
        try {
            userService.registerTeacher(name, email, password);
            redirectAttributes.addFlashAttribute("success", "Teacher account for '" + email + "' registered successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/teacher/manage-teachers";
    }

    @PostMapping("/manage-teachers/delete/{id}")
    public String deleteTeacher(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteTeacher(id);
            redirectAttributes.addFlashAttribute("success", "Teacher account removed.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/teacher/manage-teachers";
    }
}
