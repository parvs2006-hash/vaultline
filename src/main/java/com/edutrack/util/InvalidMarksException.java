package com.edutrack.util;

/**
 * InvalidMarksException
 * Custom checked exception thrown when marks value is outside the allowed range (0 to 100).
 * 
 * Demonstrates: Custom exceptions
 */
public class InvalidMarksException extends Exception {

    /**
     * Constructs the exception with a specific error message.
     * 
     * @param message Explanatory message for the user.
     */
    public InvalidMarksException(String message) {
        super(message);
    }
}
