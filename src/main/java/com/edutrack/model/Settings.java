package com.edutrack.model;

/**
 * Settings Entity
 * Holds customizable academic risk thresholds configured by teachers.
 * Enables teachers to adjust criteria for early academic intervention (SDG 4).
 * 
 * Demonstrates: Encapsulation
 */
public class Settings {

    // Demonstrates: Encapsulation
    private double attendanceRiskThreshold;    // Default: 75.0%
    private double attendanceWarningThreshold; // Default: 80.0%
    private double marksRiskThreshold;         // Default: 40.0%
    private double marksWarningThreshold;      // Default: 50.0%

    /**
     * Default constructor setting institutional baseline defaults.
     */
    public Settings() {
        this.attendanceRiskThreshold = 75.0;
        this.attendanceWarningThreshold = 80.0;
        this.marksRiskThreshold = 40.0;
        this.marksWarningThreshold = 50.0;
    }

    /**
     * Parameterized constructor.
     * 
     * @param attendanceRiskThreshold Threshold below which attendance is considered at-risk.
     * @param attendanceWarningThreshold Threshold below which attendance needs attention.
     * @param marksRiskThreshold Marks average below which student is at risk of failing.
     * @param marksWarningThreshold Marks average below which student needs academic support.
     */
    public Settings(double attendanceRiskThreshold, double attendanceWarningThreshold,
                    double marksRiskThreshold, double marksWarningThreshold) {
        this.attendanceRiskThreshold = attendanceRiskThreshold;
        this.attendanceWarningThreshold = attendanceWarningThreshold;
        this.marksRiskThreshold = marksRiskThreshold;
        this.marksWarningThreshold = marksWarningThreshold;
    }

    // Getters and Setters

    public double getAttendanceRiskThreshold() {
        return attendanceRiskThreshold;
    }

    public void setAttendanceRiskThreshold(double attendanceRiskThreshold) {
        this.attendanceRiskThreshold = attendanceRiskThreshold;
    }

    public double getAttendanceWarningThreshold() {
        return attendanceWarningThreshold;
    }

    public void setAttendanceWarningThreshold(double attendanceWarningThreshold) {
        this.attendanceWarningThreshold = attendanceWarningThreshold;
    }

    public double getMarksRiskThreshold() {
        return marksRiskThreshold;
    }

    public void setMarksRiskThreshold(double marksRiskThreshold) {
        this.marksRiskThreshold = marksRiskThreshold;
    }

    public double getMarksWarningThreshold() {
        return marksWarningThreshold;
    }

    public void setMarksWarningThreshold(double marksWarningThreshold) {
        this.marksWarningThreshold = marksWarningThreshold;
    }
}
