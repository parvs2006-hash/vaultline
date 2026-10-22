package com.edutrack.service;

import com.edutrack.model.AttendanceRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

/**
 * AttendanceService
 * Manages daily and historical attendance logging for enrolled students.
 * Enforces data consistency by preventing duplicate records on the same calendar date.
 * 
 * Demonstrates: Vector
 * Demonstrates: Control structures (loops and conditionals)
 */
@Service
public class AttendanceService {

    private final DataStorageService dataStorageService;

    @Autowired
    public AttendanceService(DataStorageService dataStorageService) {
        this.dataStorageService = dataStorageService;
    }

    /**
     * Marks or updates attendance for a specific student and date.
     * Guarantees no duplicate records exist for the same student on the same date.
     * 
     * @param studentId Student's unique ID.
     * @param date Date formatted as YYYY-MM-DD.
     * @param present True if present, false if absent.
     */
    public synchronized void markAttendance(String studentId, String date, boolean present) {
        if (studentId == null || date == null) return;
        Vector<AttendanceRecord> records = dataStorageService.getAttendanceRecords();

        // Check for existing record on the same date
        boolean updated = false;
        // Demonstrates: Control structures (enhanced-for loop)
        for (AttendanceRecord rec : records) {
            if (rec.getStudentId().equals(studentId) && rec.getDate().equals(date)) {
                rec.setPresent(present);
                updated = true;
                break;
            }
        }

        if (!updated) {
            AttendanceRecord newRec = new AttendanceRecord(
                    UUID.randomUUID().toString(),
                    studentId,
                    date,
                    present
            );
            records.add(newRec);
        }

        dataStorageService.saveAttendance();
    }

    /**
     * Batch records attendance for multiple students for a specific date.
     * 
     * @param date Target date (YYYY-MM-DD).
     * @param attendanceMap Map of studentId -> isPresent.
     */
    public synchronized void recordBatchAttendance(String date, Map<String, Boolean> attendanceMap) {
        if (date == null || attendanceMap == null) return;
        for (Map.Entry<String, Boolean> entry : attendanceMap.entrySet()) {
            markAttendance(entry.getKey(), date, Boolean.TRUE.equals(entry.getValue()));
        }
    }

    /**
     * Retrieves attendance status of all students for a specific date.
     * 
     * @param date Target date (YYYY-MM-DD).
     * @return Map of studentId to isPresent boolean.
     */
    public Map<String, Boolean> getAttendanceForDate(String date) {
        Map<String, Boolean> map = new HashMap<>();
        if (date == null) return map;

        for (AttendanceRecord rec : dataStorageService.getAttendanceRecords()) {
            if (rec.getDate().equals(date)) {
                map.put(rec.getStudentId(), rec.isPresent());
            }
        }
        return map;
    }

    /**
     * Retrieves all recorded dates in chronological order.
     * 
     * @return Sorted list of distinct date strings.
     */
    public List<String> getAllRecordedDates() {
        Set<String> dateSet = new HashSet<>();
        for (AttendanceRecord rec : dataStorageService.getAttendanceRecords()) {
            dateSet.add(rec.getDate());
        }
        List<String> sortedDates = new ArrayList<>(dateSet);
        Collections.sort(sortedDates);
        return sortedDates;
    }

    /**
     * Retrieves the chronological attendance log for a specific student.
     * 
     * @param studentId Student ID.
     * @return List of AttendanceRecord items.
     */
    public List<AttendanceRecord> getStudentAttendanceHistory(String studentId) {
        List<AttendanceRecord> list = new ArrayList<>();
        if (studentId == null) return list;

        for (AttendanceRecord rec : dataStorageService.getAttendanceRecords()) {
            if (rec.getStudentId().equals(studentId)) {
                list.add(rec);
            }
        }
        list.sort((a, b) -> b.getDate().compareTo(a.getDate())); // Newest first
        return list;
    }

    /**
     * Computes the attendance percentage for a specific student.
     * 
     * @param studentId Student ID.
     * @return Attendance percentage (0.0 to 100.0), or -1.0 if no records exist.
     */
    public double calculateAttendancePercentage(String studentId) {
        if (studentId == null) return -1.0;
        int totalDays = 0;
        int presentDays = 0;

        for (AttendanceRecord rec : dataStorageService.getAttendanceRecords()) {
            if (rec.getStudentId().equals(studentId)) {
                totalDays++;
                if (rec.isPresent()) {
                    presentDays++;
                }
            }
        }

        if (totalDays == 0) {
            return -1.0; // No attendance recorded yet
        }
        return ((double) presentDays / totalDays) * 100.0;
    }

    /**
     * Counts the total number of attendance sessions recorded for a student.
     * 
     * @param studentId Student ID.
     * @return Total session count.
     */
    public int getTotalDaysForStudent(String studentId) {
        int count = 0;
        for (AttendanceRecord rec : dataStorageService.getAttendanceRecords()) {
            if (rec.getStudentId().equals(studentId)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Counts the number of attended sessions for a student.
     * 
     * @param studentId Student ID.
     * @return Present session count.
     */
    public int getPresentDaysForStudent(String studentId) {
        int count = 0;
        for (AttendanceRecord rec : dataStorageService.getAttendanceRecords()) {
            if (rec.getStudentId().equals(studentId) && rec.isPresent()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Calculates the aggregate attendance percentage across the entire student body.
     * 
     * @return Class average attendance percentage, or 0.0 if empty.
     */
    public double calculateClassAverageAttendance() {
        Vector<AttendanceRecord> records = dataStorageService.getAttendanceRecords();
        if (records.isEmpty()) {
            return 0.0;
        }
        int presentCount = 0;
        for (AttendanceRecord rec : records) {
            if (rec.isPresent()) {
                presentCount++;
            }
        }
        return ((double) presentCount / records.size()) * 100.0;
    }
}
