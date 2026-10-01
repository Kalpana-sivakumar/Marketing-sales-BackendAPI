package com.marketingsales.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AssignRoutePlanStaffRequest {

    @NotNull(message = "Staff id is required")
    private UUID staffId;
}
