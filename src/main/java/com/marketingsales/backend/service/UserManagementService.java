package com.marketingsales.backend.service;

import com.marketingsales.backend.constant.Role;
import com.marketingsales.backend.dto.request.CreateUserRequest;
import com.marketingsales.backend.dto.request.UpdateUserRequest;
import com.marketingsales.backend.dto.response.UserPageResponse;
import com.marketingsales.backend.dto.response.UserResponse;

import java.util.UUID;

public interface UserManagementService {
    UserResponse create(CreateUserRequest request);
    UserPageResponse findAll(String search, Role role, String region, Boolean enabled, int page, int size);
    UserResponse findById(UUID id);
    UserResponse update(UUID id, UpdateUserRequest request);
    UserResponse updateStatus(UUID id, boolean enabled);
}
