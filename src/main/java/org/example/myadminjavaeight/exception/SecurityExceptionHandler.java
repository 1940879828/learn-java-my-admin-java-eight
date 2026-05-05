package org.example.myadminjavaeight.exception;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.example.myadminjavaeight.common.Result;
import org.example.myadminjavaeight.enums.ResultCode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import cn.hutool.json.JSONUtil;

/**
 * 安全异常统一处理器
 *
 * 实现两个接口：
 * - AuthenticationEntryPoint：处理未认证（401）
 * - AccessDeniedHandler：处理无权限（403）
 *
 * 为什么需要这个类？
 * 默认情况下 Spring Security 会返回 HTML 页面或重定向到登录页。
 * 在前后端分离架构中，我们需要统一返回 JSON 格式的错误信息。
 */
@Component
public class SecurityExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {
    /** 处理 403：无权限 */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write(JSONUtil.toJsonStr(
                Result.failure(ResultCode.UNAUTHORIZED.getCode(), ResultCode.UNAUTHORIZED.getMessage())));
    }

    /** 处理 401：未认证 */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write(JSONUtil.toJsonStr(
                Result.failure(ResultCode.UNAUTHORIZED.getCode(), ResultCode.UNAUTHORIZED.getMessage())));
    }

}
