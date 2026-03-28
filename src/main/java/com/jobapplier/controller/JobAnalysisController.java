package com.jobapplier.controller;

import com.jobapplier.dto.JobAnalysisResponse;
import com.jobapplier.service.JobAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobAnalysisController {
    
    private final JobAnalysisService jobAnalysisService;
    
    /**
     * Analyze a job URL instantly
     * 
     * GET /api/jobs/analyze?url=https://linkedin.com/jobs/view/123456&save=true
     * 
     * Returns:
     * - Job details (title, company, location)
     * - Match score and recommendations
     * - Skills analysis (matching, missing)
     * - AI-generated cover letter
     * - Why you're a good fit
     */
    @GetMapping("/analyze")
    public ResponseEntity<?> analyzeJob(
            @RequestParam String url,
            @RequestParam(defaultValue = "true") boolean save) {
        
        try {
            log.info("Instant job analysis requested for URL: {}", url);
            
            if (url == null || url.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Job URL is required",
                    "usage", "GET /api/jobs/analyze?url=YOUR_JOB_URL&save=true"
                ));
            }
            
            JobAnalysisResponse analysis = jobAnalysisService.analyzeJobUrl(url, save);
            
            if (analysis.getError() != null) {
                return ResponseEntity.badRequest().body(analysis);
            }
            
            return ResponseEntity.ok(analysis);
            
        } catch (Exception e) {
            log.error("Error analyzing job", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to analyze job: " + e.getMessage(),
                "details", e.getClass().getSimpleName()
            ));
        }
    }
    
    /**
     * Quick info about the analyze endpoint
     */
    @GetMapping("/analyze/help")
    public ResponseEntity<?> help() {
        return ResponseEntity.ok(Map.of(
            "endpoint", "/api/jobs/analyze",
            "method", "GET",
            "parameters", Map.of(
                "url", "Job URL (LinkedIn, Naukri, or any job posting)",
                "save", "Auto-save if good match (default: true)"
            ),
            "example", "GET /api/jobs/analyze?url=https://www.linkedin.com/jobs/view/123456&save=true",
            "returns", Map.of(
                "matchScore", "0-100% match score",
                "recommendation", "Apply immediately / Consider / Skip",
                "matchingSkills", "Skills you have",
                "missingSkills", "Skills you're missing",
                "coverLetter", "AI-generated personalized cover letter",
                "whyGoodFit", "AI explanation of why you're qualified",
                "saved", "Whether job was saved to database"
            ),
            "powershell", "Invoke-RestMethod -Uri 'http://localhost:8080/api/jobs/analyze?url=YOUR_URL' -Method Get | ConvertTo-Json -Depth 5"
        ));
    }
}
