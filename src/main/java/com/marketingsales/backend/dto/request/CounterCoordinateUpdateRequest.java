package com.marketingsales.backend.dto.request;

import com.marketingsales.backend.constant.RouteCounterType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CounterCoordinateUpdateRequest {

    @NotNull(message = "counterType is required")
    private RouteCounterType counterType;

    @NotNull(message = "counterId is required")
    private UUID counterId;

    @Size(max = 500)
    private String address;

    @NotBlank(message = "locationName is required")
    @Size(max = 180)
    private String locationName;

    @NotNull(message = "latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
    private Double latitude;

    @NotNull(message = "longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
    private Double longitude;
}
