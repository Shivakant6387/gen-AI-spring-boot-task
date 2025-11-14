package com.example.gen.ai.service;

import com.example.gen.ai.dto.ChatRequest;
import com.example.gen.ai.dto.ChatResponse;
import com.example.gen.ai.model.ChatHistory;
import com.example.gen.ai.repository.ChatHistoryRepository;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GeminiAiService {

    private final ChatHistoryRepository chatHistoryRepository;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.model}")
    private String geminiModel; // e.g., gemini-2.5


    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private final RestTemplate restTemplate = new RestTemplate();

    // ---------------------- TEXT GENERATION ----------------------
    public ChatResponse getAiResponse(ChatRequest request) {
        String prompt = request.getPrompt() != null ? request.getPrompt() : "";

        Map<String, Object> part = Map.of("text", prompt);
        Map<String, Object> contentMap = Map.of("parts", List.of(part));
        Map<String, Object> generationConfig = Map.of(
                "temperature", 0.7,
                "maxOutputTokens", 512
        );

        Map<String, Object> body = Map.of(
                "contents", List.of(contentMap),
                "generationConfig", generationConfig
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            String apiUrl = GEMINI_API_URL + geminiModel + ":generateContent?key=" + geminiApiKey;

            ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, Map.class);

            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                return new ChatResponse("⚠️ No response from Gemini API.");
            }

            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            String aiResponse = (String) parts.get(0).get("text");

            // Save chat history
            ChatHistory chatHistory = ChatHistory.builder()
                    .userPrompt(prompt)
                    .aiResponse(aiResponse)
                    .createdAt(LocalDateTime.now())
                    .build();
            chatHistoryRepository.save(chatHistory);

            return new ChatResponse(aiResponse);

        } catch (Exception e) {
            return new ChatResponse("❌ Gemini API error: " + e.getMessage());
        }
    }
}
