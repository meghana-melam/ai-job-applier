package com.jobapplier.controller;

import com.jobapplier.model.Application;
import com.jobapplier.service.ApplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {
    
    private final ApplyService applyService;
    
    /**
     * Apply to jobs automatically
     */
    @PostMapping({"/auto-apply", "/apply"})
    public ResponseEntity<List<Application>> autoApply(@RequestParam(defaultValue = "10") int maxApplications) {
        List<Application> applications = applyService.applyToJobs(maxApplications);
        return ResponseEntity.ok(applications);
    }
    
    /**
     * Get all applications
     */
    @GetMapping
    public ResponseEntity<List<Application>> getAllApplications() {
        List<Application> applications = applyService.getAllApplications();
        return ResponseEntity.ok(applications);
    }
    
    /**
     * Get applications by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Application>> getApplicationsByStatus(@PathVariable String status) {
        List<Application> applications = applyService.getApplicationsByStatus(status);
        return ResponseEntity.ok(applications);
    }
    
    /**
     * Get recent applications
     */
    @GetMapping("/recent")
    public ResponseEntity<List<Application>> getRecentApplications(@RequestParam(defaultValue = "7") int days) {
        List<Application> applications = applyService.getRecentApplications(days);
        return ResponseEntity.ok(applications);
    }
    
    /**
     * Get application statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<ApplyService.ApplicationStats> getStats(@RequestParam(defaultValue = "7") int days) {
        ApplyService.ApplicationStats stats = applyService.getStats(days);
        return ResponseEntity.ok(stats);
    }
}
