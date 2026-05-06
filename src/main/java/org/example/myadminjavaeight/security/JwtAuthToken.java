package org.example.myadminjavaeight.security;

import java.util.Collection;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/**
 * JWT 认证令牌
 * 继承 UsernamePasswordAuthenticationToken，用于 JWT 登录认证流程
 */
public class JwtAuthToken extends UsernamePasswordAuthenticationToken {
    /**
     * 构造未认证的 Token（用于登录前）
     * @param principal 用户主体信息（通常是用户名）
     * @param credentials 凭证信息（通常是密码）
     */
    public JwtAuthToken(Object principal, Object credentials) {
        super(principal, credentials);
    }

    /**
     * 构造已认证的 Token（用于登录成功后）
     * @param principal 用户主体信息
     * @param credentials 凭证信息
     * @param authorities 用户权限集合
     */
    public JwtAuthToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
    }
}
