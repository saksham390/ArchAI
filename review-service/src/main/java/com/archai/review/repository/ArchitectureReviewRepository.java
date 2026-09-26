package com.archai.review.repository;

import com.archai.review.entity.ArchitectureReview;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArchitectureReviewRepository extends JpaRepository<ArchitectureReview, Long> {
    List<ArchitectureReview> findAllByOwnerIdOrderByCreatedAtDesc(String ownerId);
    Optional<ArchitectureReview> findByIdAndOwnerId(Long id, String ownerId);
}
