package com.jobapplier.service;

import com.jobapplier.model.Resume;
import com.jobapplier.repository.ResumeRepository;
import com.jobapplier.util.ResumeParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeService {
    
    private final ResumeRepository resumeRepository;
    private final ResumeParser resumeParser;
    
    /**
     * Parse and save resume
     */
    @Transactional
    public Resume parseAndSaveResume(String filePath) throws IOException {
        log.info("Parsing resume from: {}", filePath);
        
        Resume resume = resumeParser.parseResume(filePath);
        resume.setIsActive(true);
        
        // Deactivate other resumes
        List<Resume> activeResumes = resumeRepository.findByIsActiveTrue();
        for (Resume r : activeResumes) {
            r.setIsActive(false);
            resumeRepository.save(r);
        }
        
        Resume saved = resumeRepository.save(resume);
        log.info("Resume saved successfully with ID: {}", saved.getId());
        
        return saved;
    }
    
    /**
     * Get active resume
     */
    public Resume getActiveResume() {
        return resumeRepository.findFirstByIsActiveTrueOrderByCreatedAtDesc()
                .orElse(null);
    }
    
    /**
     * Get all resumes
     */
    public List<Resume> getAllResumes() {
        return resumeRepository.findAll();
    }
    
    /**
     * Get resume by ID
     */
    public Optional<Resume> getResumeById(Long id) {
        return resumeRepository.findById(id);
    }
    
    /**
     * Activate resume
     */
    @Transactional
    public Resume activateResume(Long id) {
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resume not found: " + id));
        
        // Deactivate all other resumes
        List<Resume> activeResumes = resumeRepository.findByIsActiveTrue();
        for (Resume r : activeResumes) {
            r.setIsActive(false);
            resumeRepository.save(r);
        }
        
        resume.setIsActive(true);
        return resumeRepository.save(resume);
    }
    
    /**
     * Delete resume
     */
    @Transactional
    public void deleteResume(Long id) {
        resumeRepository.deleteById(id);
        log.info("Deleted resume with ID: {}", id);
    }
}
