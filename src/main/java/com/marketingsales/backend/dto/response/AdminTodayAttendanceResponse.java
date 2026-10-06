package com.marketingsales.backend.dto.response;

import com.marketingsales.backend.constant.AttendanceStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class AdminTodayAttendanceResponse {

    private UUID staffId;
    private String staffName;
    private AttendanceStatus status;
    private Instant checkInTime;
    private Instant checkOutTime;
    private String currentLocationName;
    private String currentGooglePlaceId;
    private Double currentLatitude;
    private Double currentLongitude;
    private Instant lastUpdated;
}
