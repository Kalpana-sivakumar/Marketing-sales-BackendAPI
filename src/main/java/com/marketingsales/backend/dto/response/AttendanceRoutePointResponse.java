package com.marketingsales.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class AttendanceRoutePointResponse {

    private UUID pointId;
    private UUID attendanceId;
    private Double latitude;
    private Double longitude;
    private String placeName;
    private Instant recordedAt;
}
