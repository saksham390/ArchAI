package com.archai.chat.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AiChatClient {
    private final RestClient restClient;

    public AiChatClient(RestClient.Builder builder, @Value("${services.ai.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public String reply(String ownerId, String message, String context) {
        try {
            AiChatResponse response = restClient.post()
                .uri("/api/ai/chat")
                .header("X-User-Id", ownerId)
                .body(new AiChatRequest(message, context))
                .retrieve()
                .body(AiChatResponse.class);
            if (response == null || response.reply() == null || response.reply().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI service returned an empty reply");
            }
            return response.reply();
        } catch (RestClientException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI chat service is unavailable", exception);
        }
    }

    public record AiChatRequest(String message, String context) {}
    public record AiChatResponse(String reply) {}
}
