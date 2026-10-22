package com.edutrack.controller;

import com.edutrack.model.AttendanceRecord;
import com.edutrack.model.Student;
import com.edutrack.model.StudentAssessment;
import com.edutrack.model.Subject;
import com.edutrack.model.User;
import com.edutrack.service.AttendanceService;
import com.edutrack.service.StudentService;
import com.edutrack.service.SubjectService;
import com.edutrack.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * StudentController
 * View-only portal exclusively for enrolled students.
 * Enforces strict data isolation: students can only access their own academic records
 * based on authenticated Principal identity (Roll Number).
 * 
 * Demonstrates: Encapsulation
 * Demonstrates: Control structures (conditionals)
 */
@Controller
@RequestMapping("/student")
public class StudentController {

    private final StudentService studentService;
    private final AttendanceService attendanceService;
    private final SubjectService subjectService;
    private final UserService userService;

    @Autowired
    public StudentController(StudentService studentService,
                             AttendanceService attendanceService,
                             SubjectService subjectService,
                             UserService userService) {
        this.studentService = studentService;
        this.attendanceService = attendanceService;
        this.subjectService = subjectService;
        this.userService = userService;
    }

    /**
     * Resolves the currently authenticated student entity.
     * Prevents URL ID manipulation by deriving identity exclusively from Authentication.
     */
    private Student getAuthenticatedStudent(Authentication auth) {
        if (auth == null) return null;
        String rollNumber = auth.getName();
        return studentService.getStudentByRollNumber(rollNumber);
    }

    // ==========================================
    // 1. MY DASHBOARD
    // ==========================================
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        Student student = getAuthenticatedStudent(auth);
        if (student == null) {
            model.addAttribute("hasData", false);
            return "student/dashboard";
        }

        StudentAssessment assessment = studentService.assessStudent(student);
        Vector<Subject> subjects = subjectService.getAllSubjects();

        List<String> subjectCodes = new ArrayList<>();
        List<Integer> subjectScores = new ArrayList<>();
        for (Subject sub : subjects) {
            Integer m = student.getSubjectMark(sub.getCode());
            if (m != null) {
                subjectCodes.add(sub.getCode());
                subjectScores.add(m);
            }
        }

        model.addAttribute("hasData", true);
        model.addAttribute("student", student);
        model.addAttribute("assessment", assessment);
        model.addAttribute("subjectCodes", subjectCodes);
        model.addAttribute("subjectScores", subjectScores);

        return "student/dashboard";
    }

    // ==========================================
    // 2. MY MARKS
    // ==========================================
    @GetMapping("/marks")
    public String marks(Model model, Authentication auth) {
        Student student = getAuthenticatedStudent(auth);
        if (student == null) {
            model.addAttribute("hasData", false);
            return "student/marks";
        }

        StudentAssessment assessment = studentService.assessStudent(student);
        Vector<Subject> subjects = subjectService.getAllSubjects();

        model.addAttribute("hasData", true);
        model.addAttribute("student", student);
        model.addAttribute("assessment", assessment);
        model.addAttribute("subjects", subjects);

        return "student/marks";
    }

    // ==========================================
    // 3. MY ATTENDANCE
    // ==========================================
    @GetMapping("/attendance")
    public String attendance(Model model, Authentication auth) {
        Student student = getAuthenticatedStudent(auth);
        if (student == null) {
            model.addAttribute("hasData", false);
            return "student/attendance";
        }

        StudentAssessment assessment = studentService.assessStudent(student);
        List<AttendanceRecord> history = attendanceService.getStudentAttendanceHistory(student.getId());

        model.addAttribute("hasData", true);
        model.addAttribute("student", student);
        model.addAttribute("assessment", assessment);
        model.addAttribute("history", history);

        return "student/attendance";
    }

    // ==========================================
    // 4. MY SUGGESTIONS
    // ==========================================
    @GetMapping("/suggestions")
    public String suggestions(Model model, Authentication auth) {
        Student student = getAuthenticatedStudent(auth);
        if (student == null) {
            model.addAttribute("hasData", false);
            return "student/suggestions";
        }

        StudentAssessment assessment = studentService.assessStudent(student);

        model.addAttribute("hasData", true);
        model.addAttribute("student", student);
        model.addAttribute("assessment", assessment);

        return "student/suggestions";
    }

    // ==========================================
    // 5. MY REPORT CARD (PRINTABLE)
    // ==========================================
    @GetMapping("/report-card")
    public String reportCard(Model model, Authentication auth) {
        Student student = getAuthenticatedStudent(auth);
        if (student == null) {
            model.addAttribute("hasData", false);
            return "student/report-card";
        }

        StudentAssessment assessment = studentService.assessStudent(student);
        Vector<Subject> subjects = subjectService.getAllSubjects();

        model.addAttribute("hasData", true);
        model.addAttribute("student", student);
        model.addAttribute("assessment", assessment);
        model.addAttribute("subjects", subjects);

        return "student/report-card";
    }

    // ==========================================
    // 6. CHANGE PASSWORD
    // ==========================================
    @GetMapping("/change-password")
    public String changePasswordPage(@RequestParam(value = "firstLogin", required = false) String firstLogin,
                                     Model model, Authentication auth) {
        User user = userService.getUserByUsername(auth.getName());
        model.addAttribute("isFirstLogin", "true".equals(firstLogin) || (user != null && user.isMustChangePassword()));
        return "student/change-password";
    }

    @PostMapping("/change-password")
    public String processChangePassword(@RequestParam("newPassword") String newPassword,
                                        @RequestParam("confirmPassword") String confirmPassword,
                                        Authentication auth,
                                        RedirectAttributes redirectAttributes) {
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "New passwords do not match.");
            return "redirect:/student/change-password";
        }

        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Password must be at least 6 characters long.");
            return "redirect:/student/change-password";
        }

        try {
            userService.changePassword(auth.getName(), newPassword);
            redirectAttributes.addFlashAttribute("success", "Password changed successfully!");
            return "redirect:/student/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/student/change-password";
        }
    }
}
