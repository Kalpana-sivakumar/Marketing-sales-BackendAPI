package com.marketingsales.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UserPageResponse {
    private List<UserResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
