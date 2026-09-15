package com.marketingsales.backend.controller;

import com.marketingsales.backend.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Reference-only examples of role-scoped endpoints. Delete this class once
 * real feature controllers (campaigns, leads, targets, reports, etc.) are
 * built — it exists purely to show the @PreAuthorize pattern that both the
 * dashboard (ADMIN / MARKETING_MANAGER) and the mobile app (STAFF) will use.
 */
@RestController
@RequestMapping("/api/demo")
@Tag(name = "Demo (remove in real build)", description = "Reference examples of role-based access control")
public class DemoRoleController {

    @GetMapping("/dashboard-only")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING_MANAGER')")
    public ApiResponse<Map<String, String>> dashboardOnly() {
        return ApiResponse.success(Map.of("message", "Visible to ADMIN and MARKETING_MANAGER only"));
    }

    @GetMapping("/staff-only")
    @PreAuthorize("hasRole('STAFF')")
    public ApiResponse<Map<String, String>> staffOnly() {
        return ApiResponse.success(Map.of("message", "Visible to STAFF (mobile app) only"));
    }

    @GetMapping("/admin-only")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, String>> adminOnly() {
        return ApiResponse.success(Map.of("message", "Visible to ADMIN only"));
    }
}
