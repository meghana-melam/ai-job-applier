package com.jobapplier.controller;

import com.jobapplier.ai.CoverLetterGenerator;
import com.jobapplier.model.Application;
import com.jobapplier.model.Job;
import com.jobapplier.model.Resume;
import com.jobapplier.repository.ApplicationRepository;
import com.jobapplier.repository.JobRepository;
import com.jobapplier.service.JobService;
import com.jobapplier.service.ResumeService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/apply-assist")
@RequiredArgsConstructor
public class ManualApplyController {
    
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final ResumeService resumeService;
    private final CoverLetterGenerator coverLetterGenerator;
    private final JobService jobService;
    
    /**
     * Get everything you need to manually apply to a job
     */
    @GetMapping("/prepare/{jobId}")
    public ResponseEntity<?> prepareApplication(@PathVariable Long jobId) {
        try {
            Job job = jobRepository.findById(jobId).orElse(null);
            if (job == null) {
                return ResponseEntity.notFound().build();
            }
            
            Resume resume = resumeService.getActiveResume();
            if (resume == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "No active resume found"));
            }
            
            // Generate cover letter
            String coverLetter = coverLetterGenerator.generateCoverLetter(job, resume);
            
            // Create application record as "PENDING"
            Application application = new Application();
            application.setJob(job);
            application.setResume(resume);
            application.setAppliedDate(LocalDateTime.now());
            application.setCoverLetter(coverLetter);
            application.setStatus("PENDING_MANUAL");
            applicationRepository.save(application);
            
            // Mark job as in progress
            job.setStatus("APPLYING");
            jobRepository.save(job);
            
            log.info("Prepared application for manual apply: {} at {}", job.getTitle(), job.getCompany());
            
            return ResponseEntity.ok(Map.of(
                "jobId", job.getId(),
                "jobTitle", job.getTitle(),
                "company", job.getCompany(),
                "location", job.getLocation(),
                "jobUrl", job.getJobUrl(),
                "coverLetter", coverLetter,
                "instructions", "1. Copy the cover letter above\n2. Open the job URL\n3. Apply manually\n4. Mark as applied using: /confirm/" + job.getId()
            ));
            
        } catch (Exception e) {
            log.error("Error preparing application", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Confirm you manually applied to a job
     */
    @PostMapping("/confirm/{jobId}")
    public ResponseEntity<?> confirmApplication(@PathVariable Long jobId) {
        try {
            Job job = jobRepository.findById(jobId).orElse(null);
            if (job == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Find pending application and mark as success
            Application app = applicationRepository.findByJobId(jobId).stream()
                    .filter(a -> "PENDING_MANUAL".equals(a.getStatus()))
                    .findFirst()
                    .orElse(null);
            
            if (app != null) {
                app.setStatus("SUCCESS_MANUAL");
                app.setAppliedDate(LocalDateTime.now());
                applicationRepository.save(app);
            }
            
            // Mark job as applied
            job.setStatus("APPLIED");
            jobRepository.save(job);
            
            log.info("Confirmed manual application: {} at {}", job.getTitle(), job.getCompany());
            
            return ResponseEntity.ok(Map.of(
                "message", "Application confirmed!",
                "job", job.getTitle(),
                "company", job.getCompany()
            ));
            
        } catch (Exception e) {
            log.error("Error confirming application", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get batch of jobs ready to apply with cover letters
     */
    @GetMapping("/batch")
    public ResponseEntity<?> getBatchToApply(@RequestParam(defaultValue = "5") int count) {
        try {
            Resume resume = resumeService.getActiveResume();
            if (resume == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "No active resume found"));
            }
            
            List<Job> matchedJobs = jobService.getMatchedJobs().stream()
                    .filter(j -> !"APPLIED".equals(j.getStatus()))
                    .limit(count)
                    .toList();
            
            List<ApplicationPackage> packages = new ArrayList<>();
            
            for (Job job : matchedJobs) {
                String coverLetter = coverLetterGenerator.generateCoverLetter(job, resume);
                
                // Create pending application
                Application application = new Application();
                application.setJob(job);
                application.setResume(resume);
                application.setAppliedDate(LocalDateTime.now());
                application.setCoverLetter(coverLetter);
                application.setStatus("PENDING_MANUAL");
                applicationRepository.save(application);
                
                packages.add(new ApplicationPackage(
                    job.getId(),
                    job.getTitle(),
                    job.getCompany(),
                    job.getLocation(),
                    job.getJobUrl(),
                    job.getMatchScore() != null ? job.getMatchScore().intValue() : 0,
                    coverLetter
                ));
                
                job.setStatus("APPLYING");
                jobRepository.save(job);
            }
            
            log.info("Prepared {} jobs for manual application", packages.size());
            
            return ResponseEntity.ok(Map.of(
                "count", packages.size(),
                "applications", packages,
                "instructions", "Apply to each job manually, then confirm with: POST /api/apply-assist/confirm/{jobId}"
            ));
            
        } catch (Exception e) {
            log.error("Error preparing batch", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Cancel a pending manual application
     */
    @DeleteMapping("/cancel/{jobId}")
    public ResponseEntity<?> cancelApplication(@PathVariable Long jobId) {
        try {
            Job job = jobRepository.findById(jobId).orElse(null);
            if (job == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Delete pending application
            applicationRepository.findByJobId(jobId).stream()
                    .filter(a -> "PENDING_MANUAL".equals(a.getStatus()))
                    .forEach(applicationRepository::delete);
            
            // Reset job status
            job.setStatus("MATCHED");
            jobRepository.save(job);
            
            return ResponseEntity.ok(Map.of("message", "Application cancelled"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

@Data
class ApplicationPackage {
    private Long jobId;
    private String title;
    private String company;
    private String location;
    private String jobUrl;
    private Integer matchScore;
    private String coverLetter;
    
    public ApplicationPackage(Long jobId, String title, String company, String location, 
                             String jobUrl, Integer matchScore, String coverLetter) {
        this.jobId = jobId;
        this.title = title;
        this.company = company;
        this.location = location;
        this.jobUrl = jobUrl;
        this.matchScore = matchScore;
        this.coverLetter = coverLetter;
    }
}
