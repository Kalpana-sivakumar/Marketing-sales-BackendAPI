package com.marketingsales.backend.service;

import com.marketingsales.backend.dto.response.StaffRouteResponse;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface StaffRouteService {
    Optional<StaffRouteResponse> getPublishedRouteForDate(UUID staffId, LocalDate date);
}
