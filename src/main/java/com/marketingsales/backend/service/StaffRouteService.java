package com.marketingsales.backend.service;

import com.marketingsales.backend.dto.request.AttendanceActionRequest;
import com.marketingsales.backend.dto.response.AdminRouteTrackingRowResponse;
import com.marketingsales.backend.dto.response.StaffRouteResponse;
import com.marketingsales.backend.dto.response.StaffRouteStopVisitResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StaffRouteService {
    Optional<StaffRouteResponse> getPublishedRouteForDate(UUID staffId, LocalDate date);

    StaffRouteStopVisitResponse checkInStop(UUID staffId, UUID itemId, AttendanceActionRequest request);

    StaffRouteStopVisitResponse checkOutStop(UUID staffId, UUID itemId, AttendanceActionRequest request);

    List<AdminRouteTrackingRowResponse> getRouteTracking(LocalDate date, UUID staffId);
}