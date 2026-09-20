package com.marketingsales.backend.service;

import com.marketingsales.backend.dto.request.AttendanceActionRequest;
import com.marketingsales.backend.dto.response.StaffAttendanceResponse;

import java.util.List;
import java.util.UUID;

public interface StaffAttendanceService {
    StaffAttendanceResponse checkIn(UUID userId, AttendanceActionRequest request);

    StaffAttendanceResponse checkOut(UUID userId, AttendanceActionRequest request);

    List<StaffAttendanceResponse> getStaffAttendanceForDashboard();
}
