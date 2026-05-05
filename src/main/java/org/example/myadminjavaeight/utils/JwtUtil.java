package org.example.myadminjavaeight.utils;

import java.util.Date;
import java.util.Optional;

import org.example.myadminjavaeight.config.JwtConfig;
import org.example.myadminjavaeight.constants.SecurityConstants;
import org.springframework.stereotype.Component;

import cn.hutool.core.lang.UUID;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * JWT 工具类
 *
 * 职责：
 * 1. 生成 Access Token（含用户信息和权限）
 * 2. 生成 Refresh Token（仅含用户ID）
 * 3. 解析 Token 获取 Claims
 * 4. 验证 Token 是否有效
 */
@Component
public class JwtUtil {

    private final JwtConfig jwtConfig;

    public JwtUtil(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    /**
     * 生成 Access Token
     *
     * @param userId      用户ID
     * @param username    用户名（作为 subject）
     * @param authorities 权限列表（逗号分隔）
     * @return 签名后的 JWT 字符串
     */
    public String generateAccessToken(Long userId, String username, String authorities) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtConfig.getAccessTokenExpiration());
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .setId(jti)
                .setSubject(username)
                .claim(SecurityConstants.CLAIM_USER_ID, userId)
                .claim(SecurityConstants.CLAIM_AUTHORITIES, authorities)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 生成 Refresh Token
     * 注意：Refresh Token 不包含权限信息，只包含用户ID
     * 这是一个安全设计——即使 Refresh Token 泄露，攻击者也无法直接获取用户权限
     */
    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtConfig.getRefreshTokenExpiration());
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .setId(jti)
                .claim(SecurityConstants.CLAIM_USER_ID, userId)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析 Token，获取 Claims（声明集合）
     * 如果 Token 无效或已过期，会抛出异常
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtConfig.getSecret().getBytes()) // 设置验签密钥
                .build()
                .parseClaimsJws(token) // 解析并验证签名
                .getBody(); // 获取 Payload
    }

    /**
     * 验证 Token 是否有效
     * 内部会检查：签名是否正确、是否过期
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            // 签名错误、过期、格式错误等都会抛异常
            return false;
        }
    }

    /** 从 Token 中提取 JTI（JWT ID） */
    public String getJtiFromToken(String token) {
        return parseToken(token).getId();
    }

    /** 从 Token 中提取用户ID */
    public Optional<Long> getUserIdFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return Optional.ofNullable(claims.get(SecurityConstants.CLAIM_USER_ID, Long.class));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
