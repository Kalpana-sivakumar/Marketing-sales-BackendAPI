package com.marketingsales.backend.dto.response;

import com.marketingsales.backend.constant.RouteCounterType;
import com.marketingsales.backend.constant.RouteVisitStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
public class AdminRouteTrackingRowResponse {
    private UUID visitId;
    private UUID staffId;
    private String staffName;
    private UUID routeId;
    private String routeName;
    private LocalDate routeWeekStart;
    private LocalDate visitDate;
    private UUID routePlanItemId;
    private Integer visitOrder;
    private RouteCounterType counterType;
    private String category;
    private UUID counterId;
    private String counterName;
    private String zone;
    private String address;
    private String phone;
    private RouteVisitStatus visitStatus;
    private Instant checkInTime;
    private Instant checkOutTime;
    private Double checkInLatitude;
    private Double checkInLongitude;
    private String checkInLocationName;
    private Double checkOutLatitude;
    private Double checkOutLongitude;
    private String checkOutLocationName;
    private Long visitDurationSeconds;
    private boolean checkInCoordinateMatched;
    private boolean checkOutCoordinateMatched;
    private boolean coordinateVerified;
    private Double checkInDistanceMeters;
    private Double checkOutDistanceMeters;
}