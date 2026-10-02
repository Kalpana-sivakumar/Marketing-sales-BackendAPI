package com.marketingsales.backend.dto.request;

import com.marketingsales.backend.constant.RouteCounterType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AddRoutePlanCounterRequest {

    @NotNull(message = "Counter type is required")
    private RouteCounterType counterType;

    @NotNull(message = "Counter id is required")
    private UUID counterId;
}
