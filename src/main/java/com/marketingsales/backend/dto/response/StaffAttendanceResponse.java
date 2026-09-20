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
    private String checkInPlace;
    private Instant checkOutDateTime;
    private String checkOutPlace;
}
