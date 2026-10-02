package com.marketingsales.backend.dto.response;

import com.marketingsales.backend.constant.RouteCounterType;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class CounterLocationPickerResponse {
    private RouteCounterType counterType;
    private UUID counterId;
    private String counterName;
    private String locationName;
    private String address;
    private Double latitude;
    private Double longitude;
    private String openStreetMapPinUrl;
    private String openStreetMapSearchUrl;
    private String openStreetMapEditorUrl;
}
