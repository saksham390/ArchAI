package com.archai.review.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DesignClient {
    private final RestClient restClient;

    public DesignClient(RestClient.Builder builder, @Value("${services.design.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public DesignSnapshot get(Long designId, String ownerId) {
        try {
            return restClient.get()
                .uri("/api/designs/{id}", designId)
                .header("X-User-Id", ownerId)
                .retrieve()
                .body(DesignSnapshot.class);
        } catch (HttpClientErrorException.NotFound exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Design not found", exception);
        }
    }

    public record DesignSnapshot(Long id, String title, String description, String diagram) {}
}
