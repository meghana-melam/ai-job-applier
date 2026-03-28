package com.jobapplier.controller;

import com.jobapplier.model.Application;
import com.jobapplier.model.Job;
import com.jobapplier.repository.ApplicationRepository;
import com.jobapplier.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class TrackerUIController {
    
    private final ApplicationRepository applicationRepository;
    
    /**
     * Serve the Application Tracker UI
     */
    @GetMapping("/")
    public String index() {
        return "forward:/tracker.html";
    }
    
    @GetMapping("/tracker")
    public String tracker() {
        return "forward:/tracker.html";
    }
    
    /**
     * API endpoint for tracker data
     */
    @GetMapping("/api/tracker/applications")
    @ResponseBody
    public List<Map<String, Object>> getApplications() {
        List<Application> applications = applicationRepository.findAll();
        
        return applications.stream()
            .map(app -> {
                Map<String, Object> data = new HashMap<>();
                
                Job job = app.getJob();
                
                if (job != null) {
                    data.put("id", app.getId());
                    data.put("title", job.getTitle());
                    data.put("company", job.getCompany());
                    data.put("location", job.getLocation());
                    data.put("jobUrl", job.getJobUrl());
                    data.put("matchScore", job.getMatchScore() != null ? Math.round(job.getMatchScore()) : 0);
                    data.put("status", app.getStatus());
                    data.put("appliedAt", app.getAppliedDate() != null ? app.getAppliedDate().toString() : null);
                    data.put("createdAt", app.getCreatedAt().toString());
                    data.put("hasCoverLetter", app.getCoverLetter() != null && !app.getCoverLetter().isEmpty());
                }
                
                return data;
            })
            .filter(data -> !data.isEmpty())
            .sorted((a, b) -> {
                // Sort by match score descending
                Integer scoreA = (Integer) a.get("matchScore");
                Integer scoreB = (Integer) b.get("matchScore");
                return scoreB.compareTo(scoreA);
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Get dashboard statistics
     */
    @GetMapping("/api/tracker/stats")
    @ResponseBody
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long total = applicationRepository.count();
        long applied = applicationRepository.countByStatus("APPLIED");
        long pending = applicationRepository.countByStatus("PENDING_MANUAL");
        long rejected = applicationRepository.countByStatus("REJECTED");
        
        List<Application> allApps = applicationRepository.findAll();
        OptionalDouble avgScore = allApps.stream()
            .filter(app -> app.getJob() != null && app.getJob().getMatchScore() != null)
            .mapToDouble(app -> app.getJob().getMatchScore())
            .average();
        
        stats.put("total", total);
        stats.put("applied", applied);
        stats.put("pending", pending);
        stats.put("rejected", rejected);
        stats.put("averageScore", avgScore.isPresent() ? Math.round(avgScore.getAsDouble()) : 0);
        
        return stats;
    }
    
    /**
     * Get cover letter for application
     */
    @GetMapping("/api/tracker/cover-letter/{id}")
    @ResponseBody
    public Map<String, String> getCoverLetter(@PathVariable Long id) {
        Map<String, String> response = new HashMap<>();
        
        Application app = applicationRepository.findById(id).orElse(null);
        if (app != null && app.getCoverLetter() != null) {
            response.put("coverLetter", app.getCoverLetter());
        } else {
            response.put("error", "Cover letter not found");
        }
        
        return response;
    }
    
    /**
     * Update application status
     */
    @PutMapping("/api/tracker/status/{id}")
    @ResponseBody
    public Map<String, String> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Map<String, String> response = new HashMap<>();
        
        Application app = applicationRepository.findById(id).orElse(null);
        if (app != null) {
            String newStatus = request.get("status");
            app.setStatus(newStatus);
            
            if ("APPLIED".equals(newStatus) && app.getAppliedDate() == null) {
                app.setAppliedDate(java.time.LocalDateTime.now());
            }
            
            applicationRepository.save(app);
            response.put("message", "Status updated successfully");
            response.put("status", newStatus);
        } else {
            response.put("error", "Application not found");
        }
        
        return response;
    }
}
