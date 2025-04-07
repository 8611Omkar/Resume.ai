import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/v1/resume';

export const generateResume = async (description) => {
    try {
        // Create a simple request with just the summary
        const response = await axios.post(`${API_BASE_URL}/generate`, {
            summary: description
        }, {
            headers: {
                'Content-Type': 'application/json'
            }
        });
        return response;
    } catch (error) {
        console.error('Error generating resume:', error);
        throw error;
    }
};

// If summary is provided, we can generate a resume
if (resume.getSummary() != null && !resume.getSummary().isEmpty()) {
    logger.info("Generating resume from summary");
    // Extract name and email from summary if not provided
    if (resume.getPersonalInformation() == null) {
        resume.setPersonalInformation(extractPersonalInfo(resume.getSummary()));
    }
    // Validate API key
    if (apiKey == null || apiKey.isEmpty() || "mock-api-key".equals(apiKey)) {
        logger.warn("Invalid or missing GRAQ API key. Using mock implementation.");
        return generateMockResume(resume);
    }
    return generateResumeFromSummary(resume);
} 