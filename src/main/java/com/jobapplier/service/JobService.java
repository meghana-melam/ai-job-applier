package com.jobapplier.service;

import com.jobapplier.ai.ResumeMatcher;
import com.jobapplier.model.Job;
import com.jobapplier.model.Resume;
import com.jobapplier.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {
    
    private final JobRepository jobRepository;
    private final ResumeMatcher resumeMatcher;
    private final ResumeService resumeService;
    
    /**
     * Fetch jobs from all platforms (DEPRECATED - use instant analysis or manual entry)
     */
    @Transactional
    public List<Job> fetchJobs(int maxPerPlatform) {
        log.warn("fetchJobs called but scraping is disabled. Use /api/jobs/analyze or manual entry instead.");
        return new ArrayList<>();
    }
    
    /**
     * Save new jobs (avoid duplicates)
     */
    private List<Job> saveNewJobs(List<Job> jobs) {
        List<Job> savedJobs = new ArrayList<>();
        
        for (Job job : jobs) {
            if (!jobRepository.existsByJobUrl(job.getJobUrl())) {
                Job saved = jobRepository.save(job);
                savedJobs.add(saved);
                log.debug("Saved new job: {} at {}", job.getTitle(), job.getCompany());
            } else {
                log.debug("Job already exists: {}", job.getJobUrl());
            }
        }
        
        return savedJobs;
    }
    
    /**
     * Match jobs with resume and calculate scores
     */
    @Transactional
    public List<Job> matchJobs() {
        log.info("Starting job matching process");
        
        Resume activeResume = resumeService.getActiveResume();
        if (activeResume == null) {
            log.error("No active resume found");
            return List.of();
        }
        
        List<Job> newJobs = jobRepository.findByStatus("NEW");
        log.info("Found {} new jobs to match", newJobs.size());
        
        List<Job> matchedJobs = new ArrayList<>();
        
        for (Job job : newJobs) {
            try {
                double score = resumeMatcher.calculateMatchScore(job, activeResume);
                job.setMatchScore(score);
                
                if (resumeMatcher.meetsMinimumCriteria(score)) {
                    job.setStatus("MATCHED");
                    matchedJobs.add(job);
                    log.info("Job matched: {} at {} - Score: {}", 
                             job.getTitle(), job.getCompany(), score);
                } else {
                    job.setStatus("REJECTED");
                    log.debug("Job rejected (low score): {} - Score: {}", job.getTitle(), score);
                }
                
                jobRepository.save(job);
            } catch (Exception e) {
                log.error("Error matching job: {}", job.getTitle(), e);
            }
        }
        
        log.info("Matching complete. Matched jobs: {}", matchedJobs.size());
        return matchedJobs;
    }
    
    /**
     * Get all jobs
     */
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }
    
    /**
     * Get jobs by status
     */
    public List<Job> getJobsByStatus(String status) {
        return jobRepository.findByStatus(status);
    }
    
    /**
     * Get matched jobs (sorted by score)
     */
    public List<Job> getMatchedJobs() {
        return jobRepository.findByMinMatchScore(0.0);
    }
    
    /**
     * Get Easy Apply jobs that are matched
     */
    public List<Job> getEasyApplyMatchedJobs() {
        return jobRepository.findEasyApplyJobsByStatus("MATCHED");
    }
    
    /**
     * Get recent jobs
     */
    public List<Job> getRecentJobs(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return jobRepository.findRecentJobs(since);
    }
    
    /**
     * Get job by ID
     */
    public Optional<Job> getJobById(Long id) {
        return jobRepository.findById(id);
    }
    
    /**
     * Update job status
     */
    @Transactional
    public Job updateJobStatus(Long jobId, String status) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));
        job.setStatus(status);
        return jobRepository.save(job);
    }
    
    /**
     * Manually save a single job
     */
    @Transactional
    public Job saveJob(Job job) {
        // Check for duplicates by URL
        if (job.getJobUrl() != null && !job.getJobUrl().isEmpty()) {
            if (jobRepository.existsByJobUrl(job.getJobUrl())) {
                log.warn("Job already exists with URL: {}", job.getJobUrl());
                throw new RuntimeException("Job with this URL already exists");
            }
        }
        
        Job saved = jobRepository.save(job);
        log.info("Manually saved job: {} at {}", saved.getTitle(), saved.getCompany());
        return saved;
    }
    
    /**
     * Get statistics
     */
    public JobStats getStats(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        
        Long total = jobRepository.countByStatusSince("NEW", since) +
                     jobRepository.countByStatusSince("MATCHED", since) +
                     jobRepository.countByStatusSince("APPLIED", since);
        
        Long matched = jobRepository.countByStatusSince("MATCHED", since);
        Long applied = jobRepository.countByStatusSince("APPLIED", since);
        
        return new JobStats(total, matched, applied);
    }
    
    public record JobStats(Long total, Long matched, Long applied) {}
}
