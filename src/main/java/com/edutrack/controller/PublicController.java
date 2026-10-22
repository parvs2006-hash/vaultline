package com.edutrack.controller;

import com.edutrack.model.ClassSummary;
import com.edutrack.service.StudentService;
import com.edutrack.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * PublicController
 * Handles publicly accessible institutional information pages:
 * Landing redirects, About EduTrack, and the UN SDG 4 Quality Education showcase.
 */
@Controller
public class PublicController {

    private final UserService userService;
    private final StudentService studentService;

    @Autowired
    public PublicController(UserService userService, StudentService studentService) {
        this.userService = userService;
        this.studentService = studentService;
    }

    /**
     * Root URL routing.
     * Directs teachers to /teacher/dashboard, students to /student/dashboard,
     * new deployments to /setup, or unauthenticated guests to /login.
     */
    @GetMapping("/")
    public String index(Authentication authentication) {
        if (!userService.hasAnyTeacher()) {
            return "redirect:/setup";
        }
        if (authentication != null && authentication.isAuthenticated()) {
            boolean isTeacher = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));
            if (isTeacher) {
                return "redirect:/teacher/dashboard";
            }
            boolean isStudent = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));
            if (isStudent) {
                return "redirect:/student/dashboard";
            }
        }
        return "redirect:/login";
    }

    /**
     * Renders About Page detailing Java fundamentals concepts and project architecture.
     */
    @GetMapping("/about")
    public String about(Model model, Authentication authentication) {
        model.addAttribute("authenticated", authentication != null && authentication.isAuthenticated());
        model.addAttribute("isTeacher", authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER")));
        model.addAttribute("isStudent", authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT")));
        return "about";
    }

    /**
     * Renders UN SDG 4 (Quality Education) Page with live statistics from the running application.
     */
    @GetMapping("/sdg4")
    public String sdg4(Model model, Authentication authentication) {
        ClassSummary summary = studentService.generateClassSummary();
        model.addAttribute("summary", summary);
        model.addAttribute("authenticated", authentication != null && authentication.isAuthenticated());
        model.addAttribute("isTeacher", authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER")));
        model.addAttribute("isStudent", authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT")));
        return "sdg4";
    }
}
