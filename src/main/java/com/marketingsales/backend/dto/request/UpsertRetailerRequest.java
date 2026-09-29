package com.marketingsales.backend.dto.request;

import com.marketingsales.backend.constant.CustomerStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class UpsertRetailerRequest {

    @NotBlank(message = "Code is required")
    @Size(max = 50)
    private String code;

    @NotBlank(message = "Name is required")
    @Size(max = 180)
    private String name;

    @NotBlank(message = "Contact person is required")
    @Size(max = 150)
    private String contactPerson;

    @NotBlank(message = "Mobile is required")
    @Size(max = 20)
    private String mobile;

    @NotBlank(message = "Zone is required")
    @Size(max = 150)
    private String zone;

    @NotBlank(message = "Route is required")
    @Size(max = 150)
    private String route;

    @NotNull(message = "Assigned staff is required")
    private UUID assignedStaffId;

    @NotNull(message = "Status is required")
    private CustomerStatus status;

    private BigDecimal outstandingAmount;
    private Instant lastOrderAt;
}
