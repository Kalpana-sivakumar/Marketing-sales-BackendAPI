package com.marketingsales.backend.dto.response;

import com.marketingsales.backend.constant.RouteCounterType;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class StaffCounterResponse {
    private RouteCounterType counterType;
    private UUID counterId;
    private String code;
    private String name;
    private String zone;
    private String route;
    private UUID plannedRouteId;
    private String plannedRouteName;
}
