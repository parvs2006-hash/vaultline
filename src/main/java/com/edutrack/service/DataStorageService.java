package com.edutrack.service;

import com.edutrack.model.AttendanceRecord;
import com.edutrack.model.Settings;
import com.edutrack.model.Student;
import com.edutrack.model.Subject;
import com.edutrack.model.User;
import com.edutrack.util.InvalidMarksException;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

/**
 * DataStorageService
 * Coordinates in-memory storage using java.util.Vector collections and handles
 * persistent file I/O to CSV files in the data/ folder.
 * Ensures the system starts completely empty if no user-generated files exist.
 * 
 * Demonstrates: Vector
 * Demonstrates: Strings and StringBuffer
 * Demonstrates: Control structures (loops and conditionals)
 */
@Service
public class DataStorageService {

    private static final String DATA_DIR = "data";
    private static final String USERS_FILE = DATA_DIR + File.separator + "users.csv";
    private static final String STUDENTS_FILE = DATA_DIR + File.separator + "students.csv";
    private static final String ATTENDANCE_FILE = DATA_DIR + File.separator + "attendance.csv";
    private static final String SUBJECTS_FILE = DATA_DIR + File.separator + "subjects.csv";
    private static final String SETTINGS_FILE = DATA_DIR + File.separator + "settings.csv";

    // Demonstrates: Vector (thread-safe, index-based dynamic array collections)
    private final Vector<User> users = new Vector<>();
    private final Vector<Student> students = new Vector<>();
    private final Vector<AttendanceRecord> attendanceRecords = new Vector<>();
    private final Vector<Subject> subjects = new Vector<>();
    private Settings settings = new Settings();

    /**
     * Initializes storage upon application startup.
     * Ensures directory and empty CSV files exist, then loads persisted records.
     */
    @PostConstruct
    public synchronized void init() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Initialize CSV files if missing (with header row only, no dummy data)
        ensureCsvFile(USERS_FILE, "id,username,password,role,fullName,email,mustChangePassword,failedAttempts,lockoutUntil");
        ensureCsvFile(STUDENTS_FILE, "id,rollNumber,name,className,email,subjectMarks");
        ensureCsvFile(ATTENDANCE_FILE, "id,studentId,date,present");
        ensureCsvFile(SUBJECTS_FILE, "id,code,name,maxMarks");
        ensureCsvFile(SETTINGS_FILE, "attendanceRiskThreshold,attendanceWarningThreshold,marksRiskThreshold,marksWarningThreshold");

        // Load data from files into Vector collections
        loadSettings();
        loadSubjects();
        loadStudents();
        loadAttendance();
        loadUsers();
    }

    /**
     * Creates an empty CSV file with only the header row if it does not exist.
     * 
     * Demonstrates: Strings and StringBuffer
     * 
     * @param filePath Path of file.
     * @param header CSV header string.
     */
    private void ensureCsvFile(String filePath, String header) {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                // Demonstrates: Strings and StringBuffer
                StringBuffer sb = new StringBuffer();
                sb.append(header).append("\n");
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    writer.write(sb.toString());
                }
            } catch (IOException e) {
                System.err.println("Could not initialize file " + filePath + ": " + e.getMessage());
            }
        }
    }

    // ==========================================
    // USERS PERSISTENCE
    // ==========================================

    private void loadUsers() {
        users.clear();
        File file = new File(USERS_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // Skip header
            // Demonstrates: Control structures (while loop)
            while ((line = reader.readLine()) != null) {
                // Demonstrates: Strings and StringBuffer (trim and split)
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 7) {
                    User u = new User();
                    u.setId(parts[0].trim());
                    u.setUsername(parts[1].trim());
                    u.setPassword(parts[2].trim());
                    u.setRole(parts[3].trim());
                    u.setFullName(parts[4].trim());
                    u.setEmail(parts[5].trim());
                    u.setMustChangePassword(Boolean.parseBoolean(parts[6].trim()));
                    if (parts.length >= 9) {
                        u.setFailedAttempts(Integer.parseInt(parts[7].trim()));
                        u.setLockoutUntil(Long.parseLong(parts[8].trim()));
                    }
                    users.add(u);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading users: " + e.getMessage());
        }
    }

    public synchronized void saveUsers() {
        // Demonstrates: Strings and StringBuffer
        StringBuffer sb = new StringBuffer();
        sb.append("id,username,password,role,fullName,email,mustChangePassword,failedAttempts,lockoutUntil\n");
        // Demonstrates: Control structures (enhanced-for loop)
        for (User u : users) {
            sb.append(escapeCsv(u.getId())).append(",")
              .append(escapeCsv(u.getUsername())).append(",")
              .append(escapeCsv(u.getPassword())).append(",")
              .append(escapeCsv(u.getRole())).append(",")
              .append(escapeCsv(u.getFullName())).append(",")
              .append(escapeCsv(u.getEmail())).append(",")
              .append(u.isMustChangePassword()).append(",")
              .append(u.getFailedAttempts()).append(",")
              .append(u.getLockoutUntil()).append("\n");
        }
        writeFile(USERS_FILE, sb.toString());
    }

    // ==========================================
    // STUDENTS PERSISTENCE
    // ==========================================

    private void loadStudents() {
        students.clear();
        File file = new File(STUDENTS_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 5) {
                    Student s = new Student();
                    s.setId(parts[0].trim());
                    s.setRollNumber(parts[1].trim());
                    s.setName(parts[2].trim());
                    s.setClassName(parts[3].trim());
                    s.setEmail(parts[4].trim());

                    if (parts.length >= 6 && !parts[5].trim().isEmpty()) {
                        String marksStr = parts[5].trim();
                        Map<String, Integer> marksMap = new LinkedHashMap<>();
                        String[] pairs = marksStr.split(";");
                        for (String pair : pairs) {
                            String[] kv = pair.split(":");
                            if (kv.length == 2) {
                                try {
                                    marksMap.put(kv[0].trim(), Integer.parseInt(kv[1].trim()));
                                } catch (NumberFormatException ignored) {}
                            }
                        }
                        try {
                            s.setSubjectMarks(marksMap);
                        } catch (InvalidMarksException ignored) {}
                    }
                    students.add(s);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading students: " + e.getMessage());
        }
    }

    public synchronized void saveStudents() {
        // Demonstrates: Strings and StringBuffer
        StringBuffer sb = new StringBuffer();
        sb.append("id,rollNumber,name,className,email,subjectMarks\n");
        for (Student s : students) {
            StringBuffer marksBuffer = new StringBuffer();
            Map<String, Integer> map = s.getSubjectMarks();
            boolean first = true;
            for (Map.Entry<String, Integer> e : map.entrySet()) {
                if (!first) marksBuffer.append(";");
                marksBuffer.append(e.getKey()).append(":").append(e.getValue());
                first = false;
            }

            sb.append(escapeCsv(s.getId())).append(",")
              .append(escapeCsv(s.getRollNumber())).append(",")
              .append(escapeCsv(s.getName())).append(",")
              .append(escapeCsv(s.getClassName())).append(",")
              .append(escapeCsv(s.getEmail())).append(",")
              .append(escapeCsv(marksBuffer.toString())).append("\n");
        }
        writeFile(STUDENTS_FILE, sb.toString());
    }

    // ==========================================
    // ATTENDANCE PERSISTENCE
    // ==========================================

    private void loadAttendance() {
        attendanceRecords.clear();
        File file = new File(ATTENDANCE_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 4) {
                    AttendanceRecord rec = new AttendanceRecord();
                    rec.setId(parts[0].trim());
                    rec.setStudentId(parts[1].trim());
                    rec.setDate(parts[2].trim());
                    rec.setPresent(Boolean.parseBoolean(parts[3].trim()));
                    attendanceRecords.add(rec);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading attendance: " + e.getMessage());
        }
    }

    public synchronized void saveAttendance() {
        // Demonstrates: Strings and StringBuffer
        StringBuffer sb = new StringBuffer();
        sb.append("id,studentId,date,present\n");
        for (AttendanceRecord rec : attendanceRecords) {
            sb.append(escapeCsv(rec.getId())).append(",")
              .append(escapeCsv(rec.getStudentId())).append(",")
              .append(escapeCsv(rec.getDate())).append(",")
              .append(rec.isPresent()).append("\n");
        }
        writeFile(ATTENDANCE_FILE, sb.toString());
    }

    // ==========================================
    // SUBJECTS PERSISTENCE
    // ==========================================

    private void loadSubjects() {
        subjects.clear();
        File file = new File(SUBJECTS_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 4) {
                    Subject sub = new Subject();
                    sub.setId(parts[0].trim());
                    sub.setCode(parts[1].trim());
                    sub.setName(parts[2].trim());
                    sub.setMaxMarks(Integer.parseInt(parts[3].trim()));
                    subjects.add(sub);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading subjects: " + e.getMessage());
        }
    }

    public synchronized void saveSubjects() {
        // Demonstrates: Strings and StringBuffer
        StringBuffer sb = new StringBuffer();
        sb.append("id,code,name,maxMarks\n");
        for (Subject sub : subjects) {
            sb.append(escapeCsv(sub.getId())).append(",")
              .append(escapeCsv(sub.getCode())).append(",")
              .append(escapeCsv(sub.getName())).append(",")
              .append(sub.getMaxMarks()).append("\n");
        }
        writeFile(SUBJECTS_FILE, sb.toString());
    }

    // ==========================================
    // SETTINGS PERSISTENCE
    // ==========================================

    private void loadSettings() {
        File file = new File(SETTINGS_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // Skip header
            if ((line = reader.readLine()) != null) {
                line = line.trim();
                String[] parts = line.split(",", -1);
                if (parts.length >= 4) {
                    settings.setAttendanceRiskThreshold(Double.parseDouble(parts[0].trim()));
                    settings.setAttendanceWarningThreshold(Double.parseDouble(parts[1].trim()));
                    settings.setMarksRiskThreshold(Double.parseDouble(parts[2].trim()));
                    settings.setMarksWarningThreshold(Double.parseDouble(parts[3].trim()));
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading settings: " + e.getMessage());
        }
    }

    public synchronized void saveSettings() {
        // Demonstrates: Strings and StringBuffer
        StringBuffer sb = new StringBuffer();
        sb.append("attendanceRiskThreshold,attendanceWarningThreshold,marksRiskThreshold,marksWarningThreshold\n");
        sb.append(settings.getAttendanceRiskThreshold()).append(",")
          .append(settings.getAttendanceWarningThreshold()).append(",")
          .append(settings.getMarksRiskThreshold()).append(",")
          .append(settings.getMarksWarningThreshold()).append("\n");
        writeFile(SETTINGS_FILE, sb.toString());
    }

    private void writeFile(String filePath, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
        } catch (IOException e) {
            System.err.println("Error writing file " + filePath + ": " + e.getMessage());
        }
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }

    // Demonstrates: Vector Accessors

    public Vector<User> getUsers() {
        return users;
    }

    public Vector<Student> getStudents() {
        return students;
    }

    public Vector<AttendanceRecord> getAttendanceRecords() {
        return attendanceRecords;
    }

    public Vector<Subject> getSubjects() {
        return subjects;
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
        saveSettings();
    }
}
