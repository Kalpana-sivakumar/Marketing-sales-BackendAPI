package com.marketingsales.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class StaffAttendanceResponse {

    private UUID attendanceId;
    private UUID employeeId;
    private String employeeName;
    private String contactNumber;
    private Instant checkInDateTime;
    private Double checkInLatitude;
    private Double checkInLongitude;
    private Instant checkOutDateTime;
    private Double checkOutLatitude;
    private Double checkOutLongitude;
}
