package org.example.myadminjavaeight.security;

import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * JWT 认证提供者
 * 继承 DaoAuthenticationProvider，用于处理 JWT 登录认证
 */
public class JwtAuthenticationProvider extends DaoAuthenticationProvider {
    /**
     * 构造函数，配置用户详情服务和密码编码器
     * @param userDetailsService 用户详情服务
     * @param passwordEncoder 密码编码器
     */
    public JwtAuthenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        setHideUserNotFoundExceptions(false); // 不隐藏用户未找到异常，便于前端提示
        setUserDetailsService(userDetailsService);
        setPasswordEncoder(passwordEncoder);
    }

    /**
     * 指定支持的认证类型为 JwtAuthToken
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthToken.class.isAssignableFrom(authentication);
    }
}
