package com.archai.review.dto;

import com.archai.review.entity.ArchitectureReview;
import com.archai.review.entity.ReviewFinding;
import java.time.Instant;
import java.util.List;

public record ReviewResponse(
    Long id,
    Long designId,
    String title,
    int score,
    String summary,
    List<Finding> findings,
    Instant createdAt
) {
    public static ReviewResponse from(ArchitectureReview review) {
        return new ReviewResponse(
            review.getId(),
            review.getDesignId(),
            review.getTitle(),
            review.getScore(),
            review.getSummary(),
            review.getFindings().stream().map(Finding::from).toList(),
            review.getCreatedAt()
        );
    }

    public record Finding(String severity, String category, String title, String detail, String recommendation) {
        static Finding from(ReviewFinding finding) {
            return new Finding(finding.getSeverity(), finding.getCategory(), finding.getTitle(), finding.getDetail(), finding.getRecommendation());
        }
    }
}
