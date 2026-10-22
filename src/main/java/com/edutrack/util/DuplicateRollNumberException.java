package com.edutrack.util;

/**
 * DuplicateRollNumberException
 * Custom checked exception thrown when a teacher attempts to register
 * a student with a roll number that is already assigned to another student.
 * 
 * Demonstrates: Custom exceptions
 */
public class DuplicateRollNumberException extends Exception {

    /**
     * Constructs the exception with a specific error message.
     * 
     * @param message Explanatory message for the user.
     */
    public DuplicateRollNumberException(String message) {
        super(message);
    }
}
