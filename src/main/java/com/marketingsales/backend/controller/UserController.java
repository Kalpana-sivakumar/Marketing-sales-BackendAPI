package com.marketingsales.backend.controller;

import com.marketingsales.backend.dto.response.ApiResponse;
import com.marketingsales.backend.dto.response.AuthResponse;
import com.marketingsales.backend.constant.Role;
import com.marketingsales.backend.dto.request.CreateUserRequest;
import com.marketingsales.backend.dto.request.UpdateUserRequest;
import com.marketingsales.backend.dto.request.UserStatusRequest;
import com.marketingsales.backend.dto.response.UserPageResponse;
import com.marketingsales.backend.dto.response.UserResponse;
import com.marketingsales.backend.entity.User;
import com.marketingsales.backend.exception.ResourceNotFoundException;
import com.marketingsales.backend.repository.UserRepository;
import com.marketingsales.backend.security.UserPrincipal;
import com.marketingsales.backend.service.UserManagementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
@Tag(name = "Users", description = "Authenticated profile and administrator user management endpoints")
public class UserController {

    private final UserRepository userRepository;
    private final UserManagementService userManagementService;

    @GetMapping("/me")
    public ApiResponse<AuthResponse.UserSummary> me(@AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        AuthResponse.UserSummary summary = AuthResponse.UserSummary.builder()
                .id(user.getId().toString())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .region(user.getRegion())
                .lastLoginAt(user.getLastLoginAt())
                .build();

        return ApiResponse.success(summary);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody CreateUserRequest request) {
        UserResponse user = userManagementService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", user));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserPageResponse> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ApiResponse.success(userManagementService.findAll(search, role, region, enabled, page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> findById(@PathVariable UUID id) {
        return ApiResponse.success(userManagementService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        return ApiResponse.success("User updated successfully", userManagementService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UserStatusRequest request
    ) {
        String message = request.getEnabled() ? "User activated successfully" : "User deactivated successfully";
        return ApiResponse.success(message, userManagementService.updateStatus(id, request.getEnabled()));
    }
}
