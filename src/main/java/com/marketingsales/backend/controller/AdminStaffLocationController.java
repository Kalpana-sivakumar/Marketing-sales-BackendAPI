package com.marketingsales.backend.controller;

import com.marketingsales.backend.dto.response.AdminLiveStaffLocationResponse;
import com.marketingsales.backend.dto.response.ApiResponse;
import com.marketingsales.backend.service.StaffAttendanceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/staff")
@RequiredArgsConstructor
@Tag(name = "Admin Staff Locations", description = "Administrator staff live-location monitoring endpoints")
public class AdminStaffLocationController {

    private final StaffAttendanceService staffAttendanceService;

    @GetMapping("/live-locations")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<AdminLiveStaffLocationResponse>> getLiveLocations() {
        return ApiResponse.success(staffAttendanceService.getTodayLiveLocationsForAdmin());
    }
}
