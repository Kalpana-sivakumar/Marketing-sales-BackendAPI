package com.marketingsales.backend.dto.response;

import com.marketingsales.backend.constant.CustomerStatus;
import com.marketingsales.backend.entity.Distributor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class DistributorRowResponse {

    private UUID id;
    private String code;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private String googlePlaceId;
    private String contactPerson;
    private String mobile;
    private String zone;
    private String route;
    private UUID assignedStaffId;
    private String assignedStaffName;
    private CustomerStatus status;
    private BigDecimal outstandingAmount;
    private Instant lastOrderAt;

    public static DistributorRowResponse from(Distributor distributor) {
        return DistributorRowResponse.builder()
                .id(distributor.getId())
                .code(distributor.getCode())
                .name(distributor.getName())
                .address(distributor.getAddress())
                .latitude(distributor.getLatitude())
                .longitude(distributor.getLongitude())
                .googlePlaceId(distributor.getGooglePlaceId())
                .contactPerson(distributor.getContactPerson())
                .mobile(distributor.getMobile())
                .zone(distributor.getZone())
                .route(distributor.getRoute())
                .assignedStaffId(distributor.getAssignedStaffId())
                .assignedStaffName(distributor.getAssignedStaff().getFullName())
                .status(distributor.getStatus())
                .outstandingAmount(distributor.getOutstandingAmount())
                .lastOrderAt(distributor.getLastOrderAt())
                .build();
    }
}