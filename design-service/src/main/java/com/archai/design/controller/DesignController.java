package com.archai.design.controller;

import com.archai.design.dto.DesignRequest;
import com.archai.design.entity.SystemDesign;
import com.archai.design.repository.SystemDesignRepository;
import java.util.List;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/designs")
public class DesignController {
    private final SystemDesignRepository repository;

    public DesignController(SystemDesignRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<SystemDesign> list(@RequestHeader("X-User-Id") String ownerId) {
        return repository.findAllByOwnerIdOrderByUpdatedAtDesc(ownerId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SystemDesign> get(@PathVariable Long id, @RequestHeader("X-User-Id") String ownerId) {
        return repository.findByIdAndOwnerId(id, ownerId)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SystemDesign> create(
        @RequestHeader("X-User-Id") String ownerId,
        @RequestBody DesignRequest request
    ) {
        validate(request);
        SystemDesign design = new SystemDesign();
        design.setOwnerId(ownerId);
        apply(design, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(design));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SystemDesign> update(
        @PathVariable Long id,
        @RequestHeader("X-User-Id") String ownerId,
        @RequestBody DesignRequest request
    ) {
        validate(request);
        return repository.findByIdAndOwnerId(id, ownerId)
            .map(design -> {
                apply(design, request);
                return ResponseEntity.ok(repository.save(design));
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestHeader("X-User-Id") String ownerId) {
        return repository.findByIdAndOwnerId(id, ownerId)
            .map(design -> {
                repository.delete(design);
                return ResponseEntity.noContent().<Void>build();
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private static void validate(DesignRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Design title is required");
        }
    }

    private static void apply(SystemDesign design, DesignRequest request) {
        design.setTitle(request.title().trim());
        design.setDescription(request.description());
        design.setDiagram(request.diagram());
    }
}
