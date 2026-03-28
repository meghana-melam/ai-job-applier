package com.jobapplier.service;

import com.jobapplier.ai.CoverLetterGenerator;
import com.jobapplier.ai.ResumeMatcher;
import com.jobapplier.dto.JobAnalysisResponse;
import com.jobapplier.model.Job;
import com.jobapplier.model.Resume;
import com.jobapplier.repository.JobRepository;
import com.jobapplier.repository.ResumeRepository;
import com.jobapplier.util.JobParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobAnalysisService {
    
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;
    private final JobParser jobParser;
    private final ResumeMatcher resumeMatcher;
    private final CoverLetterGenerator coverLetterGenerator;
    
    /**
     * Analyze a job URL and return comprehensive analysis
     */
    public JobAnalysisResponse analyzeJobUrl(String jobUrl, boolean autoSave) {
        JobAnalysisResponse response = new JobAnalysisResponse();
        response.setJobUrl(jobUrl);
        
        try {
            log.info("Analyzing job URL: {}", jobUrl);
            
            // 1. Get active resume
            Resume resume = resumeRepository.findFirstByIsActiveTrueOrderByCreatedAtDesc()
                .orElseThrow(() -> new RuntimeException("No active resume found. Please upload a resume first."));
            
            // 2. Fetch and parse job page
            Job job = fetchJobDetails(jobUrl);
            
            // 3. Enrich with AI parsing
            jobParser.enrichJobData(job);
            
            // 4. Match with resume
            Double matchScore = resumeMatcher.calculateMatchScore(job, resume);
            job.setMatchScore(matchScore);
            
            // 5. Populate response
            response.setTitle(job.getTitle());
            response.setCompany(job.getCompany());
            response.setLocation(job.getLocation());
            response.setDescription(job.getDescription());
            response.setMatchScore(matchScore);
            
            // 6. Set match level and recommendation
            setMatchLevelAndRecommendation(response, matchScore);
            
            // 7. Skills analysis
            analyzeSkills(response, job, resume);
            
            // 8. Generate insights
            generateInsights(response, job, resume, matchScore);
            
            // 9. Generate cover letter if good match
            if (matchScore >= 60) {
                String coverLetter = coverLetterGenerator.generateCoverLetter(job, resume);
                response.setCoverLetter(coverLetter);
            } else {
                response.setCoverLetter("Match score too low. Cover letter not generated.");
            }
            
            // 10. Auto-save if requested and good match
            if (autoSave && matchScore >= 60) {
                if (jobRepository.findByJobUrl(jobUrl).isEmpty()) {
                    job.setScrapedDate(LocalDateTime.now());
                    job.setPostedDate(LocalDateTime.now());
                    job.setStatus("PENDING");
                    Job savedJob = jobRepository.save(job);
                    response.setSaved(true);
                    response.setJobId(savedJob.getId());
                    log.info("Job saved to database with ID: {}", savedJob.getId());
                } else {
                    Job existingJob = jobRepository.findByJobUrl(jobUrl).get();
                    response.setSaved(true);
                    response.setJobId(existingJob.getId());
                }
            }
            
            log.info("Analysis complete: {} at {} - {}% match", 
                job.getTitle(), job.getCompany(), matchScore.intValue());
            
        } catch (Exception e) {
            log.error("Error analyzing job URL: {}", e.getMessage(), e);
            response.setError("Failed to analyze job: " + e.getMessage());
        }
        
        return response;
    }
    
    private Job fetchJobDetails(String jobUrl) throws Exception {
        Job job = new Job();
        job.setJobUrl(jobUrl);
        
        if (jobUrl.contains("linkedin.com")) {
            job.setSourcePlatform("LINKEDIN");
            fetchLinkedInJob(job, jobUrl);
        } else if (jobUrl.contains("naukri.com")) {
            job.setSourcePlatform("NAUKRI");
            fetchNaukriJob(job, jobUrl);
        } else {
            job.setSourcePlatform("OTHER");
            fetchGenericJob(job, jobUrl);
        }
        
        return job;
    }
    
    private void fetchLinkedInJob(Job job, String url) throws Exception {
        Document doc = Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            .timeout(10000)
            .get();
        
        try {
            String title = doc.select("h1.top-card-layout__title, h1.topcard__title").first() != null 
                ? doc.select("h1.top-card-layout__title, h1.topcard__title").first().text() 
                : "Unknown Title";
            job.setTitle(jobParser.normalizeJobTitle(title));
            
            String company = doc.select("a.topcard__org-name-link").first() != null
                ? doc.select("a.topcard__org-name-link").first().text()
                : "Unknown Company";
            job.setCompany(company.trim());
            
            String location = doc.select("span.topcard__flavor--bullet").first() != null
                ? doc.select("span.topcard__flavor--bullet").first().text()
                : "Remote";
            job.setLocation(location.trim());
            
            String description = doc.select("div.show-more-less-html__markup").first() != null
                ? doc.select("div.show-more-less-html__markup").first().text()
                : "Description not available";
            job.setDescription(description);
            
            job.setIsEasyApply(doc.select("button[data-job-id]").size() > 0);
            
        } catch (Exception e) {
            log.warn("Error parsing LinkedIn job details: {}", e.getMessage());
            job.setTitle("Java Developer");
            job.setCompany("Company from LinkedIn");
            job.setLocation("Remote");
            job.setDescription("Job description could not be fetched.");
        }
    }
    
    private void fetchNaukriJob(Job job, String url) throws Exception {
        Document doc = Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            .timeout(10000)
            .get();
        
        try {
            String title = doc.select("h1").first() != null
                ? doc.select("h1").first().text()
                : "Unknown Title";
            job.setTitle(jobParser.normalizeJobTitle(title));
            
            String company = doc.select("a.comp-name").first() != null
                ? doc.select("a.comp-name").first().text()
                : "Unknown Company";
            job.setCompany(company.trim());
            
            job.setLocation("India");
            job.setDescription(doc.body().text());
            job.setIsEasyApply(false);
            
        } catch (Exception e) {
            log.warn("Error parsing Naukri job details: {}", e.getMessage());
            job.setTitle("Java Developer");
            job.setCompany("Company from Naukri");
            job.setLocation("India");
            job.setDescription("Job description could not be fetched.");
        }
    }
    
    private void fetchGenericJob(Job job, String url) throws Exception {
        Document doc = Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            .timeout(10000)
            .get();
        
        job.setTitle(doc.title());
        job.setCompany("Unknown");
        job.setLocation("Remote");
        job.setDescription(doc.body().text());
        job.setIsEasyApply(false);
    }
    
    private void setMatchLevelAndRecommendation(JobAnalysisResponse response, Double matchScore) {
        if (matchScore >= 85) {
            response.setMatchLevel("Excellent Match");
            response.setRecommendation("⭐ STRONGLY RECOMMENDED - Apply immediately!");
        } else if (matchScore >= 70) {
            response.setMatchLevel("Good Match");
            response.setRecommendation("✅ RECOMMENDED - Strong candidate, apply soon");
        } else if (matchScore >= 60) {
            response.setMatchLevel("Fair Match");
            response.setRecommendation("👍 CONSIDER - Decent fit, worth applying");
        } else if (matchScore >= 40) {
            response.setMatchLevel("Weak Match");
            response.setRecommendation("⚠️ MAYBE - Consider if desperate, but not ideal");
        } else {
            response.setMatchLevel("Poor Match");
            response.setRecommendation("❌ SKIP - Not a good fit, save your time");
        }
    }
    
    private void analyzeSkills(JobAnalysisResponse response, Job job, Resume resume) {
        List<String> requiredSkills = jobParser.extractSkills(job.getDescription());
        
        final List<String> resumeSkills = (resume.getSkills() != null && !resume.getSkills().isEmpty()) 
            ? new ArrayList<>(resume.getSkills())
            : new ArrayList<>();
        
        List<String> matchingSkills = requiredSkills.stream()
            .filter(skill -> resumeSkills.stream()
                .anyMatch(rs -> rs.equalsIgnoreCase(skill)))
            .collect(Collectors.toList());
        
        List<String> missingSkills = requiredSkills.stream()
            .filter(skill -> matchingSkills.stream()
                .noneMatch(ms -> ms.equalsIgnoreCase(skill)))
            .collect(Collectors.toList());
        
        response.setRequiredSkills(requiredSkills);
        response.setMatchingSkills(matchingSkills);
        response.setMissingSkills(missingSkills);
        response.setYourStrengths(resumeSkills.size() > 0 
            ? resumeSkills.subList(0, Math.min(5, resumeSkills.size()))
            : new ArrayList<>());
    }
    
    private void generateInsights(JobAnalysisResponse response, Job job, Resume resume, Double matchScore) {
        if (matchScore >= 70) {
            String topSkills = response.getMatchingSkills().isEmpty() 
                ? "your experience" 
                : String.join(", ", response.getMatchingSkills().subList(0, Math.min(3, response.getMatchingSkills().size())));
            
            String resumeTopSkill = "your background";
            if (resume.getSkills() != null && !resume.getSkills().isEmpty()) {
                resumeTopSkill = resume.getSkills().get(0);
            }
            
            response.setWhyGoodFit(
                String.format("You have %d%% of required skills. Your experience with %s aligns well with this role. " +
                    "Your background in %s makes you a strong candidate.",
                    matchScore.intValue(),
                    topSkills,
                    resumeTopSkill)
            );
        } else {
            response.setWhyGoodFit("Limited skill match. May need additional qualifications.");
        }
        
        if (!response.getMissingSkills().isEmpty()) {
            int skillCount = Math.min(3, response.getMissingSkills().size());
            String missingSkillsList = String.join(", ", 
                response.getMissingSkills().subList(0, skillCount));
            response.setConcernsToAddress(
                "Missing skills: " + missingSkillsList + ". Consider highlighting transferable skills or willingness to learn."
            );
        } else {
            response.setConcernsToAddress("No major concerns. You meet all key requirements!");
        }
    }
}
