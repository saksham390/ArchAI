package com.archai.ai.controller;

import com.archai.ai.dto.AiGenerationRequest;
import com.archai.ai.dto.AiGenerationResponse;
import com.archai.ai.dto.AiReviewRequest;
import com.archai.ai.dto.AiReviewResponse;
import com.archai.ai.dto.AiChatRequest;
import com.archai.ai.dto.AiChatResponse;
import com.archai.ai.service.AiGenerationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final AiGenerationService generationService;

    public AiController(AiGenerationService generationService) {
        this.generationService = generationService;
    }

    @PostMapping("/generate")
    public AiGenerationResponse generate(
        @RequestHeader("X-User-Id") String ownerId,
        @Valid @RequestBody AiGenerationRequest request
    ) {
        return generationService.generate(ownerId, request);
    }

    @PostMapping("/review")
    public AiReviewResponse review(
        @RequestHeader("X-User-Id") String ownerId,
        @Valid @RequestBody AiReviewRequest request
    ) {
        return generationService.review(ownerId, request);
    }

    @PostMapping("/chat")
    public AiChatResponse chat(
        @RequestHeader("X-User-Id") String ownerId,
        @Valid @RequestBody AiChatRequest request
    ) {
        return generationService.chat(ownerId, request);
    }
}
