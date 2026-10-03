package com.medbook.service;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GeminiService {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.base-url:https://generativelanguage.googleapis.com/v1beta/openai/}")
    private String baseUrl;

    @Value("${gemini.api.model:gemini-1.5-flash}")
    private String model;

    @Value("${gemini.api.max-tokens:200}")
    private Integer maxTokens;

    @Value("${gemini.api.temperature:0.3}")
    private Double temperature;

    private final OkHttpClient httpClient = new OkHttpClient();

    public String getMedicalSpecializationRecommendation(String symptoms) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return null;
        }

        String prompt = String.format("""
            You are a medical AI assistant. Based on the patient's symptoms described below, recommend the most appropriate medical specialization.

            Patient symptoms: "%s"

            Available specializations:
            - Cardiology (heart, cardiovascular issues)
            - Dermatology (skin conditions)
            - Orthopedics (bones, joints, muscles)
            - Neurology (brain, nervous system)
            - Gastroenterology (digestive system)
            - Pediatrics (children's health)
            - Psychiatry (mental health)
            - Ophthalmology (eye conditions)
            - Urology (urinary system)
            - Gynecology (women's health)

            Please respond with ONLY the specialization name (e.g., "Cardiology") and a brief explanation of why this specialization is recommended.
            Format: "SPECIALIZATION: [name] | REASON: [brief explanation]"
            """, symptoms);

        return callChatCompletions(prompt);
    }

    public String getDetailedMedicalAdvice(String symptoms) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return null;
        }

        String prompt = String.format("""
            You are a medical AI assistant. A patient has described the following symptoms:

            "%s"

            Please provide:
            1. A brief analysis of the symptoms
            2. Recommended next steps

            Keep the response in 50 words. Do not provide specific diagnoses.
            """, symptoms);

        return callChatCompletions(prompt);
    }

    private String callChatCompletions(String userPrompt) {
        String url = baseUrl.endsWith("/") ? baseUrl + "chat/completions" : baseUrl + "/chat/completions";

        String body = "{"
                + "\"model\":\"" + escape(model) + "\"," 
                + "\"temperature\":" + temperature + ","
                + "\"max_tokens\":" + maxTokens + ","
                + "\"messages\":["
                + "{\"role\":\"system\",\"content\":\"You are a helpful medical AI assistant.\"},"
                + "{\"role\":\"user\",\"content\":\"" + escape(userPrompt) + "\"}"
                + "]" 
                + "}";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("x-goog-api-key", apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(body, JSON))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return null;
            }
            String json = response.body().string();
            return extractContentFromOpenAIStyleResponse(json);
        } catch (IOException e) {
            return null;
        }
    }

    private String extractContentFromOpenAIStyleResponse(String json) {
        // extremely small extraction to avoid adding a JSON library; Gemini's
        // OpenAI-compatible response includes "choices":[{"message":{"content":"..."}}]
        int msgIndex = json.indexOf("\"message\"");
        if (msgIndex < 0) return null;
        int contentIndex = json.indexOf("\"content\"", msgIndex);
        if (contentIndex < 0) return null;
        int colon = json.indexOf(":", contentIndex);
        if (colon < 0) return null;
        int startQuote = json.indexOf('"', colon + 1);
        if (startQuote < 0) return null;
        int endQuote = json.indexOf('"', startQuote + 1);
        if (endQuote < 0) return null;
        String content = json.substring(startQuote + 1, endQuote);
        return content.replace("\\n", "\n").replace("\\\"", "\"");
    }

    private String escape(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}


