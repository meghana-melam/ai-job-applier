package com.jobapplier.service;

import com.jobapplier.ai.CoverLetterGenerator;
import com.jobapplier.model.Application;
import com.jobapplier.model.Job;
import com.jobapplier.model.Resume;
import com.jobapplier.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplyService {
    
    private final ApplicationRepository applicationRepository;
    private final JobService jobService;
    private final ResumeService resumeService;
    private final CoverLetterGenerator coverLetterGenerator;
    
    /**
     * Apply to jobs automatically (DEPRECATED - automation blocked by bot detection)
     * Use /api/apply-assist endpoints for manual application with AI cover letters
     */
    @Transactional
    public List<Application> applyToJobs(int maxApplications) {
        log.warn("Automatic application is disabled due to bot detection. Use /api/apply-assist endpoints instead.");
        return new ArrayList<>();
    }
    
    /**
     * Apply to a single job (DEPRECATED - use manual assist)
     */
    @Transactional
    public Application applyToJob(Job job, Resume resume) {
        log.info("Creating application record for: {} at {}", job.getTitle(), job.getCompany());
        
        Application application = new Application();
        application.setJob(job);
        application.setResume(resume);
        application.setAppliedDate(LocalDateTime.now());
        
        try {
            // Generate cover letter
            String coverLetter = coverLetterGenerator.generateCoverLetter(job, resume);
            application.setCoverLetter(coverLetter);
            application.setStatus("PENDING_MANUAL");
            log.info("Cover letter generated for: {}", job.getTitle());
        } catch (Exception e) {
            application.setStatus("FAILED");
            application.setErrorMessage(e.getMessage());
            log.error("Error generating cover letter", e);
        }
        
        return applicationRepository.save(application);
    }
    
    /**
     * Get all applications
     */
    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }
    
    /**
     * Get applications by status
     */
    public List<Application> getApplicationsByStatus(String status) {
        return applicationRepository.findByStatus(status);
    }
    
    /**
     * Get recent applications
     */
    public List<Application> getRecentApplications(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return applicationRepository.findApplicationsSince(since);
    }
    
    /**
     * Get application statistics
     */
    public ApplicationStats getStats(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        
        Long total = applicationRepository.countApplicationsSince(since);
        Long successful = applicationRepository.countSuccessfulApplicationsSince(since);
        Long pending = applicationRepository.findPendingResponses().stream()
                .filter(app -> app.getAppliedDate().isAfter(since))
                .count();
        
        return new ApplicationStats(total, successful, pending);
    }
    
    public record ApplicationStats(Long total, Long successful, Long pending) {}
}
