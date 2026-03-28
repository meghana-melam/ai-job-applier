package com.jobapplier.controller;

import com.jobapplier.model.Job;
import com.jobapplier.repository.JobRepository;
import com.jobapplier.util.JobParser;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/manual")
@RequiredArgsConstructor
public class ManualJobController {
    
    private final JobRepository jobRepository;
    private final JobParser jobParser;
    
    /**
     * Add a job manually - simplified version
     */
    @PostMapping("/add-job")
    public ResponseEntity<?> addJob(@RequestBody ManualJobRequest request) {
        try {
            Job job = new Job();
            job.setTitle(jobParser.normalizeJobTitle(request.getTitle()));
            job.setCompany(request.getCompany());
            job.setLocation(request.getLocation() != null ? request.getLocation() : "Not specified");
            job.setJobUrl(request.getJobUrl());
            job.setDescription(request.getDescription());
            job.setSourcePlatform(request.getSource() != null ? request.getSource().toUpperCase() : "MANUAL");
            job.setScrapedDate(LocalDateTime.now());
            job.setPostedDate(LocalDateTime.now());
            job.setIsEasyApply(request.getIsEasyApply() != null ? request.getIsEasyApply() : false);
            
            // Parse skills from description or use provided skills
            if (request.getSkills() != null && !request.getSkills().isEmpty()) {
                job.setRequiredSkills(request.getSkills());
            }
            
            // Enrich job data (extracts skills, salary, etc. from description)
            jobParser.enrichJobData(job);
            
            // Set experience level if provided
            if (request.getExperience() != null) {
                job.setExperienceLevel(request.getExperience());
            }
            
            Job savedJob = jobRepository.save(job);
            log.info("Manually added job: {} at {}", savedJob.getTitle(), savedJob.getCompany());
            
            return ResponseEntity.ok(Map.of(
                "message", "Job added successfully",
                "job", savedJob
            ));
            
        } catch (Exception e) {
            log.error("Error adding manual job", e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to add job: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Add multiple jobs at once
     */
    @PostMapping("/add-jobs-bulk")
    public ResponseEntity<?> addJobsBulk(@RequestBody List<ManualJobRequest> requests) {
        try {
            List<Job> savedJobs = requests.stream().map(request -> {
                Job job = new Job();
                job.setTitle(jobParser.normalizeJobTitle(request.getTitle()));
                job.setCompany(request.getCompany());
                job.setLocation(request.getLocation() != null ? request.getLocation() : "Not specified");
                job.setJobUrl(request.getJobUrl());
                job.setDescription(request.getDescription());
                job.setSourcePlatform(request.getSource() != null ? request.getSource().toUpperCase() : "MANUAL");
                job.setScrapedDate(LocalDateTime.now());
                job.setPostedDate(LocalDateTime.now());
                job.setIsEasyApply(request.getIsEasyApply() != null ? request.getIsEasyApply() : false);
                
                if (request.getSkills() != null && !request.getSkills().isEmpty()) {
                    job.setRequiredSkills(request.getSkills());
                }
                
                jobParser.enrichJobData(job);
                
                if (request.getExperience() != null) {
                    job.setExperienceLevel(request.getExperience());
                }
                
                return jobRepository.save(job);
            }).collect(Collectors.toList());
            
            log.info("Bulk added {} jobs", savedJobs.size());
            
            return ResponseEntity.ok(Map.of(
                "message", "Jobs added successfully",
                "count", savedJobs.size(),
                "jobs", savedJobs
            ));
            
        } catch (Exception e) {
            log.error("Error adding bulk jobs", e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to add jobs: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Quick add job with minimal fields - for fast entry
     */
    @PostMapping("/quick-add")
    public ResponseEntity<?> quickAdd(
            @RequestParam String title,
            @RequestParam String company,
            @RequestParam String url,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String skills,
            @RequestParam(defaultValue = "MANUAL") String source
    ) {
        try {
            Job job = new Job();
            job.setTitle(jobParser.normalizeJobTitle(title));
            job.setCompany(company);
            job.setLocation(location != null ? location : "Not specified");
            job.setJobUrl(url);
            job.setDescription(description != null ? description : "");
            job.setSourcePlatform(source.toUpperCase());
            job.setScrapedDate(LocalDateTime.now());
            job.setPostedDate(LocalDateTime.now());
            job.setIsEasyApply(false);
            
            // Parse skills if provided as comma-separated
            if (skills != null && !skills.trim().isEmpty()) {
                List<String> skillList = Arrays.stream(skills.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
                job.setRequiredSkills(skillList);
            }
            
            jobParser.enrichJobData(job);
            
            Job savedJob = jobRepository.save(job);
            log.info("Quick added job: {} at {}", savedJob.getTitle(), savedJob.getCompany());
            
            return ResponseEntity.ok(Map.of(
                "message", "Job added successfully",
                "jobId", savedJob.getId(),
                "title", savedJob.getTitle()
            ));
            
        } catch (Exception e) {
            log.error("Error quick adding job", e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to add job: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Delete a manually added job
     */
    @DeleteMapping("/delete-job/{id}")
    public ResponseEntity<?> deleteJob(@PathVariable Long id) {
        try {
            Job job = jobRepository.findById(id).orElse(null);
            if (job == null) {
                return ResponseEntity.notFound().build();
            }
            
            jobRepository.delete(job);
            log.info("Deleted job: {} (ID: {})", job.getTitle(), id);
            
            return ResponseEntity.ok(Map.of(
                "message", "Job deleted successfully",
                "jobId", id
            ));
            
        } catch (Exception e) {
            log.error("Error deleting job", e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to delete job: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get all manually added jobs
     */
    @GetMapping("/list")
    public ResponseEntity<?> listManualJobs() {
        try {
            List<Job> manualJobs = jobRepository.findBySourcePlatform("MANUAL");
            return ResponseEntity.ok(Map.of(
                "count", manualJobs.size(),
                "jobs", manualJobs
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to retrieve jobs: " + e.getMessage()
            ));
        }
    }
}

/**
 * Request DTO for manual job entry
 */
@Data
class ManualJobRequest {
    private String title;
    private String company;
    private String location;
    private String jobUrl;
    private String description;
    private String experience;
    private List<String> skills;
    private String source;  // LINKEDIN, NAUKRI, MANUAL, etc.
    private Boolean isEasyApply;
}
