package com.marketingsales.backend.controller;

import com.marketingsales.backend.dto.request.StaffLiveLocationRequest;
import com.marketingsales.backend.dto.response.ApiResponse;
import com.marketingsales.backend.dto.response.StaffLiveLocationResponse;
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
@RequestMapping("/api/staff")
@RequiredArgsConstructor
@Tag(name = "Staff Live Location", description = "Live location updates from staff mobile app")
public class StaffLocationController {

    private final StaffAttendanceService staffAttendanceService;

    @PostMapping("/location")
    @PreAuthorize("hasRole('STAFF')")
    public ApiResponse<StaffLiveLocationResponse> updateLiveLocation(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody StaffLiveLocationRequest request
    ) {
        StaffLiveLocationResponse response = staffAttendanceService.updateLiveLocation(principal.getId(), request);
        return ApiResponse.success("Live location updated successfully", response);
    }
}
