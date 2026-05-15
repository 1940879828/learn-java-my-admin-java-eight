package org.example.myadminjavaeight.service;

import org.example.myadminjavaeight.domain.dto.LoginResponse;
import org.example.myadminjavaeight.domain.dto.RefreshRequest;

public interface AuthService {
    LoginResponse refresh(RefreshRequest refreshRequest);

    void logout(Long userId);
}
