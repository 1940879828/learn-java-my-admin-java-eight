package org.example.myadminjavaeight.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.SwaggerUiConfigParameters;
import org.springdoc.core.SwaggerUiConfigProperties;
import org.springdoc.core.SwaggerUiOAuthProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springdoc.webmvc.ui.SwaggerIndexTransformer;
import org.springdoc.webmvc.ui.SwaggerWelcomeCommon;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.resource.ResourceTransformerChain;
import org.springdoc.webmvc.ui.SwaggerIndexPageTransformer;
import org.springframework.web.servlet.resource.TransformedResource;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * OpenAPI / Swagger UI 配置。
 * - customOpenAPI()：提供文档基础模板（标题、安全方案、手写接口），springdoc 按类型注入后合并 Controller 注解生成 /v3/api-docs。
 * - swaggerIndexTransformer()：拦截 swagger-ui/index.html，向 html 注入 JS 加载暗色主题 CSS。
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // 安全方案名，下面 components 和 addSecurityItem 通过它互相引用
        final String securitySchemeName = "Bearer Token";
        return new OpenAPI()
            .openapi("3.0.1")
            // 文档头部信息（标题、版本、联系人）
            .info(
                new Info()
                    .title("ADMIN API")
                    .version("1.0.0")
                    .contact(new Contact().name("learn-java-my-admin-java-eight"))
            )
            // 声明 JWT Bearer 鉴权方案，Swagger UI 据此渲染右上角的 Authorize 按钮
            .components(
                new Components()
                    .addSecuritySchemes(
                        securitySchemeName,
                        new SecurityScheme()
                            .name(securitySchemeName)
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                    )
            )
            // 全局默认所有接口都要求 Bearer Token（具体接口可单独覆盖）
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            // 登录接口由 JwtLoginFilter 拦截处理、没有对应 Controller 方法，
            // springdoc 扫描不到，这里手动补充其文档定义
            .path("/api/v1/auth/login", createLoginPathItem());
    }

    /**
     * 手动构造 POST /api/v1/auth/login 的文档：
     * 请求体、200 / 401 响应示例，并声明该接口免鉴权。
     */
    private PathItem createLoginPathItem() {
        return new PathItem()
            .post(
                new Operation()
                    .summary("用户登录")
                    .description("用户登录接口，由 JwtLoginFilter 处理，返回 Access Token 和 Refresh Token")
                    .tags(Collections.singletonList("认证"))
                    // 覆盖全局安全要求：登录接口本身不需要 Token
                    .security(Collections.emptyList())
                    .requestBody(
                        new RequestBody()
                            .description("登录请求参数")
                            .required(true)
                            .content(
                                new Content()
                                    .addMediaType(
                                        "application/json",
                                        new MediaType()
                                            .schema(new Schema<>().$ref("#/components/schemas/LoginRequest"))
                                            .example("{username:admin,password:123456}")
                                    )
                            )
                    )
                    .responses(
                        new ApiResponses()
                            .addApiResponse(
                                "200",
                                new ApiResponse()
                                    .description("登录成功")
                                    .content(new Content().addMediaType(
                                        "application/json",
                                        new MediaType().schema(
                                            new Schema<>()
                                                .example(
                                                    "{\n"
                                                        + "  \"code\": 200,\n"
                                                        + "  \"message\": \"success\",\n"
                                                        + "  \"data\": {\n"
                                                        + "    \"accessToken\": \"eyJhbGciOiJIUzUxMiJ9...\",\n"
                                                        + "    \"refreshToken\": \"eyJhbGciOiJIUzUxMiJ9...\",\n"
                                                        + "    \"tokenPrefix\": \"Bearer\"\n"
                                                        + "  },\n"
                                                        + "  \"timestamp\": 1715644800000\n"
                                                        + "}"
                                                )
                                        )
                                    ))
                            )
                            .addApiResponse(
                                "401",
                                new ApiResponse()
                                    .description("登录失败（用户名或密码错误、账户锁定等）")
                                    .content(new Content().addMediaType(
                                        "application/json",
                                        new MediaType().schema(
                                            new Schema<>().example(
                                                "{\n"
                                                    + "  \"code\": 401,\n"
                                                    + "  \"message\": \"用户名或密码错误\",\n"
                                                    + "  \"data\": {\n"
                                                    + "    \"remainingAttempts\": 4,\n"
                                                    + "    \"lockRemainingSeconds\": null\n"
                                                    + "  },\n"
                                                    + "  \"timestamp\": 1715644800000\n"
                                                    + "}"
                                            )
                                        )
                                    ))
                            )
                    )
            );
    }

    // 暗色主题 CSS 的 CDN 地址，由下面注入的 JS 在浏览器端加载
    private static final String DARK_THEME_CSS_URL = "https://cdn.jsdelivr.net/gh/Amoenus/SwaggerDark@master/SwaggerDark.css";

    /**
     * 自定义 Swagger UI 页面：通过覆盖 springdoc 默认的 SwaggerIndexPageTransformer，
     * 在返回 swagger-ui/index.html 时往末尾追加一段 JS，让浏览器动态加载暗色主题 CSS。
     * 参数由 Spring 自动注入，原样传给父类构造方法以保留默认行为。
     */
    @Bean
    public SwaggerIndexTransformer swaggerIndexTransformer(
        SwaggerUiConfigProperties swaggerUiConfigProperties,
        SwaggerUiOAuthProperties swaggerUiOAuthProperties,
        SwaggerUiConfigParameters swaggerUiConfigParameters,
        SwaggerWelcomeCommon swaggerWelcomeCommon,
        ObjectMapperProvider objectMapperProvider
    ) {

        return new SwaggerIndexPageTransformer(swaggerUiConfigProperties, swaggerUiOAuthProperties, swaggerUiConfigParameters, swaggerWelcomeCommon, objectMapperProvider) {

            /**
             * Spring MVC 在返回 index.html 静态资源前会调用本方法，
             * 这里在父类处理结果上追加一段 JS，运行时插入 <link> 标签加载暗色 CSS。
             */
            @Override
            @NonNull
            public Resource transform(@NonNull HttpServletRequest request, @NonNull Resource resource, @NonNull ResourceTransformerChain transformerChain) throws IOException {

                // 先让父类做默认处理（注入 OAuth 配置、swagger-config 路径等）
                Resource transformed = super.transform(request, resource, transformerChain);
                if (!(transformed instanceof TransformedResource)) {
                    return transformed;
                }

                byte[] bytes = ((TransformedResource) transformed).getByteArray();
                // 这段 JS 在浏览器端执行：创建一个 <link rel="stylesheet"> 并挂到 <head>
                String injection =
                    "\n(function(){var l=document.createElement('link');"
                        + "l.rel='stylesheet';l.href='"
                        + DARK_THEME_CSS_URL
                        + "';document.head.appendChild(l);})();\n";

                // 把 JS 追加到 html 末尾后返回新的资源
                byte[] patched = (new String(bytes, StandardCharsets.UTF_8) + injection).getBytes(StandardCharsets.UTF_8);

                return new TransformedResource(resource, patched);
            }
        };
    }
}
