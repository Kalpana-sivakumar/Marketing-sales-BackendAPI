package com.marketingsales.backend.service.impl;

import com.marketingsales.backend.constant.Role;
import com.marketingsales.backend.dto.request.CreateUserRequest;
import com.marketingsales.backend.dto.request.UpdateUserRequest;
import com.marketingsales.backend.dto.response.UserPageResponse;
import com.marketingsales.backend.dto.response.UserResponse;
import com.marketingsales.backend.entity.User;
import com.marketingsales.backend.exception.DuplicateResourceException;
import com.marketingsales.backend.exception.ResourceNotFoundException;
import com.marketingsales.backend.repository.UserRepository;
import com.marketingsales.backend.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse create(CreateUserRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .region(request.getRegion().trim())
                .role(request.getRole())
                .build();
        return UserResponse.from(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserPageResponse findAll(String search, Role role, String region, Boolean enabled, int page, int size) {
        Page<User> users = userRepository.searchUsers(
                buildLikePattern(search), role, normalizeToLower(region), enabled,
                PageRequest.of(page, size, Sort.by("fullName").ascending())
        );
        return UserPageResponse.builder()
                .content(users.getContent().stream().map(UserResponse::from).toList())
                .page(users.getNumber())
                .size(users.getSize())
                .totalElements(users.getTotalElements())
                .totalPages(users.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(UUID id) {
        return UserResponse.from(getUser(id));
    }

    @Override
    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest request) {
        User user = getUser(id);
        if (StringUtils.hasText(request.getEmail())) {
            String email = request.getEmail().trim().toLowerCase();
            if (userRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
                throw new DuplicateResourceException("An account with this email already exists");
            }
            user.setEmail(email);
        }
        if (StringUtils.hasText(request.getFullName())) user.setFullName(request.getFullName().trim());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (StringUtils.hasText(request.getRegion())) user.setRegion(request.getRegion().trim());
        if (request.getRole() != null) user.setRole(request.getRole());
        if (StringUtils.hasText(request.getPassword())) user.setPassword(passwordEncoder.encode(request.getPassword()));
        return UserResponse.from(userRepository.saveAndFlush(user));
    }

    @Override
    @Transactional
    public UserResponse updateStatus(UUID id, boolean enabled) {
        User user = getUser(id);
        user.setEnabled(enabled);
        return UserResponse.from(userRepository.saveAndFlush(user));
    }

    private User getUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String normalizeToLower(String value) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase() : null;
    }

    private String buildLikePattern(String value) {
        String normalized = normalizeToLower(value);
        return normalized == null ? "" : "%" + normalized + "%";
    }
}
