package com.marketingsales.backend.dto.request;

import com.marketingsales.backend.constant.CustomerStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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

    @Size(max = 500)
    private String address;

    @NotBlank(message = "Mobile is required")
    @Size(max = 20)
    private String mobile;

    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
    private Double longitude;

    @Size(max = 255)
    private String googlePlaceId;

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