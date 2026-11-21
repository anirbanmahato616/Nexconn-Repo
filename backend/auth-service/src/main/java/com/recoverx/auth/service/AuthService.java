
package com.recoverx.auth.service;

import com.recoverx.auth.dto.AuthResponse;
import com.recoverx.auth.dto.LoginRequest;
import com.recoverx.auth.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
