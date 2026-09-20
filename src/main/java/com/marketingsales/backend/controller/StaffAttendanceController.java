package com.marketingsales.backend.controller;

import com.marketingsales.backend.dto.request.AttendanceActionRequest;
import com.marketingsales.backend.dto.response.ApiResponse;
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

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@Tag(name = "Staff Attendance", description = "Check-in/check-out from staff mobile app and attendance list for dashboard")
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

    @PostMapping("/check-out")
    @PreAuthorize("hasRole('STAFF')")
    public ApiResponse<StaffAttendanceResponse> checkOut(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AttendanceActionRequest request
    ) {
        StaffAttendanceResponse response = staffAttendanceService.checkOut(principal.getId(), request);
        return ApiResponse.success("Check-out recorded successfully", response);
    }

    @GetMapping("/staff")
    @PreAuthorize("hasAnyRole('ADMIN','MARKETING_MANAGER')")
    public ApiResponse<List<StaffAttendanceResponse>> getStaffAttendanceForDashboard() {
        List<StaffAttendanceResponse> attendance = staffAttendanceService.getStaffAttendanceForDashboard();
        return ApiResponse.success(attendance);
    }
}
