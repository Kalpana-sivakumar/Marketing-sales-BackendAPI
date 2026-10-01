package com.marketingsales.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RoutePageResponse {
    private List<RouteRowResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
