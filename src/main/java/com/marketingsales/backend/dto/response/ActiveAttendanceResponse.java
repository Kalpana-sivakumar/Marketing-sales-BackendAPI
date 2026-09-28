package com.marketingsales.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class ActiveAttendanceResponse {

    private UUID attendanceId;
    private UUID employeeId;
    private Instant checkInDateTime;
    private Instant checkOutDateTime;
}
