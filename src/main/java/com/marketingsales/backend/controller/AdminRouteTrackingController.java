package com.marketingsales.backend.controller;

import com.marketingsales.backend.dto.response.AdminRouteTrackingRowResponse;
import com.marketingsales.backend.dto.response.ApiResponse;
import com.marketingsales.backend.service.StaffRouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/route-tracking")
@RequiredArgsConstructor
@Tag(name = "Admin Route Tracking", description = "Route-stop tracking board for admin and managers")
public class AdminRouteTrackingController {

    private final StaffRouteService staffRouteService;

    @Operation(summary = "Get route tracking rows by date and optional staff")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MARKETING_MANAGER')")
    public ApiResponse<List<AdminRouteTrackingRowResponse>> getRouteTracking(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) UUID staffId
    ) {
        return ApiResponse.success(staffRouteService.getRouteTracking(date, staffId));
    }
}
