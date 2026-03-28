package com.jobapplier.controller;

import com.jobapplier.model.Resume;
import com.jobapplier.service.ResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {
    
    private final ResumeService resumeService;
    
    @Value("${resume.directory}")
    private String resumeDirectory;
    
    /**
     * Parse resume from the configured directory
     */
    @PostMapping("/parse")
    public ResponseEntity<?> parseResume() {
        try {
            // Find the first PDF file in the resume directory
            File dir = new File(resumeDirectory);
            File[] pdfFiles = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".pdf"));
            
            if (pdfFiles == null || pdfFiles.length == 0) {
                return ResponseEntity.badRequest().body("No PDF resume found in: " + resumeDirectory);
            }
            
            String resumePath = pdfFiles[0].getAbsolutePath();
            log.info("Parsing resume: {}", resumePath);
            
            Resume resume = resumeService.parseAndSaveResume(resumePath);
            return ResponseEntity.ok(resume);
            
        } catch (IOException e) {
            log.error("Error parsing resume", e);
            return ResponseEntity.internalServerError().body("Error parsing resume: " + e.getMessage());
        }
    }
    
    /**
     * Get active resume
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActiveResume() {
        Resume resume = resumeService.getActiveResume();
        if (resume == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(resume);
    }
    
    /**
     * Get all resumes
     */
    @GetMapping
    public ResponseEntity<List<Resume>> getAllResumes() {
        return ResponseEntity.ok(resumeService.getAllResumes());
    }
    
    /**
     * Activate a specific resume
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<Resume> activateResume(@PathVariable Long id) {
        try {
            Resume resume = resumeService.activateResume(id);
            return ResponseEntity.ok(resume);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
