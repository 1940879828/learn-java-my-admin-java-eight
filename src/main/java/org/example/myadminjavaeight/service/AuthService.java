package org.example.myadminjavaeight.service;

import org.example.myadminjavaeight.domain.dto.LoginResponse;
import org.example.myadminjavaeight.domain.dto.RefreshRequest;
import org.example.myadminjavaeight.domain.dto.RegisterRequest;

public interface AuthService {
    void register(RegisterRequest registerRequest);

    LoginResponse refresh(RefreshRequest refreshRequest);

    void logout(Long userId);

    void unlockUser(Long userId);
}
