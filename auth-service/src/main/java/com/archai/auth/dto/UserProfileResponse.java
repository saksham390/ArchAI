package com.archai.auth.dto;

import com.archai.auth.entity.User;
import java.time.Instant;

public record UserProfileResponse(String userId, String email, String fullName, String role, Instant createdAt) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
            String.valueOf(user.getId()),
            user.getEmail(),
            user.getFullName(),
            user.getRole().name(),
            user.getCreatedAt()
        );
    }
}
