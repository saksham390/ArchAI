package com.archai.review.service;

import com.archai.review.client.AiReviewClient;
import com.archai.review.client.DesignClient;
import com.archai.review.dto.CreateReviewRequest;
import com.archai.review.dto.ReviewResponse;
import com.archai.review.entity.ArchitectureReview;
import com.archai.review.entity.ReviewFinding;
import com.archai.review.repository.ArchitectureReviewRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReviewApplicationService {
    private static final int MAX_FINDINGS = 20;

    private final ArchitectureReviewRepository repository;
    private final DesignClient designClient;
    private final AiReviewClient aiReviewClient;

    public ReviewApplicationService(
        ArchitectureReviewRepository repository,
        DesignClient designClient,
        AiReviewClient aiReviewClient
    ) {
        this.repository = repository;
        this.designClient = designClient;
        this.aiReviewClient = aiReviewClient;
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> list(String ownerId) {
        return repository.findAllByOwnerIdOrderByCreatedAtDesc(ownerId).stream()
            .map(ReviewResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public ReviewResponse get(Long reviewId, String ownerId) {
        return repository.findByIdAndOwnerId(reviewId, ownerId)
            .map(ReviewResponse::from)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
    }

    public ReviewResponse create(String ownerId, CreateReviewRequest request) {
        DesignClient.DesignSnapshot design = designClient.get(request.designId(), ownerId);
        if (design == null || design.diagram() == null || design.diagram().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The design needs a diagram before it can be reviewed");
        }
        String requirements = request.requirements() == null || request.requirements().isBlank()
            ? design.description()
            : request.requirements();
        if (requirements == null || requirements.isBlank()) {
            requirements = "Review the architecture for " + design.title() + ".";
        }

        AiReviewClient.AiReviewResult result = aiReviewClient.review(
            ownerId,
            new AiReviewClient.AiReviewRequest(design.title(), requirements, design.diagram())
        );
        ArchitectureReview review = new ArchitectureReview();
        review.setOwnerId(ownerId);
        review.setDesignId(design.id());
        review.setTitle(design.title());
        review.setScore(Math.max(0, Math.min(100, result.score())));
        review.setSummary(result.summary() == null ? "" : result.summary());
        List<ReviewFinding> findings = result.findings() == null ? List.of() : result.findings().stream()
            .limit(MAX_FINDINGS)
            .map(finding -> new ReviewFinding(
                finding.severity(),
                finding.category(),
                finding.title(),
                finding.detail(),
                finding.recommendation()
            ))
            .toList();
        review.setFindings(findings);
        return ReviewResponse.from(repository.save(review));
    }
}
