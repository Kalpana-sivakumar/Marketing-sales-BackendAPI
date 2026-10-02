package com.marketingsales.backend.dto.response;

import com.marketingsales.backend.constant.CustomerStatus;
import com.marketingsales.backend.entity.CustomerCounter;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class CustomerCounterRowResponse {

    private UUID id;
    private UUID distributorId;
    private String distributorName;
    private String code;
    private String name;
    private String contactPerson;
    private String address;
    private String mobile;
    private Double latitude;
    private Double longitude;
    private String zone;
    private String route;
    private UUID assignedStaffId;
    private String assignedStaffName;
    private CustomerStatus status;
    private BigDecimal outstandingAmount;
    private Instant lastOrderAt;

    public static CustomerCounterRowResponse from(CustomerCounter counter) {
        return CustomerCounterRowResponse.builder()
                .id(counter.getId())
                .distributorId(counter.getDistributorId())
                .distributorName(counter.getDistributor().getName())
                .code(counter.getCode())
                .name(counter.getName())
                .contactPerson(counter.getContactPerson())
                .address(counter.getAddress())
                .mobile(counter.getMobile())
                .latitude(counter.getLatitude())
                .longitude(counter.getLongitude())
                .zone(counter.getDistributor().getZone())
                .route(counter.getDistributor().getRoute())
                .assignedStaffId(counter.getDistributor().getAssignedStaffId())
                .assignedStaffName(counter.getDistributor().getAssignedStaff().getFullName())
                .status(counter.getStatus())
                .outstandingAmount(counter.getOutstandingAmount())
                .lastOrderAt(counter.getLastOrderAt())
                .build();
    }
}