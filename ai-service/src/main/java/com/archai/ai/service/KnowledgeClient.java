package com.archai.ai.service;

import com.archai.ai.dto.KnowledgeReference;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class KnowledgeClient {
    private final RestClient restClient;

    public KnowledgeClient(RestClient.Builder builder, @Value("${ai.knowledge.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public List<KnowledgeReference> search(String ownerId, String query) {
        List<KnowledgeReference> results = restClient.get()
            .uri(uriBuilder -> uriBuilder.path("/api/knowledge").queryParam("query", query).build())
            .header("X-User-Id", ownerId)
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});
        return results == null ? List.of() : results;
    }
}
