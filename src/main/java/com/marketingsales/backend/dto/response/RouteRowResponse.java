package com.marketingsales.backend.dto.response;

import com.marketingsales.backend.constant.RoutePlanStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
public class RouteRowResponse {
    private UUID routeId;
    private String name;
    private String zone;
    private boolean active;
    private LocalDate weekStart;
    private UUID staffId;
    private String staffName;
    private RoutePlanStatus planStatus;
    private long counterCount;
}
