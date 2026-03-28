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
import java.util.List;

@Slf4j
@Component
public class CoverLetterGenerator {
    
    @Value("${gemini.api.key}")
    private String geminiApiKey;
    
    @Value("${gemini.model:gemini-pro}")
    private String model;
    
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    
    /**
     * Generate customized cover letter for a job application
     */
    public String generateCoverLetter(Job job, Resume resume) {
        try {
            log.info("Generating cover letter for: {} at {}", job.getTitle(), job.getCompany());
            
            String prompt = buildCoverLetterPrompt(job, resume);
            String coverLetter = callGeminiAPI(prompt);
            
            log.info("Successfully generated cover letter");
            return coverLetter;
        } catch (Exception e) {
            log.error("Error generating cover letter", e);
            return generateFallbackCoverLetter(job, resume);
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
     * Build prompt for cover letter generation
     */
    private String buildCoverLetterPrompt(Job job, Resume resume) {
        try {
            String promptTemplate = loadPromptTemplate("src/main/resources/prompts/cover_letter_prompt.txt");
            
            return promptTemplate
                    .replace("{candidate_name}", resume.getName())
                    .replace("{job_title}", job.getTitle())
                    .replace("{company}", job.getCompany())
                    .replace("{job_description}", job.getDescription() != null ? job.getDescription() : "")
                    .replace("{required_skills}", String.join(", ", job.getRequiredSkills() != null ? job.getRequiredSkills() : List.of()))
                    .replace("{resume_skills}", String.join(", ", resume.getSkills() != null ? resume.getSkills() : List.of()))
                    .replace("{resume_experience}", String.valueOf(resume.getYearsOfExperience()))
                    .replace("{resume_summary}", resume.getSummary() != null ? resume.getSummary() : "");
        } catch (Exception e) {
            log.warn("Error building prompt, using default", e);
            return String.format(
                    "Write a professional cover letter for %s applying to the position of %s at %s. " +
                    "The candidate has %d years of experience with skills in: %s. " +
                    "Highlight relevant skills and express enthusiasm for the role. Keep it concise (200-250 words).",
                    resume.getName(), job.getTitle(), job.getCompany(),
                    resume.getYearsOfExperience(),
                    String.join(", ", resume.getSkills() != null ? resume.getSkills() : List.of())
            );
        }
    }
    
    /**
     * Load prompt template from file
     */
    private String loadPromptTemplate(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }
    
    /**
     * Generate fallback cover letter if AI fails
     */
    private String generateFallbackCoverLetter(Job job, Resume resume) {
        return String.format(
                "Dear Hiring Manager,\n\n" +
                "I am writing to express my strong interest in the %s position at %s. " +
                "With %d years of professional experience and expertise in %s, " +
                "I am confident in my ability to contribute effectively to your team.\n\n" +
                "My background aligns well with the requirements of this role, and I am particularly " +
                "excited about the opportunity to apply my skills in %s to drive meaningful results.\n\n" +
                "I would welcome the opportunity to discuss how my experience and skills can benefit %s. " +
                "Thank you for considering my application.\n\n" +
                "Sincerely,\n%s",
                job.getTitle(),
                job.getCompany(),
                resume.getYearsOfExperience(),
                String.join(", ", resume.getSkills() != null && !resume.getSkills().isEmpty() ? 
                           resume.getSkills().subList(0, Math.min(3, resume.getSkills().size())) : 
                           List.of("various technologies")),
                job.getTitle(),
                job.getCompany(),
                resume.getName()
        );
    }
}
