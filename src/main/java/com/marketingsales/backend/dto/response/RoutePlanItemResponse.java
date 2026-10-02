package com.marketingsales.backend.dto.response;

import com.marketingsales.backend.constant.RouteCounterType;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class RoutePlanItemResponse {
    private UUID itemId;
    private RouteCounterType counterType;
    private UUID counterId;
    private int visitOrder;
    private String counterName;
    private String zone;
}
