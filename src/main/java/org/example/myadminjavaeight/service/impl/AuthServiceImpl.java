package org.example.myadminjavaeight.service.impl;

import io.jsonwebtoken.Claims;
import org.example.myadminjavaeight.config.JwtConfig;
import org.example.myadminjavaeight.constants.SecurityConstants;
import org.example.myadminjavaeight.domain.dto.LoginResponse;
import org.example.myadminjavaeight.domain.dto.RefreshRequest;
import org.example.myadminjavaeight.domain.dto.RegisterRequest;
import org.example.myadminjavaeight.domain.entity.sys.SysRefreshToken;
import org.example.myadminjavaeight.domain.entity.sys.SysUser;
import org.example.myadminjavaeight.exception.TokenExpiredException;
import org.example.myadminjavaeight.exception.UsernameExistsException;
import org.example.myadminjavaeight.mapper.RefreshTokenMapper;
import org.example.myadminjavaeight.mapper.UserMapper;
import org.example.myadminjavaeight.service.AuthService;
import org.example.myadminjavaeight.utils.HashUtil;
import org.example.myadminjavaeight.utils.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class AuthServiceImpl implements AuthService {

    private final JwtUtil jwtUtil;
    private final JwtConfig jwtConfig;
    private final RefreshTokenMapper refreshTokenMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(
        JwtUtil jwtUtil,
        JwtConfig jwtConfig,
        RefreshTokenMapper refreshTokenMapper,
        UserMapper userMapper,
        PasswordEncoder passwordEncoder
    ){
        this.jwtUtil = jwtUtil;
        this.jwtConfig = jwtConfig;
        this.refreshTokenMapper = refreshTokenMapper;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void register(RegisterRequest registerRequest) {

        SysUser existingUser = userMapper.findByUsername(registerRequest.getUsername());
        if (existingUser != null) {
            throw new UsernameExistsException();
        }
        SysUser user = new SysUser();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setStatus(1);
        user.setCreateTime(new Date());

        userMapper.insert(user);
    }

    @Override
    @Transactional
    public LoginResponse refresh(RefreshRequest refreshRequest) {

        String refreshTokenValue = refreshRequest.getRefreshToken();

        if (!jwtUtil.validateToken(refreshTokenValue)) {
            throw new TokenExpiredException("Refresh Token无效或已过期");
        }

        Claims claims = jwtUtil.parseToken(refreshTokenValue);
        Long userId = claims.get(SecurityConstants.CLAIM_USER_ID, Long.class);
        String jti = claims.getId();

        String tokenHash = HashUtil.sha256(refreshTokenValue);
        SysRefreshToken storeToken = refreshTokenMapper.findByTokenHash(tokenHash);

        if (storeToken == null) {
            throw new TokenExpiredException("Refresh Token不存在");
        }

        if (storeToken.getExpireTime().before(new Date())) {
            refreshTokenMapper.deleteByJtiId(jti);
            throw new TokenExpiredException("Refresh Token已过期");
        }

        refreshTokenMapper.deleteByJtiId(jti);

        SysUser user = userMapper.findById(userId);
        if (user == null) {
            throw new TokenExpiredException("用户不存在");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new TokenExpiredException("用户已禁用");
        }

        String authorities = "ROLE_USER";
        String newAccessToken = jwtUtil.generateAccessToken(userId, user.getUsername(), authorities);
        String newRefreshToken = jwtUtil.generateRefreshToken(userId);

        String newTokenHash = HashUtil.sha256(refreshTokenValue);
        String newJti = jwtUtil.getJtiFromToken(newRefreshToken);
        Date newExpireTime = jwtUtil.parseToken(newRefreshToken).getExpiration();
        SysRefreshToken newSysRefreshToken = new SysRefreshToken();
        newSysRefreshToken.setUserId(userId);
        newSysRefreshToken.setTokenHash(newTokenHash);
        newSysRefreshToken.setExpireTime(newExpireTime);
        newSysRefreshToken.setJtiId(newJti);
        refreshTokenMapper.insert(newSysRefreshToken);

        return new LoginResponse(newAccessToken, newRefreshToken, jwtConfig.getTokenPrefix().trim());
    }

    @Override
    @Transactional
    public void logout(Long userId) {
        refreshTokenMapper.deleteByUserId(userId);
    }

    @Override
    @Transactional
    public void unlockUser(Long userId) {
        userMapper.unlockUser(userId);
    }
}
