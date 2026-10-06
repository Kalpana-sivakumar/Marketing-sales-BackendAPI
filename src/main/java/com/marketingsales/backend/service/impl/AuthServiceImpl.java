package com.marketingsales.backend.service.impl;

import com.marketingsales.backend.constant.Role;
import com.marketingsales.backend.dto.request.LoginRequest;
import com.marketingsales.backend.dto.request.RefreshTokenRequest;
import com.marketingsales.backend.dto.request.RegisterRequest;
import com.marketingsales.backend.dto.response.AuthResponse;
import com.marketingsales.backend.entity.User;
import com.marketingsales.backend.exception.DuplicateResourceException;
import com.marketingsales.backend.exception.InvalidTokenException;
import com.marketingsales.backend.repository.UserRepository;
import com.marketingsales.backend.security.JwtUtil;
import com.marketingsales.backend.security.UserPrincipal;
import com.marketingsales.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .region(request.getRegion())
                .role(request.getRole() == null ? Role.STAFF : request.getRole())
                .build();

        User saved = userRepository.save(user);
        return buildAuthResponse(saved);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword())
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new InvalidTokenException("User no longer exists"));
        user.setLastLoginAt(Instant.now());

        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String token = request.getRefreshToken();

        if (jwtUtil.isTokenExpired(token) || !"refresh".equals(jwtUtil.extractTokenType(token))) {
            throw new InvalidTokenException("Refresh token is invalid or expired");
        }

        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new InvalidTokenException("User no longer exists"));
        if (!user.isEnabled() || !user.isAccountNonLocked()) {
            throw new InvalidTokenException("User account is inactive");
        }

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getId().toString(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail(), user.getId().toString());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresInSeconds(jwtUtil.getAccessTokenExpirationSeconds())
                .user(AuthResponse.UserSummary.builder()
                        .id(user.getId().toString())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .role(user.getRole().name())
                        .region(user.getRegion())
                        .lastLoginAt(user.getLastLoginAt())
                        .build())
                .build();
    }
}
