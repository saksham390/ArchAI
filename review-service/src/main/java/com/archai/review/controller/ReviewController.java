package com.archai.review.controller;

import com.archai.review.dto.CreateReviewRequest;
import com.archai.review.dto.ReviewResponse;
import com.archai.review.service.ReviewApplicationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    private final ReviewApplicationService reviewService;

    public ReviewController(ReviewApplicationService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public List<ReviewResponse> list(@RequestHeader("X-User-Id") String ownerId) {
        return reviewService.list(ownerId);
    }

    @GetMapping("/{id}")
    public ReviewResponse get(@PathVariable Long id, @RequestHeader("X-User-Id") String ownerId) {
        return reviewService.get(id, ownerId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse create(
        @RequestHeader("X-User-Id") String ownerId,
        @Valid @RequestBody CreateReviewRequest request
    ) {
        return reviewService.create(ownerId, request);
    }
}
