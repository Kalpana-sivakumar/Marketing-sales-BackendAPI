package com.marketingsales.backend.controller;

import com.marketingsales.backend.dto.response.ApiResponse;
import com.marketingsales.backend.dto.response.AuthResponse;
import com.marketingsales.backend.entity.User;
import com.marketingsales.backend.exception.ResourceNotFoundException;
import com.marketingsales.backend.repository.UserRepository;
import com.marketingsales.backend.security.UserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Example of a protected, authenticated-user-scoped endpoint.
 * Demonstrates how downstream feature controllers should read the
 * current principal and enforce role checks (see @PreAuthorize examples
 * once role-scoped features are added, e.g. campaigns, leads, targets).
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Authenticated user profile endpoints")
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ApiResponse<AuthResponse.UserSummary> me(@AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        AuthResponse.UserSummary summary = AuthResponse.UserSummary.builder()
                .id(user.getId().toString())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();

        return ApiResponse.success(summary);
    }
}
