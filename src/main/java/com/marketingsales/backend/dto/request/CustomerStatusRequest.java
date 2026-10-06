package com.marketingsales.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerStatusRequest {

    @NotNull(message = "Active flag is required")
    private Boolean active;
}
