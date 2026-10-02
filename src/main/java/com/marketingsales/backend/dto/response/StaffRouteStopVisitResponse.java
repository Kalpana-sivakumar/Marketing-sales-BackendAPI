package com.marketingsales.backend.dto.response;

import com.marketingsales.backend.constant.RouteVisitStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class StaffRouteStopVisitResponse {
    private UUID itemId;
    private RouteVisitStatus status;
    private Instant checkInTime;
    private Instant checkOutTime;
    private Double checkInLatitude;
    private Double checkInLongitude;
    private String checkInLocationName;
    private Double checkOutLatitude;
    private Double checkOutLongitude;
    private String checkOutLocationName;
    private Long visitDurationSeconds;
    private Double checkInDistanceMeters;
    private Double checkOutDistanceMeters;
    private boolean coordinateVerified;
}