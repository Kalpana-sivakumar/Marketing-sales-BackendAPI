package com.marketingsales.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductStatusRequest {

    @NotNull(message = "Active status is required")
    private Boolean active;
}
