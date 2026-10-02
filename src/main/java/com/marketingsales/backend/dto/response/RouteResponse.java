package com.marketingsales.backend.dto.response;

import com.marketingsales.backend.entity.Route;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class RouteResponse {
    private UUID id;
    private String name;
    private String zone;
    private boolean active;

    public static RouteResponse from(Route route) {
        return RouteResponse.builder()
                .id(route.getId())
                .name(route.getName())
                .zone(route.getZone())
                .active(route.isActive())
                .build();
    }
}
