package com.marketingsales.backend.repository.projection;

import java.util.UUID;

public interface RoutePlanItemCountProjection {
    UUID getRoutePlanId();

    long getCounterCount();
}
