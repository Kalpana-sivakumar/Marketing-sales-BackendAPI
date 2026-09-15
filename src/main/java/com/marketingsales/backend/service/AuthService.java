package com.marketingsales.backend.service;

import com.marketingsales.backend.dto.request.LoginRequest;
import com.marketingsales.backend.dto.request.RefreshTokenRequest;
import com.marketingsales.backend.dto.request.RegisterRequest;
import com.marketingsales.backend.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);
}
