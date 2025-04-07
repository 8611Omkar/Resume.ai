package com.example.resume.controller;

import com.example.resume.model.Resume;
import com.example.resume.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/resume")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"}, 
            allowedHeaders = "*",
            methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS},
            allowCredentials = "true")
public class ResumeController {
    private static final Logger logger = LoggerFactory.getLogger(ResumeController.class);

    @Autowired
    private ResumeService resumeService;

    @PostMapping("/generate")
    public ResponseEntity<?> generateResume(@RequestBody Resume resume) {
        try {
            logger.info("Received resume generation request");
            
            // Validate required fields
            if (resume == null) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "Resume data is required");
            }

            // If summary is provided, we can generate a resume without requiring name/email
            if (resume.getSummary() != null && !resume.getSummary().isEmpty()) {
                logger.info("Generating resume from summary");
                String generatedResume = resumeService.generateResume(resume);
                return ResponseEntity.ok(generatedResume);
            }

            // For non-summary requests, validate required fields
            if ((resume.getName() == null || resume.getName().trim().isEmpty()) && 
                (resume.getPersonalInformation() == null || 
                 resume.getPersonalInformation().getName() == null || 
                 resume.getPersonalInformation().getName().trim().isEmpty())) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "Name is required");
            }

            if ((resume.getEmail() == null || resume.getEmail().trim().isEmpty()) && 
                (resume.getPersonalInformation() == null || 
                 resume.getPersonalInformation().getEmail() == null || 
                 resume.getPersonalInformation().getEmail().trim().isEmpty())) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "Email is required");
            }

            // Log the request details (excluding sensitive information)
            String name = resume.getName() != null ? resume.getName() : 
                         (resume.getPersonalInformation() != null ? resume.getPersonalInformation().getName() : "Unknown");
            logger.info("Generating resume for: {}", name);
            logger.debug("Resume details - Email: {}, Phone: {}", 
                resume.getEmail() != null ? resume.getEmail() : 
                (resume.getPersonalInformation() != null ? resume.getPersonalInformation().getEmail() : "Not provided"),
                resume.getPhone() != null ? resume.getPhone() : 
                (resume.getPersonalInformation() != null ? resume.getPersonalInformation().getPhone() : "Not provided"));

            String generatedResume = resumeService.generateResume(resume);
            return ResponseEntity.ok(generatedResume);
        } catch (Exception e) {
            logger.error("Error generating resume: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error generating resume: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Resume Generator API is running");
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(HttpStatus status, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("message", message);
        return ResponseEntity.status(status).body(response);
    }
}