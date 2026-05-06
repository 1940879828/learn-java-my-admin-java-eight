package org.example.myadminjavaeight.security.handler;

import java.io.IOException;
import java.util.Date;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.example.myadminjavaeight.common.Result;
import org.example.myadminjavaeight.config.JwtConfig;
import org.example.myadminjavaeight.domain.dto.LoginResponse;
import org.example.myadminjavaeight.domain.entity.sys.SysLoginLog;
import org.example.myadminjavaeight.domain.entity.sys.SysRefreshToken;
import org.example.myadminjavaeight.mapper.LoginLogMapper;
import org.example.myadminjavaeight.mapper.RefreshTokenMapper;
import org.example.myadminjavaeight.mapper.UserMapper;
import org.example.myadminjavaeight.security.JwtUserDetails;
import org.example.myadminjavaeight.utils.HashUtil;
import org.example.myadminjavaeight.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import cn.hutool.json.JSONUtil;
import org.springframework.stereotype.Component;

/**
 * 登录成功处理器
 * 负责生成 JWT Token、保存 RefreshToken、重置登录失败次数、记录登录日志
 */
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {
    private static final Logger log = LoggerFactory.getLogger(LoginSuccessHandler.class);

    private final JwtUtil jwtUtil;
    private final JwtConfig jwtConfig;
    private final RefreshTokenMapper refreshTokenMapper;
    private final UserMapper userMapper;
    private final LoginLogMapper loginLogMapper;

    /**
     * 构造函数，注入所需依赖
     */
    public LoginSuccessHandler(
            JwtUtil jwtUtil,
            JwtConfig jwtConfig,
            RefreshTokenMapper refreshTokenMapper,
            UserMapper userMapper,
            LoginLogMapper loginLogMapper) {
        this.jwtUtil = jwtUtil;
        this.jwtConfig = jwtConfig;
        this.refreshTokenMapper = refreshTokenMapper;
        this.userMapper = userMapper;
        this.loginLogMapper = loginLogMapper;
    }

    /**
     * 认证成功后的处理逻辑
     * 1. 生成 AccessToken 和 RefreshToken
     * 2. 保存 RefreshToken 到数据库
     * 3. 返回 Token 给客户端
     * 4. 重置登录失败次数
     * 5. 记录登录成功日志
     *
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @param authentication 认证信息（包含用户详情和权限）
     */
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        // 获取认证用户信息
        JwtUserDetails userDetails = (JwtUserDetails) authentication.getPrincipal();
        // 【Java 8 Stream】将权限列表转为逗号分隔的字符串
        String authorites = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // 生成 AccessToken（短期有效，用于 API 访问）和 RefreshToken（长期有效，用于刷新 AccessToken）
        String accessToke = jwtUtil.generateAccessToken(userDetails.getUserId(), userDetails.getUsername(), authorites);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails.getUserId());

        // 将 RefreshToken 哈希后存入数据库（安全考虑：不存储原始 Token）
        String tokenHash = HashUtil.sha256(refreshToken);
        String jti = jwtUtil.getJtiFromToken(refreshToken);
        Date expireTime = jwtUtil.parseToken(refreshToken).getExpiration();
        SysRefreshToken sysRefreshToken = new SysRefreshToken();
        sysRefreshToken.setUserId(userDetails.getUserId());
        sysRefreshToken.setTokenHash(tokenHash);
        sysRefreshToken.setExpireTime(expireTime);
        sysRefreshToken.setJtiId(jti);
        refreshTokenMapper.insert(sysRefreshToken);

        // 构建登录响应对象并返回给客户端
        LoginResponse loginResponse = new LoginResponse(accessToke, refreshToken, jwtConfig.getTokenPrefix().trim());

        response.setContentType("aplication/json;chareset=utf-8");
        response.getWriter().write(JSONUtil.toJsonStr(Result.success(loginResponse)));

        // 登录成功后重置失败次数（防止账户被锁定）
        try {
            userMapper.resetFailedAttempts(userDetails.getUserId());
        } catch (Exception e) {
            log.error("[LoginSuccess] 重置失败次数异常, userId: {}", userDetails.getUserId(), e);
        }

        // 记录登录成功日志（用于审计和安全分析）
        SysLoginLog loginLog = new SysLoginLog();
        loginLog.setUsername(userDetails.getUsername());
        loginLog.setLoginIp(getClientIp(request));
        loginLog.setStatus(1); // 1 表示登录成功
        loginLog.setLoginTime(new Date());
        try {
            loginLogMapper.insert(loginLog);
        } catch (Exception e) {
            log.error("[LoginSuccess] 写入审计日志异常", e);
        }

        log.info("[LoginSuccess] 登录成功, username: {}, ip: {}", userDetails.getUsername(), getClientIp(request));
    }

    /**
     * 获取客户端真实 IP 地址
     * 优先从代理头获取，兼容负载均衡和反向代理场景
     *
     * @param request HTTP 请求
     * @return 客户端 IP 地址
     */
    private String getClientIp(HttpServletRequest request) {
        // 优先从 X-Forwarded-For 获取（经过多层代理时会包含多个 IP）
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            // 其次从 X-Real-IP 获取（Nginx 等反向代理会设置此头）
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            // 最后从 RemoteAddr 获取（直连时的客户端 IP）
            ip = request.getRemoteAddr();
        }
        // 如果有多个 IP（逗号分隔），取第一个（最原始的客户端 IP）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
