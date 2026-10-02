package com.marketingsales.backend.controller;

import com.marketingsales.backend.dto.request.AttendanceActionRequest;
import com.marketingsales.backend.dto.response.ApiResponse;
import com.marketingsales.backend.dto.response.StaffRouteResponse;
import com.marketingsales.backend.dto.response.StaffRouteStopVisitResponse;
import com.marketingsales.backend.security.UserPrincipal;
import com.marketingsales.backend.service.StaffRouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

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

    @Operation(summary = "Get my route for today")
    @GetMapping("/route/today")
    @PreAuthorize("hasRole('STAFF')")
    public ApiResponse<StaffRouteResponse> getMyTodayRoute(@AuthenticationPrincipal UserPrincipal principal) {
        Optional<StaffRouteResponse> route = staffRouteService.getPublishedRouteForDate(principal.getId(), LocalDate.now());
        return route.map(ApiResponse::success)
                .orElseGet(() -> ApiResponse.success("No published route found for today", null));
    }

    @Operation(summary = "Check in to a route stop")
    @PostMapping("/route/stops/{itemId}/check-in")
    @PreAuthorize("hasRole('STAFF')")
    public ApiResponse<StaffRouteStopVisitResponse> checkInStop(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID itemId,
            @Valid @RequestBody AttendanceActionRequest request
    ) {
        return ApiResponse.success(
                "Stop check-in recorded successfully",
                staffRouteService.checkInStop(principal.getId(), itemId, request)
        );
    }

    @Operation(summary = "Check out from a route stop")
    @PostMapping("/route/stops/{itemId}/check-out")
    @PreAuthorize("hasRole('STAFF')")
    public ApiResponse<StaffRouteStopVisitResponse> checkOutStop(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID itemId,
            @Valid @RequestBody AttendanceActionRequest request
    ) {
        return ApiResponse.success(
                "Stop check-out recorded successfully",
                staffRouteService.checkOutStop(principal.getId(), itemId, request)
        );
    }
}