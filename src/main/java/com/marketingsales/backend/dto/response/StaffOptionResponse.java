package com.marketingsales.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class StaffOptionResponse {
    private UUID staffId;
    private String fullName;
    private String email;
    private String region;
    private long assignedDistributorCount;
    private long assignedCustomerCount;
    private long assignedRetailerCount;
    private UUID plannedRouteId;
    private String plannedRouteName;
}
