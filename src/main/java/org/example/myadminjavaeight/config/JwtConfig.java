package org.example.myadminjavaeight.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JWT 配置类
 */
@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
@NoArgsConstructor
public class JwtConfig {
    /**
     * JWT 签名密钥
     */
    private String secret;

    /**
     * 访问令牌过期时间（毫秒）
     */
    private Long accessTokenExpiration;

    /**
     * 刷新令牌过期时间（毫秒）
     */
    private Long refreshTokenExpiration;

    /**
     * 令牌前缀（如 "Bearer "）
     */
    private String tokenPrefix;

    /**
     * HTTP 请求头名称
     */
    private String header;
}
