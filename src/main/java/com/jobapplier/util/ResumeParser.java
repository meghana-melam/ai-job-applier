package com.jobapplier.util;

import com.jobapplier.model.Resume;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.Loader;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ResumeParser {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(\\+?\\d{1,3}[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}");
    private static final Pattern EXPERIENCE_PATTERN = Pattern.compile("(\\d+)\\s*\\+?\\s*years?", Pattern.CASE_INSENSITIVE);
    
    // Common technical skills
    private static final List<String> SKILL_KEYWORDS = Arrays.asList(
        "Java", "Python", "JavaScript", "TypeScript", "C++", "C#", "Go", "Rust",
        "Spring", "Spring Boot", "Hibernate", "React", "Angular", "Vue", "Node.js",
        "Docker", "Kubernetes", "AWS", "Azure", "GCP", "Jenkins", "Git",
        "MySQL", "PostgreSQL", "MongoDB", "Redis", "Elasticsearch",
        "REST", "API", "Microservices", "GraphQL", "gRPC",
        "HTML", "CSS", "Bootstrap", "Tailwind",
        "JUnit", "Mockito", "Selenium", "CI/CD"
    );
    
    /**
     * Parse PDF resume and extract information
     */
    public Resume parseResume(String filePath) throws IOException {
        Resume resume = new Resume();
        resume.setFilePath(filePath);
        
        File file = new File(filePath);
        if (!file.exists()) {
            log.warn("Resume file not found: {}", filePath);
            return resume;
        }
        
        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            // Extract various fields
            resume.setEmail(extractEmail(text));
            resume.setPhone(extractPhone(text));
            resume.setSkills(extractSkills(text));
            resume.setYearsOfExperience(extractYearsOfExperience(text));
            resume.setEducation(extractEducation(text));
            resume.setSummary(extractSummary(text));
            
            // Extract name (usually first line)
            String[] lines = text.split("\n");
            if (lines.length > 0) {
                resume.setName(lines[0].trim());
            }
            
            log.info("Successfully parsed resume: {}", filePath);
        } catch (IOException e) {
            log.error("Error parsing resume: {}", filePath, e);
            throw e;
        }
        
        return resume;
    }
    
    /**
     * Extract email from resume text
     */
    private String extractEmail(String text) {
        Matcher matcher = EMAIL_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
    
    /**
     * Extract phone from resume text
     */
    private String extractPhone(String text) {
        Matcher matcher = PHONE_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
    
    /**
     * Extract skills from resume text
     */
    private List<String> extractSkills(String text) {
        List<String> foundSkills = new ArrayList<>();
        
        for (String skill : SKILL_KEYWORDS) {
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(skill) + "\\b", Pattern.CASE_INSENSITIVE);
            if (pattern.matcher(text).find()) {
                foundSkills.add(skill);
            }
        }
        
        return foundSkills.stream().distinct().collect(Collectors.toList());
    }
    
    /**
     * Extract years of experience
     */
    private Integer extractYearsOfExperience(String text) {
        Matcher matcher = EXPERIENCE_PATTERN.matcher(text);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                log.warn("Could not parse experience: {}", matcher.group(1));
            }
        }
        return 0;
    }
    
    /**
     * Extract education information
     */
    private List<String> extractEducation(String text) {
        List<String> education = new ArrayList<>();
        
        String[] degrees = {"Bachelor", "Master", "PhD", "B.Tech", "M.Tech", "B.E", "M.E", "MBA", "BCA", "MCA"};
        
        for (String degree : degrees) {
            Pattern pattern = Pattern.compile(degree + "[^\\n]*", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                education.add(matcher.group().trim());
            }
        }
        
        return education.stream().distinct().collect(Collectors.toList());
    }
    
    /**
     * Extract summary/objective
     */
    private String extractSummary(String text) {
        String[] lines = text.split("\n");
        StringBuilder summary = new StringBuilder();
        boolean inSummary = false;
        
        for (String line : lines) {
            String lower = line.toLowerCase().trim();
            
            if (lower.contains("summary") || lower.contains("objective") || lower.contains("profile")) {
                inSummary = true;
                continue;
            }
            
            if (inSummary && (lower.contains("experience") || lower.contains("education") || lower.contains("skills"))) {
                break;
            }
            
            if (inSummary && !line.trim().isEmpty()) {
                summary.append(line.trim()).append(" ");
                if (summary.length() > 500) {
                    break;
                }
            }
        }
        
        return summary.toString().trim();
    }
}
