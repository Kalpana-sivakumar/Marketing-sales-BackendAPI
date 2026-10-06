package com.marketingsales.backend.dto.response;

import com.marketingsales.backend.constant.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class AdminLiveStaffLocationResponse {

    private UUID staffId;
    private String staffName;
    private Double latitude;
    private Double longitude;
    private String locationName;
    private String googlePlaceId;
    private Instant lastUpdated;
    private AttendanceStatus attendanceStatus;
}
