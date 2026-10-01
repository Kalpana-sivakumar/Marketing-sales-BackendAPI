package com.marketingsales.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpsertRouteRequest {

    @NotBlank(message = "Route name is required")
    @Size(max = 180)
    private String name;

    @NotBlank(message = "Zone is required")
    @Size(max = 150)
    private String zone;

    @NotNull(message = "Active flag is required")
    private Boolean active;
}
