package com.jobapplier.dto;

import lombok.Data;

import java.util.List;

@Data
public class ManualJobRequest {
    private String title;
    private String company;
    private String location;
    private String description;
    private String jobUrl;
    private List<String> skills;
    private String experienceLevel;
    private String sourcePlatform; // LINKEDIN or NAUKRI
    private Boolean isEasyApply;
    
    // Optional fields
    private String salaryRange;
    private String employmentType; // Full-time, Part-time, Contract, etc.
}
