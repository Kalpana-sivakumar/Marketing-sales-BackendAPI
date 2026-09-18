package com.marketingsales.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserStatusRequest {

    @NotNull(message = "Enabled status is required")
    private Boolean enabled;
}
