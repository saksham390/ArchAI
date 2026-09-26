package com.archai.review.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ReviewFinding {
    @Column(length = 24)
    private String severity;

    @Column(length = 64)
    private String category;

    @Column(length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String detail;

    @Column(columnDefinition = "TEXT")
    private String recommendation;

    protected ReviewFinding() {}

    public ReviewFinding(String severity, String category, String title, String detail, String recommendation) {
        this.severity = severity;
        this.category = category;
        this.title = title;
        this.detail = detail;
        this.recommendation = recommendation;
    }

    public String getSeverity() { return severity; }
    public String getCategory() { return category; }
    public String getTitle() { return title; }
    public String getDetail() { return detail; }
    public String getRecommendation() { return recommendation; }
}
