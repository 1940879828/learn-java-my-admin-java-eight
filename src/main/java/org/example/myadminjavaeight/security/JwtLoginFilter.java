package org.example.myadminjavaeight.security;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.example.myadminjavaeight.domain.dto.LoginRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JWT 登录过滤器
 * 继承 UsernamePasswordAuthenticationFilter，拦截登录请求并进行认证
 *
 * 职责：
 * 1. 拦截 POST /auth/login 请求
 * 2. 解析 JSON 格式的登录请求体
 * 3. 创建认证令牌并交给认证管理器处理
 * 4. 委托给成功/失败处理器处理认证结果
 */
public class JwtLoginFilter extends UsernamePasswordAuthenticationFilter {

    /** JSON 序列化/反序列化工具，用于解析登录请求体 */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 构造函数，配置认证管理器和处理器
     * @param authenticationManager 认证管理器，负责执行认证逻辑
     * @param successHandler 登录成功处理器，生成 JWT Token
     * @param failureHandler 登录失败处理器，返回错误信息
     */
    public JwtLoginFilter(
            AuthenticationManager authenticationManager,
            AuthenticationSuccessHandler successHandler,
            AuthenticationFailureHandler failureHandler) {
        setAuthenticationManager(authenticationManager);
        setAuthenticationSuccessHandler(successHandler);
        setAuthenticationFailureHandler(failureHandler);
        // 指定拦截的登录路径和方法
        setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/auth/login", "POST"));
    }

    /**
     * 尝试认证用户
     * 从请求体中解析用户名和密码，创建认证令牌并交给认证管理器处理
     *
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @return 认证结果（成功则包含用户信息和权限）
     * @throws AuthenticationException 认证失败时抛出
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        try {
            // 从请求体中解析 JSON 格式的登录信息
            LoginRequest loginReq = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
            // 提取用户名和密码（null 处理和去除空格）
            String username = loginReq.getUsername() == null ? "" : loginReq.getUsername().trim();
            String password = loginReq.getPassword() == null ? "" : loginReq.getPassword();
            // 创建未认证的 JWT 认证令牌
            JwtAuthToken authRequest = new JwtAuthToken(username, password);
            // 设置请求详情（如 IP 地址、Session ID 等）
            setDetails(request, authRequest);
            // 将用户名存入请求属性，供后续处理器使用
            request.setAttribute("login_username", username);
            // 调用认证管理器进行认证
            return this.getAuthenticationManager().authenticate(authRequest);
        } catch (IOException e) {
            throw new RuntimeException("登录请求解析失败", e);
        }
    }

}
