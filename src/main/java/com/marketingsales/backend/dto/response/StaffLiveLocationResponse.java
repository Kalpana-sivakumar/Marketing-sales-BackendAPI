package com.marketingsales.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class StaffLiveLocationResponse {

    private UUID locationId;
    private UUID staffId;
    private UUID attendanceId;
    private Double latitude;
    private Double longitude;
    private String locationName;
    private Instant recordedAt;
}
