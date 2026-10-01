package com.marketingsales.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class StaffRouteResponse {
    private UUID routeId;
    private String routeName;
    private String zone;
    private LocalDate weekStart;
    private List<StaffRouteStopResponse> stops;
}
