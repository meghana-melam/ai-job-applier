package com.jobapplier.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Job {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false)
    private String company;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private String location;
    
    @Column(name = "job_url", unique = true)
    private String jobUrl;
    
    @Column(name = "source_platform")
    private String sourcePlatform; // LINKEDIN, NAUKRI
    
    @ElementCollection
    @CollectionTable(name = "job_skills", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "skill")
    private List<String> requiredSkills;
    
    private String experienceLevel;
    
    private String employmentType; // FULL_TIME, PART_TIME, CONTRACT
    
    private Double matchScore;
    
    @Column(name = "is_easy_apply")
    private Boolean isEasyApply;
    
    private String status; // NEW, MATCHED, APPLIED, REJECTED
    
    @Column(name = "posted_date")
    private LocalDateTime postedDate;
    
    @Column(name = "scraped_date")
    private LocalDateTime scrapedDate;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "NEW";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
