package com.marketingsales.backend.controller;

import com.marketingsales.backend.dto.response.AdminTodayAttendanceResponse;
import com.marketingsales.backend.dto.response.ApiResponse;
import com.marketingsales.backend.dto.response.StaffAttendanceResponse;
import com.marketingsales.backend.service.StaffAttendanceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/attendance")
@RequiredArgsConstructor
@Tag(name = "Admin Attendance", description = "Administrator attendance monitoring endpoints")
public class AdminAttendanceController {

    private final StaffAttendanceService staffAttendanceService;

    @GetMapping("/today")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<AdminTodayAttendanceResponse>> getTodayAttendance() {
        return ApiResponse.success(staffAttendanceService.getTodayAttendanceForAdmin());
    }

    @GetMapping("/staff")
    @PreAuthorize("hasAnyRole('ADMIN','MARKETING_MANAGER')")
    public ApiResponse<List<StaffAttendanceResponse>> getStaffAttendanceForDashboard() {
        return ApiResponse.success(staffAttendanceService.getStaffAttendanceForDashboard());
    }
}
