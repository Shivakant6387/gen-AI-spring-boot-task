package com.example.gen.ai.controller;

import com.example.gen.ai.dto.ChatRequest;
import com.example.gen.ai.dto.ChatResponse;
import com.example.gen.ai.model.ChatHistory;
import com.example.gen.ai.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.example.gen.ai.service.GeminiAiService;

import java.util.List;


@RestController
@RequestMapping("/api/genai")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ChatController {
    private final GeminiAiService geminiAiService;
    private final ChatHistoryRepository chatHistoryRepository;

    @PostMapping("/ask")
    public ResponseEntity<ChatResponse> ask(@RequestBody ChatRequest chatRequest) {
        ChatResponse response = geminiAiService.getAiResponse(chatRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatHistory>> getChatHistory() {
        return ResponseEntity.ok(chatHistoryRepository.findAll());
    }
}
