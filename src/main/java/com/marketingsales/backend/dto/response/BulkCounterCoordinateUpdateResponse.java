package com.marketingsales.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BulkCounterCoordinateUpdateResponse {
    private int updatedCount;
}
