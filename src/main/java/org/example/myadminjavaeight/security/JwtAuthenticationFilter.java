package org.example.myadminjavaeight.security;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.example.myadminjavaeight.config.JwtConfig;
import org.example.myadminjavaeight.constants.SecurityConstants;
import org.example.myadminjavaeight.domain.entity.sys.SysUser;
import org.example.myadminjavaeight.utils.JwtUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import io.jsonwebtoken.Claims;

import org.springframework.util.StringUtils;

/**
 * JWT 认证过滤器
 *
 * 继承 OncePerRequestFilter 确保每个请求只经过一次此过滤器。
 * 职责：
 * 1. 从 Authorization Header 中提取 Token
 * 2. 验证 Token 有效性
 * 3. 将用户信息注入 Spring Security 上下文
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final JwtConfig jwtConfig;

    /**
     * 构造函数，注入 JWT 工具类和配置
     */
    public JwtAuthenticationFilter(JwtUtil jwtUtil, JwtConfig jwtConfig) {
        this.jwtUtil = jwtUtil;
        this.jwtConfig = jwtConfig;
    }

    /**
     * 过滤器核心方法，处理每个 HTTP 请求的 JWT 认证
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @param filterChain 过滤器链
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 获取请求头里 Authorization 字段的值
        String header = request.getHeader(jwtConfig.getHeader());

        // 如果没有 Token 或格式不对，直接放行（交给 Spring Security 决定是否拒绝）
        if (!StringUtils.hasText(header) || !header.startsWith(jwtConfig.getTokenPrefix())) {
            filterChain.doFilter(request, response);
            return;
        }
        // 截取 Token 部分
        String token = header.substring(jwtConfig.getTokenPrefix().length());

        try {
            // 验证 Token
            if (!jwtUtil.validateToken(token)) {
                writeUnauthorized(response);
                return;
            }

            // 解析Token里的用户信息
            Claims claims = jwtUtil.parseToken(token);
            Long userId = claims.get(SecurityConstants.CLAIM_USER_ID, Long.class);
            String username = claims.getSubject();
            String authoritiesStr = claims.get(SecurityConstants.CLAIM_AUTHORITIES, String.class);

            // 【Java 8 Stream】将逗号分隔的权限字符串转为权限对象列表
            List<SimpleGrantedAuthority> authorities = Stream.of(authoritiesStr.split(","))
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            // 构建 Spring Security 认证对象并设置到上下文
            JwtUserDetails userDetails = new JwtUserDetails(buildUserEntity(userId, username));
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails,
                    null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            writeUnauthorized(response);
            return;
        }

        // 放行请求，继续后续过滤器和Controller
        filterChain.doFilter(request, response);
    }

    /**
     * 写入 401 未授权响应
     *
     * 当 JWT Token 验证失败（过期、无效、被篡改等）时调用此方法，
     * 向客户端返回标准的 401 响应。
     *
     * @param response HttpServletResponse 对象，用于构建 HTTP 响应
     * @throws IOException 如果写入响应流时发生 I/O 错误
     */
    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        // ========== 第一步：清除安全上下文 ==========
        // SecurityContextHolder 是 Spring Security 存储当前用户认证信息的地方
        // 清除它确保后续代码不会误认为用户已登录
        // 类比：相当于把"已登录"的标签撕掉
        SecurityContextHolder.clearContext();

        // ========== 第二步：设置 HTTP 状态码 ==========
        // SC_UNAUTHORIZED = 401，表示"未授权"
        // 常见状态码：
        // 200 OK - 成功
        // 401 Unauthorized - 未认证（没有 Token 或 Token 无效）
        // 403 Forbidden - 已认证但无权限
        // 404 Not Found - 资源不存在
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // ========== 第三步：设置响应内容类型 ==========
        // application/json 表示返回 JSON 格式数据
        // charset=UTF-8 确保中文等字符正确显示
        // 不设置的话，浏览器可能按 ISO-8859-1 解码，导致中文乱码
        response.setContentType("application/json;charset=UTF-8");

        // ========== 第四步：写入响应体 ==========
        // getWriter() 获取字符输出流
        // write() 写入 JSON 字符串
        // 注意：实际项目中建议使用 Jackson 的 ObjectMapper 来序列化对象
        // 这里直接写字符串是为了简单，避免引入额外依赖
        //
        // 示例：使用 ObjectMapper 的写法
        // ObjectMapper mapper = new ObjectMapper();
        // Map<String, Object> result = new HashMap<>();
        // result.put("code", 401);
        // result.put("message", "Token已过期或无效");
        // response.getWriter().write(mapper.writeValueAsString(result));
        response.getWriter().write("{\"code\":401,\"message\":\"Token已过期或无效\"}");
    }

    /**
     * 构建用户实体对象
     * 从 JWT Token 解析出的用户信息构建 SysUser 对象
     * @param userId 用户ID
     * @param username 用户名
     * @return SysUser 用户实体
     */
    private SysUser buildUserEntity(Long userId, String username) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setUsername(username);
        user.setStatus(1);
        return user;
    }
}
