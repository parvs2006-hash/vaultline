package com.edutrack.model;

/**
 * ClassSummary Entity
 * Encapsulates aggregated statistical metrics for an entire class or cohort.
 * Computes highest average, lowest average, class mean, and pass percentage.
 * Implements Reportable to generate formal class-wide academic summaries.
 * 
 * Demonstrates: Interface
 */
public class ClassSummary implements Reportable {

    // Demonstrates: Encapsulation
    private int totalStudents;
    private double classAverage;
    private double highestAverage;
    private double lowestAverage;
    private double passPercentage;
    private double averageAttendance;
    private int atRiskCount;
    private int needsAttentionCount;
    private int goodCount;

    /**
     * Default constructor.
     */
    public ClassSummary() {
    }

    /**
     * Parameterized constructor initializing summary statistics.
     * 
     * @param totalStudents Total enrolled students.
     * @param classAverage Mean percentage across class.
     * @param highestAverage Top student percentage.
     * @param lowestAverage Lowest student percentage.
     * @param passPercentage Percentage of passing students.
     * @param averageAttendance Class attendance percentage.
     * @param atRiskCount Number of at-risk students.
     * @param needsAttentionCount Number of students needing attention.
     * @param goodCount Number of students in good standing.
     */
    public ClassSummary(int totalStudents, double classAverage, double highestAverage,
                        double lowestAverage, double passPercentage, double averageAttendance,
                        int atRiskCount, int needsAttentionCount, int goodCount) {
        this.totalStudents = totalStudents;
        this.classAverage = classAverage;
        this.highestAverage = highestAverage;
        this.lowestAverage = lowestAverage;
        this.passPercentage = passPercentage;
        this.averageAttendance = averageAttendance;
        this.atRiskCount = atRiskCount;
        this.needsAttentionCount = needsAttentionCount;
        this.goodCount = goodCount;
    }

    // Demonstrates: Interface implementation
    // Demonstrates: Strings and StringBuffer
    @Override
    public String generateReport() {
        StringBuffer sb = new StringBuffer();
        sb.append("==================================================\n");
        sb.append("        INSTITUTIONAL CLASS SUMMARY REPORT        \n");
        sb.append("       Supporting UN SDG 4: Quality Education     \n");
        sb.append("==================================================\n");
        sb.append("Total Students Enrolled : ").append(totalStudents).append("\n");
        if (totalStudents > 0) {
            sb.append(String.format("Class Average Marks     : %.2f%%\n", classAverage));
            sb.append(String.format("Highest Average Marks   : %.2f%%\n", highestAverage));
            sb.append(String.format("Lowest Average Marks    : %.2f%%\n", lowestAverage));
            sb.append(String.format("Overall Pass Rate       : %.2f%%\n", passPercentage));
            sb.append(String.format("Average Attendance Rate : %.2f%%\n", averageAttendance));
            sb.append("--------------------------------------------------\n");
            sb.append("SDG 4 EARLY INTERVENTION STATUS BREAKDOWN:\n");
            sb.append("  - In Good Standing    : ").append(goodCount).append("\n");
            sb.append("  - Needs Attention     : ").append(needsAttentionCount).append("\n");
            sb.append("  - At Risk (Alert)     : ").append(atRiskCount).append("\n");
        } else {
            sb.append("Status: Empty cohort - No student records available.\n");
        }
        sb.append("==================================================\n");
        return sb.toString();
    }

    // Getters and Setters

    public int getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(int totalStudents) {
        this.totalStudents = totalStudents;
    }

    public double getClassAverage() {
        return classAverage;
    }

    public void setClassAverage(double classAverage) {
        this.classAverage = classAverage;
    }

    public double getHighestAverage() {
        return highestAverage;
    }

    public void setHighestAverage(double highestAverage) {
        this.highestAverage = highestAverage;
    }

    public double getLowestAverage() {
        return lowestAverage;
    }

    public void setLowestAverage(double lowestAverage) {
        this.lowestAverage = lowestAverage;
    }

    public double getPassPercentage() {
        return passPercentage;
    }

    public void setPassPercentage(double passPercentage) {
        this.passPercentage = passPercentage;
    }

    public double getAverageAttendance() {
        return averageAttendance;
    }

    public void setAverageAttendance(double averageAttendance) {
        this.averageAttendance = averageAttendance;
    }

    public int getAtRiskCount() {
        return atRiskCount;
    }

    public void setAtRiskCount(int atRiskCount) {
        this.atRiskCount = atRiskCount;
    }

    public int getNeedsAttentionCount() {
        return needsAttentionCount;
    }

    public void setNeedsAttentionCount(int needsAttentionCount) {
        this.needsAttentionCount = needsAttentionCount;
    }

    public int getGoodCount() {
        return goodCount;
    }

    public void setGoodCount(int goodCount) {
        this.goodCount = goodCount;
    }
}
