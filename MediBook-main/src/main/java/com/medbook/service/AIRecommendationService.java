package com.medbook.service;

import com.medbook.entity.Doctor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AIRecommendationService {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private GeminiService geminiService;

    // Fallback keyword-based recommendation system for when OpenAI is unavailable
    private final Map<String, List<String>> illnessSpecializationMap = Map.of(
        "cardiology", Arrays.asList("heart", "chest pain", "cardiac", "blood pressure", "hypertension", "arrhythmia"),
        "dermatology", Arrays.asList("skin", "rash", "acne", "dermatitis", "eczema", "psoriasis", "mole"),
        "orthopedics", Arrays.asList("bone", "joint", "fracture", "arthritis", "back pain", "knee", "shoulder"),
        "neurology", Arrays.asList("headache", "migraine", "seizure", "dizziness", "numbness", "memory", "brain"),
        "gastroenterology", Arrays.asList("stomach", "digestive", "nausea", "vomiting", "diarrhea", "constipation", "acid reflux"),
        "pediatrics", Arrays.asList("child", "baby", "infant", "fever", "vaccination", "growth", "development"),
        "psychiatry", Arrays.asList("anxiety", "depression", "stress", "mental health", "panic", "mood", "behavior"),
        "ophthalmology", Arrays.asList("eye", "vision", "blurred", "glaucoma", "cataract", "retina", "cornea"),
        "urology", Arrays.asList("urinary", "bladder", "kidney", "prostate", "incontinence", "urination"),
        "gynecology", Arrays.asList("women", "pregnancy", "menstrual", "ovarian", "cervical", "breast", "fertility")
    );

    public List<Doctor> recommendDoctors(String illnessDescription) {
        if (illnessDescription == null || illnessDescription.trim().isEmpty()) {
            return doctorService.getAllDoctors();
        }

        List<Doctor> combined = new ArrayList<>();

        // 1) Try AI and add results first (higher priority)
        try {
            String aiResponse = geminiService.getMedicalSpecializationRecommendation(illnessDescription);
            if (aiResponse != null) {
                String specialization = extractSpecializationFromAIResponse(aiResponse);
                if (specialization != null && !specialization.isEmpty()) {
                    List<Doctor> aiDoctors = doctorService.findBySpecialization(specialization.toLowerCase());
                    combined.addAll(aiDoctors);
                }
            }
        } catch (Exception e) {
            System.err.println("Gemini API failed, continuing with fallback: " + e.getMessage());
        }

        // 2) Add fallback results
        List<Doctor> fallback = recommendDoctorsFallback(illnessDescription);
        combined.addAll(fallback);

        // 3) De-duplicate while preserving order (AI first, then fallback)
        if (combined.isEmpty()) {
            return combined;
        }
        Map<Long, Doctor> uniqueById = new LinkedHashMap<>();
        for (Doctor doctor : combined) {
            if (doctor != null && doctor.getId() != null && !uniqueById.containsKey(doctor.getId())) {
                uniqueById.put(doctor.getId(), doctor);
            }
        }
        return new ArrayList<>(uniqueById.values());
    }

    public String getRecommendationExplanation(String illnessDescription) {
        if (illnessDescription == null || illnessDescription.trim().isEmpty()) {
            return "Based on your description, we recommend consulting with any of our available doctors.";
        }

        try {
            // Try Gemini API first
            String aiResponse = geminiService.getMedicalSpecializationRecommendation(illnessDescription);

            // If AI response is null, it means API key is not configured or API failed
            if (aiResponse != null) {
                return formatAIExplanation(aiResponse);
            }
        } catch (Exception e) {
            System.err.println("Gemini API failed, falling back to keyword matching: " + e.getMessage());
        }

        // Fallback to keyword-based explanation
        return getRecommendationExplanationFallback(illnessDescription);
    }

    public String getDetailedMedicalAdvice(String illnessDescription) {
        if (illnessDescription == null || illnessDescription.trim().isEmpty()) {
            return "Please describe your symptoms for personalized medical advice.";
        }

        try {
            String aiResponse = geminiService.getDetailedMedicalAdvice(illnessDescription);

            // If AI response is null, it means API key is not configured or API failed
            if (aiResponse != null) {
                return aiResponse;
            }
        } catch (Exception e) {
            System.err.println("Gemini API failed for detailed advice: " + e.getMessage());
        }

        // Fallback message when AI is not available
        return "Our AI medical analysis is currently unavailable. Please consult with a healthcare professional for proper evaluation of your symptoms.";
    }

    private String extractSpecializationFromAIResponse(String aiResponse) {
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            return null;
        }

        // Look for "SPECIALIZATION: [name]" pattern
        String[] lines = aiResponse.split("\n");
        for (String line : lines) {
            if (line.toLowerCase().contains("specialization:")) {
                String[] parts = line.split(":");
                if (parts.length > 1) {
                    String specialization = parts[1].trim().split("\\|")[0].trim();
                    return mapSpecializationToDatabase(specialization);
                }
            }
        }

        // If no specific pattern found, try to extract from the response
        String lowerResponse = aiResponse.toLowerCase();
        for (String spec : illnessSpecializationMap.keySet()) {
            if (lowerResponse.contains(spec)) {
                return spec;
            }
        }

        return null;
    }

    private String mapSpecializationToDatabase(String aiSpecialization) {
        String lower = aiSpecialization.toLowerCase();

        // Map common AI responses to our database specializations
        if (lower.contains("cardiology") || lower.contains("cardiac") || lower.contains("heart")) {
            return "cardiology";
        } else if (lower.contains("dermatology") || lower.contains("skin")) {
            return "dermatology";
        } else if (lower.contains("orthopedics") || lower.contains("orthopedic") || lower.contains("bone") || lower.contains("joint")) {
            return "orthopedics";
        } else if (lower.contains("neurology") || lower.contains("neurological") || lower.contains("brain") || lower.contains("nervous")) {
            return "neurology";
        } else if (lower.contains("gastroenterology") || lower.contains("gastro") || lower.contains("digestive") || lower.contains("stomach")) {
            return "gastroenterology";
        } else if (lower.contains("pediatrics") || lower.contains("pediatric") || lower.contains("child")) {
            return "pediatrics";
        } else if (lower.contains("psychiatry") || lower.contains("psychiatric") || lower.contains("mental")) {
            return "psychiatry";
        } else if (lower.contains("ophthalmology") || lower.contains("ophthalmic") || lower.contains("eye") || lower.contains("vision")) {
            return "ophthalmology";
        } else if (lower.contains("urology") || lower.contains("urological") || lower.contains("urinary") || lower.contains("bladder")) {
            return "urology";
        } else if (lower.contains("gynecology") || lower.contains("gynecological") || lower.contains("women") || lower.contains("pregnancy")) {
            return "gynecology";
        }

        return aiSpecialization.toLowerCase();
    }

    private String formatAIExplanation(String aiResponse) {
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            return "Based on your description, we recommend consulting with any of our available doctors.";
        }

        // Extract the reason part if available
        if (aiResponse.contains("| REASON:")) {
            String[] parts = aiResponse.split("\\| REASON:");
            if (parts.length > 1) {
                return "AI Analysis: " + parts[1].trim();
            }
        }

        // If no specific format, return the full response
        return "AI Analysis: " + aiResponse;
    }

    // Fallback methods for when OpenAI is unavailable
    private List<Doctor> recommendDoctorsFallback(String illnessDescription) {
        String description = illnessDescription.toLowerCase();
        Map<String, Integer> specializationScores = new HashMap<>();

        // Calculate scores for each specialization based on keyword matches
        for (Map.Entry<String, List<String>> entry : illnessSpecializationMap.entrySet()) {
            String specialization = entry.getKey();
            List<String> keywords = entry.getValue();

            int score = 0;
            for (String keyword : keywords) {
                if (description.contains(keyword)) {
                    score++;
                }
            }

            if (score > 0) {
                specializationScores.put(specialization, score);
            }
        }

        // If no specific matches found, return all doctors
        if (specializationScores.isEmpty()) {
            return doctorService.getAllDoctors();
        }

        // Sort specializations by score (highest first)
        List<String> recommendedSpecializations = specializationScores.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Get doctors for the top specializations
        List<Doctor> recommendedDoctors = new ArrayList<>();
        for (String specialization : recommendedSpecializations) {
            List<Doctor> doctors = doctorService.findBySpecialization(specialization);
            recommendedDoctors.addAll(doctors);
        }

        // If no doctors found for specific specializations, return all doctors
        if (recommendedDoctors.isEmpty()) {
            return doctorService.getAllDoctors();
        }

        return recommendedDoctors;
    }

    private String getRecommendationExplanationFallback(String illnessDescription) {
        String description = illnessDescription.toLowerCase();
        Map<String, Integer> specializationScores = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : illnessSpecializationMap.entrySet()) {
            String specialization = entry.getKey();
            List<String> keywords = entry.getValue();

            int score = 0;
            for (String keyword : keywords) {
                if (description.contains(keyword)) {
                    score++;
                }
            }

            if (score > 0) {
                specializationScores.put(specialization, score);
            }
        }

        if (specializationScores.isEmpty()) {
            return "Based on your description, we recommend consulting with any of our available doctors.";
        }

        String topSpecialization = specializationScores.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("general");

        return String.format("Based on your symptoms, we recommend consulting with a %s specialist.",
                           topSpecialization);
    }
}