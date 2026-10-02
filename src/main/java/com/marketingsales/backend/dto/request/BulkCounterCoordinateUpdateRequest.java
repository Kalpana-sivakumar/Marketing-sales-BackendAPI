package com.marketingsales.backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BulkCounterCoordinateUpdateRequest {

    @NotEmpty(message = "items is required")
    private List<@Valid BulkCounterCoordinateItemRequest> items;
}
