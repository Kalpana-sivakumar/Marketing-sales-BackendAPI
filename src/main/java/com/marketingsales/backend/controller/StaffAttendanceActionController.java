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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff/attendance")
@RequiredArgsConstructor
@Tag(name = "Staff Attendance Actions", description = "Staff attendance actions from mobile app")
public class StaffAttendanceActionController {

    private final StaffAttendanceService staffAttendanceService;

    @PostMapping("/check-out")
    @PreAuthorize("hasRole('STAFF')")
    public ApiResponse<StaffAttendanceResponse> checkOut(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AttendanceActionRequest request
    ) {
        StaffAttendanceResponse response = staffAttendanceService.checkOut(principal.getId(), request);
        return ApiResponse.success("Check-out recorded successfully", response);
    }
}
