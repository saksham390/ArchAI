package com.archai.review.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "architecture_reviews")
public class ArchitectureReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ownerId;

    @Column(nullable = false)
    private Long designId;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "review_findings", joinColumns = @JoinColumn(name = "review_id"))
    @OrderColumn(name = "finding_order")
    private List<ReviewFinding> findings = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public Long getDesignId() { return designId; }
    public void setDesignId(Long designId) { this.designId = designId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public List<ReviewFinding> getFindings() { return findings; }
    public void setFindings(List<ReviewFinding> findings) { this.findings = findings; }
    public Instant getCreatedAt() { return createdAt; }
}
