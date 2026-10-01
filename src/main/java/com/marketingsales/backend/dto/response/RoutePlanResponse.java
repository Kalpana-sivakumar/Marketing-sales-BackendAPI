package com.marketingsales.backend.dto.response;

import com.marketingsales.backend.constant.RoutePlanStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class RoutePlanResponse {
    private UUID routeId;
    private String routeName;
    private String zone;
    private LocalDate weekStart;
    private UUID staffId;
    private String staffName;
    private RoutePlanStatus status;
    private Instant publishedAt;
    private Long version;
    private List<RoutePlanItemResponse> items;
}
