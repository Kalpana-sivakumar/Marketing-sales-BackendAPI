package com.marketingsales.backend.service;

import com.marketingsales.backend.dto.request.AttendanceActionRequest;
import com.marketingsales.backend.dto.request.AttendanceRoutePointRequest;
import com.marketingsales.backend.dto.response.StaffAttendanceResponse;
import com.marketingsales.backend.dto.response.AttendanceRoutePointResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StaffAttendanceService {
    StaffAttendanceResponse checkIn(UUID userId, AttendanceActionRequest request);

    StaffAttendanceResponse checkOut(UUID userId, AttendanceActionRequest request);

    List<StaffAttendanceResponse> getStaffAttendanceForDashboard();

    List<StaffAttendanceResponse> getStaffAttendanceForUser(UUID userId);

    Optional<StaffAttendanceResponse> getActiveAttendanceForUser(UUID userId);

    AttendanceRoutePointResponse addRoutePoint(UUID userId, AttendanceRoutePointRequest request);

    List<AttendanceRoutePointResponse> getActiveRoutePoints(UUID userId);
}
