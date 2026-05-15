package org.example.myadminjavaeight.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.myadminjavaeight.common.Result;
import org.example.myadminjavaeight.domain.dto.LoginResponse;
import org.example.myadminjavaeight.domain.dto.RefreshRequest;
import org.example.myadminjavaeight.domain.dto.RegisterRequest;
import org.example.myadminjavaeight.security.JwtUserDetails;
import org.example.myadminjavaeight.service.AuthService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
@Tag(name = "认证管理")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<Void> register(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.register(registerRequest);
        return Result.success();
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新token")
    public Result<LoginResponse> refresh(@Valid @RequestBody RefreshRequest refreshRequest) {
        LoginResponse response = authService.refresh(refreshRequest);
        return Result.success(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    public Result<Void> logout(@AuthenticationPrincipal JwtUserDetails userDetails) {
        authService.logout(userDetails.getUserId());
        return Result.success();
    }

    @PostMapping("/unlock/{userId}")
    @Operation(summary = "解锁用户账户")
    public Result<Void> unlockUser(@PathVariable Long userId) {
        authService.unlockUser(userId);
        return Result.success();
    }
}
