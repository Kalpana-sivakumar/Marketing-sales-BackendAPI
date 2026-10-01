package com.marketingsales.backend.dto.response;

import com.marketingsales.backend.constant.RouteCounterType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class StaffRouteStopResponse {
    private UUID itemId;
    private int visitOrder;
    private RouteCounterType counterType;
    private UUID counterId;
    private String counterName;
    private String address;
    private String phone;
    private Double latitude;
    private Double longitude;
    private BigDecimal outstandingAmount;
}
