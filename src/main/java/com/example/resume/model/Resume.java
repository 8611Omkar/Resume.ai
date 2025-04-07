package com.example.resume.model;

import lombok.Data;
import java.util.List;

@Data
public class Resume {
    // Flat structure fields (for backward compatibility)
    private String name;
    private String email;
    private String phone;
    private String summary;
    private String experience;
    private String education;
    private String skills;
    private String achievements;

    // Nested structure fields
    private PersonalInformation personalInformation;
    private List<Experience> experiences;
    private List<Education> educations;
    private List<String> skillsList;
    private List<String> achievementsList;

    @Data
    public static class PersonalInformation {
        private String name;
        private String email;
        private String phone;
        private String address;
        private String linkedin;
        private String github;
    }

    @Data
    public static class Experience {
        private String company;
        private String position;
        private String duration;
        private List<String> responsibilities;
    }

    @Data
    public static class Education {
        private String institution;
        private String degree;
        private String field;
        private String duration;
    }

    // Helper method to get name from either structure
    public String getName() {
        if (personalInformation != null && personalInformation.getName() != null) {
            return personalInformation.getName();
        }
        return name;
    }

    // Helper method to get email from either structure
    public String getEmail() {
        if (personalInformation != null && personalInformation.getEmail() != null) {
            return personalInformation.getEmail();
        }
        return email;
    }

    // Helper method to get phone from either structure
    public String getPhone() {
        if (personalInformation != null && personalInformation.getPhone() != null) {
            return personalInformation.getPhone();
        }
        return phone;
    }
}