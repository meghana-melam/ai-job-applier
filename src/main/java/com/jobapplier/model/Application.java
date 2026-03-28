package com.jobapplier.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Application {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;
    
    @ManyToOne
    @JoinColumn(name = "resume_id")
    private Resume resume;
    
    @Column(columnDefinition = "TEXT")
    private String coverLetter;
    
    private String status; // PENDING, SUCCESS, FAILED, REJECTED
    
    @Column(name = "applied_date")
    private LocalDateTime appliedDate;
    
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "response_received")
    private Boolean responseReceived;
    
    @Column(name = "response_date")
    private LocalDateTime responseDate;
    
    @Column(columnDefinition = "TEXT")
    private String responseDetails;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "PENDING";
        }
        if (responseReceived == null) {
            responseReceived = false;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
