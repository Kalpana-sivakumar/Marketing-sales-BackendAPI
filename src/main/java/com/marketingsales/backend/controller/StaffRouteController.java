package com.marketingsales.backend.controller;

import com.marketingsales.backend.dto.response.ApiResponse;
import com.marketingsales.backend.dto.response.StaffRouteResponse;
import com.marketingsales.backend.security.UserPrincipal;
import com.marketingsales.backend.service.StaffRouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/api/staff/me")
@RequiredArgsConstructor
@Tag(name = "Staff Route", description = "Published weekly route for staff mobile app")
public class StaffRouteController {

    private final StaffRouteService staffRouteService;

    @Operation(summary = "Get my published route for a date")
    @GetMapping("/route")
    @PreAuthorize("hasRole('STAFF')")
    public ApiResponse<StaffRouteResponse> getMyRoute(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        Optional<StaffRouteResponse> route = staffRouteService.getPublishedRouteForDate(principal.getId(), date);
        return route.map(ApiResponse::success)
                .orElseGet(() -> ApiResponse.success("No published route found", null));
    }
}
