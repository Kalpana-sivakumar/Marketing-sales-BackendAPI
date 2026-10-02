package com.marketingsales.backend.dto.response;

import com.marketingsales.backend.constant.RouteCounterType;
import com.marketingsales.backend.constant.RouteVisitStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class StaffRouteStopResponse {
    private UUID itemId;
    private int visitOrder;
    private RouteCounterType counterType;
    private String category;
    private UUID counterId;
    private String counterName;
    private String contactPerson;
    private String zone;
    private String address;
    private String phone;
    private Double latitude;
    private Double longitude;
    private BigDecimal outstandingAmount;
    private RouteVisitStatus visitStatus;
    private Instant checkInTime;
    private Instant checkOutTime;
    private Long visitDurationSeconds;
}