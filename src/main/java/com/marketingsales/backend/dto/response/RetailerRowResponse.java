package com.marketingsales.backend.dto.response;

import com.marketingsales.backend.constant.CustomerStatus;
import com.marketingsales.backend.entity.Retailer;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class RetailerRowResponse {

    private UUID id;
    private String hierarchyLabel;
    private String code;
    private String name;
    private String contactPerson;
    private String mobile;
    private String zone;
    private String route;
    private UUID assignedStaffId;
    private String assignedStaffName;
    private CustomerStatus status;
    private BigDecimal outstandingAmount;
    private Instant lastOrderAt;

    public static RetailerRowResponse from(Retailer retailer) {
        return RetailerRowResponse.builder()
                .id(retailer.getId())
                .hierarchyLabel(retailer.isDirectUnderGen1() ? "Direct under Gen1" : "")
                .code(retailer.getCode())
                .name(retailer.getName())
                .contactPerson(retailer.getContactPerson())
                .mobile(retailer.getMobile())
                .zone(retailer.getZone())
                .route(retailer.getRoute())
                .assignedStaffId(retailer.getAssignedStaffId())
                .assignedStaffName(retailer.getAssignedStaff().getFullName())
                .status(retailer.getStatus())
                .outstandingAmount(retailer.getOutstandingAmount())
                .lastOrderAt(retailer.getLastOrderAt())
                .build();
    }
}
