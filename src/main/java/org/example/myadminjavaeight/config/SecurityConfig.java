package org.example.myadminjavaeight.config;

import org.example.myadminjavaeight.exception.SecurityExceptionHandler;
import org.example.myadminjavaeight.security.JwtAuthenticationFilter;
import org.example.myadminjavaeight.security.JwtAuthenticationProvider;
import org.example.myadminjavaeight.security.JwtLoginFilter;
import org.example.myadminjavaeight.security.handler.LoginFailureHandler;
import org.example.myadminjavaeight.security.handler.LoginSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

/**
 * Spring Security 安全配置类
 * 配置 JWT 认证、CORS 跨域、会话管理、权限控制等
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final SecurityExceptionHandler securityExceptionHandler;
    private final UserDetailsService userDetailsService;
    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;
    private final AuthenticationConfiguration authenticationConfiguration;

    public SecurityConfig(
        JwtAuthenticationFilter jwtAuthenticationFilter,
        SecurityExceptionHandler securityExceptionHandler,
        UserDetailsService userDetailsService,
        LoginSuccessHandler loginSuccessHandler,
        LoginFailureHandler loginFailureHandler,
        AuthenticationConfiguration authenticationConfiguration) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.securityExceptionHandler = securityExceptionHandler;
        this.userDetailsService = userDetailsService;
        this.loginSuccessHandler = loginSuccessHandler;
        this.loginFailureHandler = loginFailureHandler;
        this.authenticationConfiguration = authenticationConfiguration;
    }

    /**
     * 配置 Spring Security 过滤器链
     * 包括 CSRF、CORS、会话管理、URL 权限、异常处理、JWT 过滤器等
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        AuthenticationManager authenticationManager = authenticationConfiguration.getAuthenticationManager();
        http.csrf(AbstractHttpConfigurer::disable) // 禁用 CSRF，因为使用 JWT 无状态认证
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // 启用 CORS 跨域
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 无状态会话
            .authorizeRequests(authz ->
                authz.antMatchers("/auth/**") // 认证相关接口放行
                    .permitAll()
                    .antMatchers("/api/doc/**") // 文档接口放行
                    .permitAll()
                    .antMatchers( // Swagger 接口文档相关路径放行
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/swagger-resources/**",
                        "/v3/api-docs/**",
                        "/webjars/**"
                    )
                    .permitAll()
                    .anyRequest() // 其他所有请求需要认证
                    .authenticated()
            )
            .exceptionHandling(exception -> // 配置认证和授权异常处理器
                exception.authenticationEntryPoint(securityExceptionHandler)
                    .accessDeniedHandler(securityExceptionHandler)
            )
            .authenticationProvider(new JwtAuthenticationProvider(userDetailsService, passwordEncoder())) // JWT 认证提供者
            .addFilterBefore( // 添加登录过滤器
                new JwtLoginFilter(authenticationManager, loginSuccessHandler, loginFailureHandler),
                UsernamePasswordAuthenticationFilter.class
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // 添加 JWT 验证过滤器

        return http.build();
    }

    /**
     * CORS 跨域配置 允许所有来源、常用 HTTP 方法、携带凭证
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 密码编码器，使用 BCrypt 强哈希算法
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
