package com.edutrack.service;

import com.edutrack.model.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.Vector;

/**
 * SubjectService
 * Manages academic subjects, allowing teachers to dynamically add, rename,
 * and remove courses without hardcoding.
 * 
 * Demonstrates: Vector
 * Demonstrates: Encapsulation
 */
@Service
public class SubjectService {

    private final DataStorageService dataStorageService;

    @Autowired
    public SubjectService(DataStorageService dataStorageService) {
        this.dataStorageService = dataStorageService;
    }

    /**
     * Retrieves all configured subjects.
     * 
     * Demonstrates: Vector
     * @return Vector of Subject objects.
     */
    public Vector<Subject> getAllSubjects() {
        return dataStorageService.getSubjects();
    }

    /**
     * Finds a subject by its unique ID.
     * 
     * @param id Subject unique ID.
     * @return Subject if found, null otherwise.
     */
    public Subject getSubjectById(String id) {
        if (id == null) return null;
        for (Subject s : dataStorageService.getSubjects()) {
            if (s.getId().equals(id)) {
                return s;
            }
        }
        return null;
    }

    /**
     * Finds a subject by its code (case-insensitive).
     * 
     * @param code Subject code (e.g. "JAVA").
     * @return Subject if found, null otherwise.
     */
    public Subject getSubjectByCode(String code) {
        if (code == null) return null;
        for (Subject s : dataStorageService.getSubjects()) {
            if (s.getCode().equalsIgnoreCase(code.trim())) {
                return s;
            }
        }
        return null;
    }

    /**
     * Adds a new subject to the curriculum.
     * 
     * @param code Subject code (e.g., CS101, JAVA).
     * @param name Subject full title.
     * @param maxMarks Maximum marks (default 100).
     * @return Created Subject.
     * @throws IllegalArgumentException if code is duplicate or invalid.
     */
    public synchronized Subject addSubject(String code, String name, int maxMarks) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject code cannot be empty.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject name cannot be empty.");
        }
        if (getSubjectByCode(code) != null) {
            throw new IllegalArgumentException("Subject code '" + code.toUpperCase() + "' already exists.");
        }

        Subject subject = new Subject(
                UUID.randomUUID().toString(),
                code.trim().toUpperCase(),
                name.trim(),
                maxMarks > 0 ? maxMarks : 100
        );

        dataStorageService.getSubjects().add(subject);
        dataStorageService.saveSubjects();
        return subject;
    }

    /**
     * Updates an existing subject's details.
     * 
     * @param id Subject ID.
     * @param code Updated code.
     * @param name Updated title.
     * @param maxMarks Updated max marks.
     * @return Updated Subject.
     */
    public synchronized Subject updateSubject(String id, String code, String name, int maxMarks) {
        Subject s = getSubjectById(id);
        if (s == null) {
            throw new IllegalArgumentException("Subject not found.");
        }
        Subject existingWithCode = getSubjectByCode(code);
        if (existingWithCode != null && !existingWithCode.getId().equals(id)) {
            throw new IllegalArgumentException("Another subject with code '" + code.toUpperCase() + "' already exists.");
        }

        s.setCode(code);
        s.setName(name);
        s.setMaxMarks(maxMarks > 0 ? maxMarks : 100);
        dataStorageService.saveSubjects();
        return s;
    }

    /**
     * Deletes a subject from the system and cleans up marks from students.
     * 
     * @param id Subject ID to remove.
     * @return true if removed, false otherwise.
     */
    public synchronized boolean deleteSubject(String id) {
        Subject target = getSubjectById(id);
        if (target != null) {
            dataStorageService.getSubjects().remove(target);
            dataStorageService.saveSubjects();

            // Also remove subject marks from all students
            for (var student : dataStorageService.getStudents()) {
                student.getSubjectMarks().remove(target.getCode());
                student.getSubjectMarks().remove(target.getId());
            }
            dataStorageService.saveStudents();
            return true;
        }
        return false;
    }
}
