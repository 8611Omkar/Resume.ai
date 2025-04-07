package com.example.resume.service;

import com.example.resume.model.Resume;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Service
public class ResumeService {
    private static final Logger logger = LoggerFactory.getLogger(ResumeService.class);

    @Value("${openai.api.key:mock-api-key}")
    private String apiKey;

    @Value("${openai.api.url:https://api.openai.com/v1}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateResume(Resume resume) {
        try {
            // Validate minimum required data
            if (resume == null) {
                throw new IllegalArgumentException("Resume data is required");
            }

            // If summary is provided, we can generate a resume
            if (resume.getSummary() != null && !resume.getSummary().isEmpty()) {
                logger.info("Generating resume from summary");
                String generatedResume = generateResumeFromSummary(resume);
                return generatedResume;
            }

            // Otherwise, validate required fields
            if (resume.getPersonalInformation() == null) {
                throw new IllegalArgumentException("Personal information is required");
            }
            
            if (resume.getPersonalInformation().getName() == null || resume.getPersonalInformation().getName().isEmpty()) {
                throw new IllegalArgumentException("Name is required");
            }
            
            if (resume.getPersonalInformation().getEmail() == null || resume.getPersonalInformation().getEmail().isEmpty()) {
                throw new IllegalArgumentException("Email is required");
            }

            // Validate API key
            if (apiKey == null || apiKey.isEmpty() || "mock-api-key".equals(apiKey)) {
                logger.warn("Invalid or missing OpenAI API key. Using mock implementation.");
                return generateMockResume(resume);
            }

            // Log API key status (masked for security)
            logger.info("Using OpenAI API with key: {}...{}", 
                apiKey.substring(0, 4), 
                apiKey.substring(apiKey.length() - 4));

            String prompt = buildPrompt(resume);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-3.5-turbo");
            requestBody.put("messages", new Object[] {
                new HashMap<String, String>() {{
                    put("role", "user");
                    put("content", prompt);
                }}
            });
            requestBody.put("max_tokens", 2000);
            requestBody.put("temperature", 0.7);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            logger.info("Sending request to OpenAI API at: {}", apiUrl);
            ResponseEntity<String> response = restTemplate.postForEntity(
                apiUrl + "/chat/completions",
                request,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Successfully generated resume");
                return response.getBody();
            } else {
                logger.error("OpenAI API returned error: {}", response.getBody());
                throw new RuntimeException("Failed to generate resume: " + response.getBody());
            }
        } catch (IllegalArgumentException e) {
            logger.error("Validation error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error generating resume: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating resume: " + e.getMessage());
        }
    }

    private Resume.PersonalInformation extractPersonalInfo(String summary) {
        Resume.PersonalInformation info = new Resume.PersonalInformation();
        
        // Extract name (look for "I'm [Name]" or "My name is [Name]" or "I am [Name]")
        Pattern namePattern = Pattern.compile("I'm ([A-Za-z ]+)|My name is ([A-Za-z ]+)|I am ([A-Za-z ]+)");
        Matcher nameMatcher = namePattern.matcher(summary);
        if (nameMatcher.find()) {
            String name = nameMatcher.group(1) != null ? nameMatcher.group(1) : 
                         (nameMatcher.group(2) != null ? nameMatcher.group(2) : nameMatcher.group(3));
            info.setName(name.trim());
            logger.info("Extracted name from summary: {}", name);
        } else {
            // If no name found, use a default
            info.setName("Professional Candidate");
            logger.info("No name found in summary, using default name");
        }
        
        // Extract email (look for email pattern)
        Pattern emailPattern = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
        Matcher emailMatcher = emailPattern.matcher(summary);
        if (emailMatcher.find()) {
            info.setEmail(emailMatcher.group());
            logger.info("Extracted email from summary: {}", emailMatcher.group());
        } else {
            // If no email found, use a default
            info.setEmail("candidate@example.com");
            logger.info("No email found in summary, using default email");
        }
        
        return info;
    }

    private String generateResumeFromSummary(Resume resume) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a professional resume writer. Create a detailed, professional resume based on the following summary. ");
        prompt.append("Extract key information and create appropriate sections. ");
        prompt.append("Make reasonable assumptions where information is missing.\n\n");
        
        prompt.append("SUMMARY:\n").append(resume.getSummary()).append("\n\n");
        
        prompt.append("Please create a professional resume with the following sections:\n");
        prompt.append("1. Contact Information (at the top)\n");
        prompt.append("2. Professional Summary\n");
        prompt.append("3. Skills (as bullet points)\n");
        prompt.append("4. Work Experience (with dates, company names, and achievements)\n");
        prompt.append("5. Education\n");
        prompt.append("6. Achievements and Certifications\n\n");
        prompt.append("Format the resume professionally with proper spacing, bullet points, and section headers. ");
        prompt.append("Use action verbs and quantify achievements where possible. ");
        prompt.append("Make the resume compelling and highlight the most relevant information for the job market.");
        
        return prompt.toString();
    }

    private String buildPrompt(Resume resume) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a professional resume writer. Create a detailed, professional resume based on the following information. ");
        prompt.append("Format the resume with proper sections, bullet points, and professional language. ");
        prompt.append("Make the resume compelling and highlight achievements and skills.\n\n");

        // Personal Information
        prompt.append("PERSONAL INFORMATION\n");
        if (resume.getPersonalInformation() != null) {
            prompt.append("Name: ").append(resume.getPersonalInformation().getName()).append("\n");
            prompt.append("Email: ").append(resume.getPersonalInformation().getEmail()).append("\n");
            if (resume.getPersonalInformation().getPhone() != null) {
                prompt.append("Phone: ").append(resume.getPersonalInformation().getPhone()).append("\n");
            }
            if (resume.getPersonalInformation().getAddress() != null) {
                prompt.append("Address: ").append(resume.getPersonalInformation().getAddress()).append("\n");
            }
            if (resume.getPersonalInformation().getLinkedin() != null) {
                prompt.append("LinkedIn: ").append(resume.getPersonalInformation().getLinkedin()).append("\n");
            }
            if (resume.getPersonalInformation().getGithub() != null) {
                prompt.append("GitHub: ").append(resume.getPersonalInformation().getGithub()).append("\n");
            }
        }
        prompt.append("\n");

        // Summary
        if (resume.getSummary() != null && !resume.getSummary().isEmpty()) {
            prompt.append("SUMMARY\n").append(resume.getSummary()).append("\n\n");
        } else {
            prompt.append("SUMMARY\n");
            prompt.append("Experienced professional with strong skills in various domains. ");
            prompt.append("Looking for opportunities to contribute and grow in a dynamic environment.\n\n");
        }

        // Skills
        if (resume.getSkillsList() != null && !resume.getSkillsList().isEmpty()) {
            prompt.append("SKILLS\n");
            resume.getSkillsList().forEach(skill -> prompt.append("- ").append(skill).append("\n"));
            prompt.append("\n");
        } else if (resume.getSkills() != null && !resume.getSkills().isEmpty()) {
            prompt.append("SKILLS\n").append(resume.getSkills()).append("\n\n");
        } else {
            prompt.append("SKILLS\n");
            prompt.append("- Strong communication and interpersonal skills\n");
            prompt.append("- Problem-solving and analytical abilities\n");
            prompt.append("- Team collaboration and leadership\n\n");
        }

        // Experience
        if (resume.getExperiences() != null && !resume.getExperiences().isEmpty()) {
            prompt.append("EXPERIENCE\n");
            resume.getExperiences().forEach(exp -> {
                prompt.append(exp.getCompany()).append(" - ").append(exp.getPosition()).append("\n");
                prompt.append(exp.getDuration()).append("\n");
                if (exp.getResponsibilities() != null) {
                    exp.getResponsibilities().forEach(resp -> prompt.append("- ").append(resp).append("\n"));
                }
                prompt.append("\n");
            });
        } else if (resume.getExperience() != null && !resume.getExperience().isEmpty()) {
            prompt.append("EXPERIENCE\n").append(resume.getExperience()).append("\n\n");
        } else {
            prompt.append("EXPERIENCE\n");
            prompt.append("Professional Experience\n");
            prompt.append("Various roles demonstrating strong work ethic and ability to adapt to different environments.\n\n");
        }

        // Education
        if (resume.getEducations() != null && !resume.getEducations().isEmpty()) {
            prompt.append("EDUCATION\n");
            resume.getEducations().forEach(edu -> {
                prompt.append(edu.getInstitution()).append("\n");
                prompt.append(edu.getDegree()).append(" in ").append(edu.getField()).append("\n");
                prompt.append(edu.getDuration()).append("\n\n");
            });
        } else if (resume.getEducation() != null && !resume.getEducation().isEmpty()) {
            prompt.append("EDUCATION\n").append(resume.getEducation()).append("\n\n");
        } else {
            prompt.append("EDUCATION\n");
            prompt.append("Relevant educational background with focus on professional development.\n\n");
        }

        // Achievements
        if (resume.getAchievementsList() != null && !resume.getAchievementsList().isEmpty()) {
            prompt.append("ACHIEVEMENTS\n");
            resume.getAchievementsList().forEach(achievement -> prompt.append("- ").append(achievement).append("\n"));
            prompt.append("\n");
        } else if (resume.getAchievements() != null && !resume.getAchievements().isEmpty()) {
            prompt.append("ACHIEVEMENTS\n").append(resume.getAchievements()).append("\n\n");
        } else {
            prompt.append("ACHIEVEMENTS\n");
            prompt.append("- Consistently recognized for outstanding performance\n");
            prompt.append("- Successfully completed multiple challenging projects\n\n");
        }

        prompt.append("Please create a professional resume with the following sections:\n");
        prompt.append("1. Contact Information (at the top)\n");
        prompt.append("2. Professional Summary\n");
        prompt.append("3. Skills (as bullet points)\n");
        prompt.append("4. Work Experience (with dates, company names, and achievements)\n");
        prompt.append("5. Education\n");
        prompt.append("6. Achievements and Certifications\n\n");
        prompt.append("Format the resume professionally with proper spacing, bullet points, and section headers. ");
        prompt.append("Use action verbs and quantify achievements where possible. ");
        prompt.append("Make the resume compelling and highlight the most relevant information for the job market.");
        
        return prompt.toString();
    }

    private String generateMockResume(Resume resume) {
        logger.info("Generating mock resume for: {}", 
            resume.getPersonalInformation() != null ? resume.getPersonalInformation().getName() : "Unknown");
        return buildPrompt(resume);
    }
}