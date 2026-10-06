package com.marketingsales.backend.dto.response;

import com.marketingsales.backend.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class UserResponse {
    private UUID id;
    private String fullName;
    private String email;
    private String phone;
    private String region;
    private String role;
    private boolean enabled;
    private Instant lastLoginAt;
    private Instant createdAt;
    private Instant updatedAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .region(user.getRegion())
                .role(user.getRole().name())
                .enabled(user.isEnabled())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
