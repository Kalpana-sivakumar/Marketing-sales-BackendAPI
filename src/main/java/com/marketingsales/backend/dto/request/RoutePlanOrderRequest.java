package com.marketingsales.backend.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RoutePlanOrderRequest {

    @NotEmpty(message = "itemIds cannot be empty")
    private List<UUID> itemIds;
}
