package com.jobapplier.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobAnalysisResponse {
    private String title;
    private String company;
    private String location;
    private String jobUrl;
    private String description;
    
    // Match analysis
    private Double matchScore;
    private String matchLevel; // "Excellent", "Good", "Fair", "Poor"
    private String recommendation; // "Strongly Recommended", "Apply", "Consider", "Skip"
    
    // Skills analysis
    private List<String> requiredSkills;
    private List<String> matchingSkills;
    private List<String> missingSkills;
    private List<String> yourStrengths;
    
    // AI insights
    private String whyGoodFit;
    private String concernsToAddress;
    private String coverLetter;
    
    // Metadata
    private boolean saved;
    private Long jobId;
    private String error;
}
