package com.archai.knowledge.controller;

import com.archai.knowledge.dto.KnowledgeDocumentRequest;
import com.archai.knowledge.dto.KnowledgeDocumentResponse;
import com.archai.knowledge.entity.KnowledgeDocument;
import com.archai.knowledge.repository.KnowledgeDocumentRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {
    private static final int MAX_RESULTS = 50;
    private final KnowledgeDocumentRepository repository;

    public KnowledgeController(KnowledgeDocumentRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<KnowledgeDocumentResponse> list(
        @RequestHeader("X-User-Id") String ownerId,
        @RequestParam(required = false) String query
    ) {
        var page = PageRequest.of(0, MAX_RESULTS);
        List<KnowledgeDocument> documents = query == null || query.isBlank()
            ? repository.findAllByOwnerIdOrderByCreatedAtDesc(ownerId, page)
            : repository.searchByOwnerIdAndText(ownerId, query.trim(), page);
        return documents.stream().map(KnowledgeDocumentResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<KnowledgeDocumentResponse> get(
        @PathVariable Long id,
        @RequestHeader("X-User-Id") String ownerId
    ) {
        return repository.findByIdAndOwnerId(id, ownerId)
            .map(KnowledgeDocumentResponse::from)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<KnowledgeDocumentResponse> create(
        @RequestHeader("X-User-Id") String ownerId,
        @Valid @RequestBody KnowledgeDocumentRequest request
    ) {
        KnowledgeDocument document = new KnowledgeDocument();
        document.setOwnerId(ownerId);
        apply(document, request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(KnowledgeDocumentResponse.from(repository.save(document)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<KnowledgeDocumentResponse> update(
        @PathVariable Long id,
        @RequestHeader("X-User-Id") String ownerId,
        @Valid @RequestBody KnowledgeDocumentRequest request
    ) {
        return repository.findByIdAndOwnerId(id, ownerId)
            .map(document -> {
                apply(document, request);
                return ResponseEntity.ok(KnowledgeDocumentResponse.from(repository.save(document)));
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestHeader("X-User-Id") String ownerId) {
        return repository.findByIdAndOwnerId(id, ownerId)
            .map(document -> {
                repository.delete(document);
                return ResponseEntity.noContent().<Void>build();
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private static void apply(KnowledgeDocument document, KnowledgeDocumentRequest request) {
        document.setTitle(request.title().trim());
        document.setContent(request.content());
        document.setSourceUrl(request.sourceUrl());
    }
}
