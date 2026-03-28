package com.jobapplier.util;

import com.jobapplier.model.Job;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JobParser {
    
    private static final Pattern EXPERIENCE_PATTERN = Pattern.compile("(\\d+)\\s*[-+]?\\s*(\\d*)\\s*years?", Pattern.CASE_INSENSITIVE);
    private static final Pattern SKILLS_PATTERN = Pattern.compile("(Java|Python|JavaScript|React|Angular|Spring|Node\\.js|Docker|Kubernetes|AWS|Azure|GCP|SQL|MongoDB|REST|API|Microservices)", Pattern.CASE_INSENSITIVE);
    
    /**
     * Parse job description to extract structured information
     */
    public void enrichJobData(Job job) {
        if (job.getDescription() == null) {
            return;
        }
        
        String description = job.getDescription();
        
        // Extract experience level if not already set
        if (job.getExperienceLevel() == null) {
            job.setExperienceLevel(extractExperienceLevel(description));
        }
        
        // Extract skills if not already set
        if (job.getRequiredSkills() == null || job.getRequiredSkills().isEmpty()) {
            job.setRequiredSkills(extractSkills(description));
        }
    }
    
    /**
     * Extract years of experience from job description
     */
    public String extractExperienceLevel(String description) {
        Matcher matcher = EXPERIENCE_PATTERN.matcher(description);
        if (matcher.find()) {
            String minYears = matcher.group(1);
            String maxYears = matcher.group(2);
            
            if (maxYears != null && !maxYears.isEmpty()) {
                return minYears + "-" + maxYears + " years";
            } else {
                return minYears + "+ years";
            }
        }
        return "Not specified";
    }
    
    /**
     * Extract technical skills from job description
     */
    public List<String> extractSkills(String description) {
        Matcher matcher = SKILLS_PATTERN.matcher(description);
        return matcher.results()
                .map(m -> m.group(1))
                .distinct()
                .collect(Collectors.toList());
    }
    
    /**
     * Parse relative date strings (e.g., "2 days ago")
     */
    public LocalDateTime parseRelativeDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return LocalDateTime.now();
        }
        
        dateStr = dateStr.toLowerCase().trim();
        LocalDateTime now = LocalDateTime.now();
        
        // Handle "just now" or "today"
        if (dateStr.contains("just now") || dateStr.equals("today")) {
            return now;
        }
        
        // Handle "X hours ago"
        Pattern hoursPattern = Pattern.compile("(\\d+)\\s*hours?\\s*ago");
        Matcher hoursMatcher = hoursPattern.matcher(dateStr);
        if (hoursMatcher.find()) {
            int hours = Integer.parseInt(hoursMatcher.group(1));
            return now.minusHours(hours);
        }
        
        // Handle "X days ago"
        Pattern daysPattern = Pattern.compile("(\\d+)\\s*days?\\s*ago");
        Matcher daysMatcher = daysPattern.matcher(dateStr);
        if (daysMatcher.find()) {
            int days = Integer.parseInt(daysMatcher.group(1));
            return now.minusDays(days);
        }
        
        // Handle "X weeks ago"
        Pattern weeksPattern = Pattern.compile("(\\d+)\\s*weeks?\\s*ago");
        Matcher weeksMatcher = weeksPattern.matcher(dateStr);
        if (weeksMatcher.find()) {
            int weeks = Integer.parseInt(weeksMatcher.group(1));
            return now.minusWeeks(weeks);
        }
        
        // Default to now if can't parse
        return now;
    }
    
    /**
     * Clean and normalize job title
     */
    public String normalizeJobTitle(String title) {
        if (title == null) {
            return "";
        }
        return title.trim().replaceAll("\\s+", " ");
    }
    
    /**
     * Determine if job is remote/hybrid/onsite
     */
    public String determineWorkMode(String description, String location) {
        String combined = (description + " " + location).toLowerCase();
        
        if (combined.contains("remote") || combined.contains("work from home")) {
            return "REMOTE";
        } else if (combined.contains("hybrid")) {
            return "HYBRID";
        } else {
            return "ONSITE";
        }
    }
}
