package com.edutrack.model;

/**
 * Reportable Interface
 * Defines a contract for any entity capable of generating a formatted text report.
 * 
 * Demonstrates: Interface
 */
public interface Reportable {
    
    /**
     * Generates a structured string report of the entity.
     * 
     * @return Formatted report text.
     */
    String generateReport();
}
