package com.jobapplier.ai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jobapplier.model.Job;
import com.jobapplier.model.Resume;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class ResumeMatcher {
    
    @Value("${gemini.api.key}")
    private String geminiApiKey;
    
    @Value("${gemini.model:gemini-pro}")
    private String model;
    
    @Value("${matching.min-score:60}")
    private double minMatchScore;
    
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    
    /**
     * Calculate match score between job and resume
     */
    public double calculateMatchScore(Job job, Resume resume) {
        try {
            log.info("Calculating match score for job: {} at {}", job.getTitle(), job.getCompany());
            
            // Rule-based scoring (60% weight)
            double ruleBasedScore = calculateRuleBasedScore(job, resume);
            
            // AI-based scoring (40% weight)
            double aiScore = calculateAIScore(job, resume);
            
            double finalScore = (ruleBasedScore * 0.6) + (aiScore * 0.4);
            
            log.info("Match score - Rule-based: {}, AI-based: {}, Final: {}", 
                     ruleBasedScore, aiScore, finalScore);
            
            return Math.round(finalScore * 100.0) / 100.0;
        } catch (Exception e) {
            log.error("Error calculating match score", e);
            return 0.0;
        }
    }
    
    /**
     * Rule-based scoring using skill matching and experience
     */
    private double calculateRuleBasedScore(Job job, Resume resume) {
        double score = 0.0;
        
        // Skills matching (70 points)
        if (job.getRequiredSkills() != null && !job.getRequiredSkills().isEmpty() &&
            resume.getSkills() != null && !resume.getSkills().isEmpty()) {
            
            Set<String> jobSkills = new HashSet<>();
            for (String skill : job.getRequiredSkills()) {
                jobSkills.add(skill.toLowerCase());
            }
            
            Set<String> resumeSkills = new HashSet<>();
            for (String skill : resume.getSkills()) {
                resumeSkills.add(skill.toLowerCase());
            }
            
            Set<String> matchedSkills = new HashSet<>(resumeSkills);
            matchedSkills.retainAll(jobSkills);
            
            double skillMatchPercentage = (double) matchedSkills.size() / jobSkills.size();
            score += skillMatchPercentage * 70;
            
            log.debug("Skills matched: {}/{}, Score: {}", matchedSkills.size(), jobSkills.size(), skillMatchPercentage * 70);
        }
        
        // Experience matching (30 points)
        if (job.getExperienceLevel() != null && resume.getYearsOfExperience() != null) {
            String expLevel = job.getExperienceLevel().toLowerCase();
            int resumeYears = resume.getYearsOfExperience();
            
            if (expLevel.contains("entry") || expLevel.contains("0-2")) {
                score += (resumeYears >= 0 && resumeYears <= 3) ? 30 : 15;
            } else if (expLevel.contains("mid") || expLevel.contains("2-5")) {
                score += (resumeYears >= 2 && resumeYears <= 6) ? 30 : 15;
            } else if (expLevel.contains("senior") || expLevel.contains("5+")) {
                score += (resumeYears >= 5) ? 30 : 15;
            } else {
                score += 20; // Default if can't determine
            }
        }
        
        return Math.min(score, 100.0);
    }
    
    /**
     * AI-based scoring using Google Gemini
     */
    private double calculateAIScore(Job job, Resume resume) {
        try {
            String prompt = buildMatchingPrompt(job, resume);
            String response = callGeminiAPI(prompt);
            
            // Parse score from response
            return parseScoreFromResponse(response);
        } catch (Exception e) {
            log.error("Error in AI scoring", e);
            return 50.0; // Default fallback score
        }
    }
    
    /**
     * Call Google Gemini API
     */
    private String callGeminiAPI(String prompt) throws IOException {
        String url = String.format(
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
            model, geminiApiKey
        );
        
        JsonObject requestBody = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();
        
        part.addProperty("text", prompt);
        parts.add(part);
        content.add("parts", parts);
        contents.add(content);
        requestBody.add("contents", contents);
        
        RequestBody body = RequestBody.create(gson.toJson(requestBody), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Gemini API call failed: " + response);
            }
            
            String responseBody = response.body().string();
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
            
            return jsonResponse
                    .getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();
        }
    }
    
    /**
     * Build prompt for AI matching
     */
    private String buildMatchingPrompt(Job job, Resume resume) {
        try {
            String promptTemplate = loadPromptTemplate("src/main/resources/prompts/job_match_prompt.txt");
            
            return promptTemplate
                    .replace("{job_title}", job.getTitle())
                    .replace("{company}", job.getCompany())
                    .replace("{job_description}", job.getDescription() != null ? job.getDescription() : "")
                    .replace("{required_skills}", String.join(", ", job.getRequiredSkills() != null ? job.getRequiredSkills() : List.of()))
                    .replace("{resume_skills}", String.join(", ", resume.getSkills() != null ? resume.getSkills() : List.of()))
                    .replace("{resume_experience}", String.valueOf(resume.getYearsOfExperience()))
                    .replace("{resume_summary}", resume.getSummary() != null ? resume.getSummary() : "");
        } catch (Exception e) {
            log.warn("Error building prompt, using default", e);
            return String.format("Rate the match between this job and resume on a scale of 0-100:\n\nJob: %s at %s\nResume Skills: %s\nExperience: %d years",
                    job.getTitle(), job.getCompany(), 
                    String.join(", ", resume.getSkills() != null ? resume.getSkills() : List.of()),
                    resume.getYearsOfExperience());
        }
    }
    
    /**
     * Parse score from AI response
     */
    private double parseScoreFromResponse(String response) {
        try {
            // Look for number patterns
            String cleaned = response.replaceAll("[^0-9.]", "");
            double score = Double.parseDouble(cleaned);
            return Math.min(Math.max(score, 0), 100);
        } catch (Exception e) {
            log.warn("Could not parse score from response: {}", response);
            return 50.0;
        }
    }
    
    /**
     * Load prompt template from file
     */
    private String loadPromptTemplate(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }
    
    /**
     * Check if job meets minimum match criteria
     */
    public boolean meetsMinimumCriteria(double matchScore) {
        return matchScore >= minMatchScore;
    }
}
