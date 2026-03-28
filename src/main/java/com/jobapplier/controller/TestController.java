package com.jobapplier.controller;

import com.jobapplier.model.Job;
import com.jobapplier.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {
    
    private final JobRepository jobRepository;
    
    /**
     * Create sample test jobs for testing the complete workflow
     */
    @PostMapping("/create-sample-jobs")
    public ResponseEntity<?> createSampleJobs(@RequestParam(defaultValue = "5") int count) {
        try {
            List<Job> sampleJobs = new ArrayList<>();
            
            // Sample Job 1 - Java Developer
            Job job1 = new Job();
            job1.setTitle("Java Developer");
            job1.setCompany("Tech Corp");
            job1.setLocation("Bangalore");
            job1.setDescription("We are looking for an experienced Java developer. Required skills: Java, Spring Boot, MySQL, REST APIs, CI/CD, Docker.");
            job1.setRequiredSkills(List.of("Java", "Spring Boot", "MySQL", "REST API"));
            job1.setSourcePlatform("TEST");
            job1.setJobUrl("https://example.com/job1");
            job1.setScrapedDate(LocalDateTime.now());
            job1.setPostedDate(LocalDateTime.now().minusDays(2));
            job1.setIsEasyApply(true);
            sampleJobs.add(job1);
            
            // Sample Job 2 - Senior Java Developer
            Job job2 = new Job();
            job2.setTitle("Senior Java Developer");
            job2.setCompany("Innovate Solutions");
            job2.setLocation("Hyderabad");
            job2.setDescription("Senior position requiring expertise in Java, Spring Boot, Microservices, Azure cloud, and DevOps practices.");
            job2.setRequiredSkills(List.of("Java", "Spring Boot", "Microservices", "Azure", "Docker"));
            job2.setSourcePlatform("TEST");
            job2.setJobUrl("https://example.com/job2");
            job2.setScrapedDate(LocalDateTime.now());
            job2.setPostedDate(LocalDateTime.now().minusDays(1));
            job2.setIsEasyApply(true);
            sampleJobs.add(job2);
            
            // Sample Job 3 - Full Stack Java Developer
            Job job3 = new Job();
            job3.setTitle("Full Stack Java Developer");
            job3.setCompany("Digital Enterprises");
            job3.setLocation("Pune");
            job3.setDescription("Full stack role with Java Spring Boot backend and React frontend. Experience with CI/CD pipelines and cloud platforms required.");
            job3.setRequiredSkills(List.of("Java", "Spring Boot", "React", "CI/CD", "AWS"));
            job3.setSourcePlatform("TEST");
            job3.setJobUrl("https://example.com/job3");
            job3.setScrapedDate(LocalDateTime.now());
            job3.setPostedDate(LocalDateTime.now());
            job3.setIsEasyApply(false);
            sampleJobs.add(job3);
            
            // Sample Job 4 - Java Backend Engineer
            Job job4 = new Job();
            job4.setTitle("Java Backend Engineer");
            job4.setCompany("Cloud Systems");
            job4.setLocation("Mumbai");
            job4.setDescription("Backend engineer for microservices architecture. Skills: Java, Spring Boot, Kubernetes, Docker, MySQL.");
            job4.setRequiredSkills(List.of("Java", "Microservices", "Kubernetes", "Docker"));
            job4.setSourcePlatform("TEST");
            job4.setJobUrl("https://example.com/job4");
            job4.setScrapedDate(LocalDateTime.now());
            job4.setPostedDate(LocalDateTime.now().minusDays(3));
            job4.setIsEasyApply(true);
            sampleJobs.add(job4);
            
            // Sample Job 5 - Java Application Developer  
            Job job5 = new Job();
            job5.setTitle("Java Application Developer");
            job5.setCompany("Enterprise Solutions");
            job5.setLocation("Chennai");
            job5.setDescription("Develop and maintain enterprise applications using Java, Spring Framework, REST APIs, and modern DevOps tools.");
            job5.setRequiredSkills(List.of("Java", "Spring", "REST API", "Git", "Maven"));
            job5.setSourcePlatform("TEST");
            job5.setJobUrl("https://example.com/job5");
            job5.setScrapedDate(LocalDateTime.now());
            job5.setPostedDate(LocalDateTime.now().minusDays(4));
            job5.setIsEasyApply(true);
            sampleJobs.add(job5);
            
            // Save only requested count
            List<Job> jobsToSave = sampleJobs.subList(0, Math.min(count, sampleJobs.size()));
            List<Job> savedJobs = jobRepository.saveAll(jobsToSave);
            
            log.info("Created {} sample test jobs", savedJobs.size());
            
            return ResponseEntity.ok(Map.of(
                "message", "Sample jobs created successfully",
                "count", savedJobs.size(),
                "jobs", savedJobs
            ));
            
        } catch (Exception e) {
            log.error("Error creating sample jobs", e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Clear all test jobs
     */
    @DeleteMapping("/clear-jobs")
    public ResponseEntity<?> clearJobs() {
        try {
            List<Job> testJobs = jobRepository.findBySourcePlatform("TEST");
            jobRepository.deleteAll(testJobs);
            log.info("Deleted {} test jobs", testJobs.size());
            return ResponseEntity.ok(Map.of("message", "Test jobs cleared", "count", testJobs.size()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}
