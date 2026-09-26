package com.archai.review.client;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AiReviewClient {
    private final RestClient restClient;

    public AiReviewClient(RestClient.Builder builder, @Value("${services.ai.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public AiReviewResult review(String ownerId, AiReviewRequest request) {
        try {
            return restClient.post()
                .uri("/api/ai/review")
                .header("X-User-Id", ownerId)
                .body(request)
                .retrieve()
                .body(AiReviewResult.class);
        } catch (RestClientException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI review service is unavailable", exception);
        }
    }

    public record AiReviewRequest(String title, String requirements, String architectureDiagram) {}
    public record AiReviewResult(int score, String summary, List<Finding> findings) {}
    public record Finding(String severity, String category, String title, String detail, String recommendation) {}
}
