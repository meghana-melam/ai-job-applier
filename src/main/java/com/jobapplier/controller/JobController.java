package com.jobapplier.controller;

import com.jobapplier.dto.ManualJobRequest;
import com.jobapplier.model.Job;
import com.jobapplier.service.JobService;
import com.jobapplier.util.JobParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {
    
    private final JobService jobService;
    private final JobParser jobParser;
    
    /**
     * Fetch jobs from all platforms
     */
    @PostMapping("/fetch")
    public ResponseEntity<List<Job>> fetchJobs(@RequestParam(defaultValue = "20") int maxPerPlatform) {
        List<Job> jobs = jobService.fetchJobs(maxPerPlatform);
        return ResponseEntity.ok(jobs);
    }
    
    /**
     * Match jobs with resume
     */
    @PostMapping("/match")
    public ResponseEntity<List<Job>> matchJobs() {
        List<Job> matchedJobs = jobService.matchJobs();
        return ResponseEntity.ok(matchedJobs);
    }
    
    /**
     * Get all jobs
     */
    @GetMapping
    public ResponseEntity<List<Job>> getAllJobs() {
        List<Job> jobs = jobService.getAllJobs();
        return ResponseEntity.ok(jobs);
    }
    
    /**
     * Get jobs by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Job>> getJobsByStatus(@PathVariable String status) {
        List<Job> jobs = jobService.getJobsByStatus(status);
        return ResponseEntity.ok(jobs);
    }
    
    /**
     * Get matched jobs
     */
    @GetMapping("/matched")
    public ResponseEntity<List<Job>> getMatchedJobs() {
        List<Job> jobs = jobService.getMatchedJobs();
        return ResponseEntity.ok(jobs);
    }
    
    /**
     * Get Easy Apply matched jobs
     */
    @GetMapping("/easy-apply")
    public ResponseEntity<List<Job>> getEasyApplyJobs() {
        List<Job> jobs = jobService.getEasyApplyMatchedJobs();
        return ResponseEntity.ok(jobs);
    }
    
    /**
     * Get recent jobs
     */
    @GetMapping("/recent")
    public ResponseEntity<List<Job>> getRecentJobs(@RequestParam(defaultValue = "7") int days) {
        List<Job> jobs = jobService.getRecentJobs(days);
        return ResponseEntity.ok(jobs);
    }
    
    /**
     * Get job by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Job> getJobById(@PathVariable Long id) {
        return jobService.getJobById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Update job status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Job> updateJobStatus(@PathVariable Long id, @RequestParam String status) {
        Job job = jobService.updateJobStatus(id, status);
        return ResponseEntity.ok(job);
    }
    
    /**
     * Get job statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<JobService.JobStats> getStats(@RequestParam(defaultValue = "7") int days) {
        JobService.JobStats stats = jobService.getStats(days);
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Manually add a job (copy-paste from LinkedIn/Naukri)
     */
    @PostMapping("/manual")
    public ResponseEntity<?> addManualJob(@RequestBody ManualJobRequest request) {
        try {
            log.info("Manually adding job: {} at {}", request.getTitle(), request.getCompany());
            
            // Validate required fields
            if (request.getTitle() == null || request.getTitle().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Job title is required"));
            }
            if (request.getCompany() == null || request.getCompany().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Company name is required"));
            }
            
            // Create job entity
            Job job = new Job();
            job.setTitle(jobParser.normalizeJobTitle(request.getTitle()));
            job.setCompany(request.getCompany().trim());
            job.setLocation(request.getLocation() != null ? request.getLocation() : "Not specified");
            job.setDescription(request.getDescription() != null ? request.getDescription() : "");
            job.setJobUrl(request.getJobUrl() != null ? request.getJobUrl() : "");
            job.setRequiredSkills(request.getSkills() != null ? request.getSkills() : List.of());
            job.setExperienceLevel(request.getExperienceLevel());
            job.setSourcePlatform(request.getSourcePlatform() != null ? request.getSourcePlatform() : "MANUAL");
            job.setIsEasyApply(request.getIsEasyApply() != null ? request.getIsEasyApply() : false);
            job.setScrapedDate(LocalDateTime.now());
            job.setPostedDate(LocalDateTime.now());
            job.setStatus("NEW");
            
            // Enrich with AI if description provided
            if (request.getDescription() != null && !request.getDescription().isEmpty()) {
                jobParser.enrichJobData(job);
            }
            
            // Save job
            Job savedJob = jobService.saveJob(job);
            
            log.info("Successfully added manual job with ID: {}", savedJob.getId());
            
            return ResponseEntity.ok(Map.of(
                "message", "Job added successfully",
                "jobId", savedJob.getId(),
                "job", savedJob
            ));
            
        } catch (Exception e) {
            log.error("Error adding manual job", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Bulk add multiple jobs manually
     */
    @PostMapping("/manual/bulk")
    public ResponseEntity<?> addBulkManualJobs(@RequestBody List<ManualJobRequest> requests) {
        try {
            log.info("Bulk adding {} manual jobs", requests.size());
            
            List<Job> savedJobs = new java.util.ArrayList<>();
            List<String> errors = new java.util.ArrayList<>();
            
            for (int i = 0; i < requests.size(); i++) {
                ManualJobRequest request = requests.get(i);
                try {
                    // Create and save job
                    Job job = new Job();
                    job.setTitle(jobParser.normalizeJobTitle(request.getTitle()));
                    job.setCompany(request.getCompany().trim());
                    job.setLocation(request.getLocation() != null ? request.getLocation() : "Not specified");
                    job.setDescription(request.getDescription() != null ? request.getDescription() : "");
                    job.setJobUrl(request.getJobUrl() != null ? request.getJobUrl() : "");
                    job.setRequiredSkills(request.getSkills() != null ? request.getSkills() : List.of());
                    job.setExperienceLevel(request.getExperienceLevel());
                    job.setSourcePlatform(request.getSourcePlatform() != null ? request.getSourcePlatform() : "MANUAL");
                    job.setIsEasyApply(request.getIsEasyApply() != null ? request.getIsEasyApply() : false);
                    job.setScrapedDate(LocalDateTime.now());
                    job.setPostedDate(LocalDateTime.now());
                    job.setStatus("NEW");
                    
                    if (request.getDescription() != null && !request.getDescription().isEmpty()) {
                        jobParser.enrichJobData(job);
                    }
                    
                    Job savedJob = jobService.saveJob(job);
                    savedJobs.add(savedJob);
                    
                } catch (Exception e) {
                    errors.add("Job " + (i + 1) + ": " + e.getMessage());
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "message", "Bulk job import completed",
                "successful", savedJobs.size(),
                "failed", errors.size(),
                "jobs", savedJobs,
                "errors", errors
            ));
            
        } catch (Exception e) {
            log.error("Error in bulk job import", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
