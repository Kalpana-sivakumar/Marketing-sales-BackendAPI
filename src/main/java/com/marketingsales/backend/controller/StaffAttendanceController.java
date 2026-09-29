package com.marketingsales.backend.controller;

import com.marketingsales.backend.dto.request.AttendanceActionRequest;
import com.marketingsales.backend.dto.request.AttendanceRoutePointRequest;
import com.marketingsales.backend.dto.response.ActiveAttendanceResponse;
import com.marketingsales.backend.dto.response.ApiResponse;
import com.marketingsales.backend.dto.response.AttendanceRoutePointResponse;
import com.marketingsales.backend.dto.response.StaffAttendanceResponse;
import com.marketingsales.backend.security.UserPrincipal;
import com.marketingsales.backend.service.StaffAttendanceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/staff/attendance")
@RequiredArgsConstructor
@Tag(name = "Staff Attendance", description = "Staff attendance and route endpoints for the mobile app")
public class StaffAttendanceController {

    private final StaffAttendanceService staffAttendanceService;

    @PostMapping("/check-in")
    @PreAuthorize("hasRole('STAFF')")
    public ApiResponse<StaffAttendanceResponse> checkIn(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AttendanceActionRequest request
    ) {
        StaffAttendanceResponse response = staffAttendanceService.checkIn(principal.getId(), request);
        return ApiResponse.success("Check-in recorded successfully", response);
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('STAFF')")
    public ApiResponse<List<StaffAttendanceResponse>> getMyAttendanceHistory(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.success(staffAttendanceService.getStaffAttendanceForUser(principal.getId()));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STAFF')")
    public ApiResponse<ActiveAttendanceResponse> getMyActiveAttendance(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Optional<StaffAttendanceResponse> active = staffAttendanceService.getActiveAttendanceForUser(principal.getId());
        if (active.isEmpty()) {
            return ApiResponse.success("No active attendance record", null);
        }

        StaffAttendanceResponse data = active.get();
        ActiveAttendanceResponse response = ActiveAttendanceResponse.builder()
                .attendanceId(data.getAttendanceId())
                .employeeId(data.getEmployeeId())
                .checkInDateTime(data.getCheckInDateTime())
                .checkOutDateTime(data.getCheckOutDateTime())
                .build();
        return ApiResponse.success(response);
    }

    @PostMapping("/route-point")
    @PreAuthorize("hasRole('STAFF')")
    public ApiResponse<AttendanceRoutePointResponse> addRoutePoint(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AttendanceRoutePointRequest request
    ) {
        AttendanceRoutePointResponse response = staffAttendanceService.addRoutePoint(principal.getId(), request);
        return ApiResponse.success("Route point recorded successfully", response);
    }

    @GetMapping("/me/route")
    @PreAuthorize("hasRole('STAFF')")
    public ApiResponse<List<AttendanceRoutePointResponse>> getMyActiveRoute(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<AttendanceRoutePointResponse> points = staffAttendanceService.getActiveRoutePoints(principal.getId());
        return ApiResponse.success(points);
    }
}
