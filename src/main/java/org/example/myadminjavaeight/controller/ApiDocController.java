package org.example.myadminjavaeight.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * API 文档辅助控制器 用于导出 Schemas 等信息，方便复制给 AI 分析
 */
@RestController
@RequestMapping("/api/doc")
@Tag(name = "接口文档")
public class ApiDocController {

    private final ObjectMapper objectMapper;

    @Value("${server.port}")
    private int serverPort;

    public ApiDocController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private JsonNode getOpenApiDoc() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        String apiDocsUrl = "http://localhost:" + serverPort + "/v3/api-docs";
        String json = restTemplate.getForObject(apiDocsUrl, String.class);
        return objectMapper.readTree(json);
    }

    /**
     * 导出所有 Schemas 定义
     */
    @GetMapping(value = "/schemas", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getSchemas() {
        try {
            JsonNode openApiDoc = getOpenApiDoc();
            JsonNode componentsNode = openApiDoc.path("components");
            JsonNode schemasNode = componentsNode.path("schemas");

            if (schemasNode.isMissingNode() || schemasNode.isEmpty()) {
                Map<String, Object> error = new LinkedHashMap<>();
                error.put("error", "Schemas 为空");
                error.put("tip", "请检查 Controller 是否使用了 @RequestBody/@ResponseBody");
                return error;
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("totalCount", schemasNode.size());
            result.put("schemas", objectMapper.convertValue(schemasNode, Map.class));
            return result;
        } catch (Exception e) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "无法获取 schemas 信息");
            error.put("message", e.getMessage());
            error.put("exceptionType", e.getClass().getName());
            return error;
        }
    }

    /**
     * 导出完整的 OpenAPI 文档
     */
    @GetMapping(value = "/full", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonNode getFullApiDoc() {
        try {
            return getOpenApiDoc();
        } catch (Exception e) {
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "无法获取 OpenAPI 文档");
            error.put("message", e.getMessage());
            return objectMapper.valueToTree(error);
        }
    }

    /** 导出所有 API 接口列表 */
    @GetMapping(value = "/apis", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getApis() {
        try {
            JsonNode openApiDoc = getOpenApiDoc();
            JsonNode pathsNode = openApiDoc.path("paths");

            if (pathsNode.isMissingNode() || pathsNode.isEmpty()) {
                return "错误: 没有找到 API 接口\n\n"
                    + "可能原因:\n"
                    + "1. Controller 中没有定义接口\n"
                    + "2. SpringDoc 配置有误";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("# 项目 API 接口列表\n\n");
            sb.append("共 ").append(pathsNode.size()).append(" 个接口路径\n\n");
            sb.append("---\n\n");

            pathsNode
                .fields()
                .forEachRemaining(
                    pathEntry -> {
                        String path = pathEntry.getKey();
                        JsonNode pathItem = pathEntry.getValue();

                        sb.append("## ").append(path).append("\n\n");

                        pathItem
                            .fields()
                            .forEachRemaining(
                                methodEntry -> {
                                    String method = methodEntry.getKey().toUpperCase();
                                    JsonNode operation = methodEntry.getValue();

                                    sb.append("### ").append(method).append("\n\n");

                                    JsonNode summary = operation.path("summary");
                                    if (!summary.isMissingNode()) {
                                        sb.append("**摘要**: ").append(summary.asText()).append("\n\n");
                                    }

                                    JsonNode description = operation.path("description");
                                    if (!description.isMissingNode()) {
                                        sb.append("**描述**: ").append(description.asText()).append("\n\n");
                                    }

                                    JsonNode requestBody = operation.path("requestBody");
                                    if (!requestBody.isMissingNode()) {
                                        sb.append("**请求体**:\n");
                                        JsonNode content = requestBody.path("content");
                                        if (!content.isMissingNode()) {
                                            content
                                                .fields()
                                                .forEachRemaining(
                                                    contentEntry -> {
                                                        String mediaType = contentEntry.getKey();
                                                        JsonNode schema = contentEntry.getValue().path("schema");
                                                        String ref = schema.path("$ref").asText("");
                                                        if (!ref.isEmpty()) {
                                                            String schemaName =
                                                                ref.substring(ref.lastIndexOf('/') + 1);
                                                            sb.append("- ")
                                                                .append(mediaType)
                                                                .append(": ")
                                                                .append(schemaName)
                                                                .append("\n");
                                                        }
                                                    });
                                        }
                                        sb.append("\n");
                                    }

                                    JsonNode responses = operation.path("responses");
                                    if (!responses.isMissingNode()) {
                                        sb.append("**响应**:\n");
                                        responses
                                            .fields()
                                            .forEachRemaining(
                                                responseEntry -> {
                                                    String statusCode = responseEntry.getKey();
                                                    JsonNode response = responseEntry.getValue();
                                                    JsonNode responseDesc = response.path("description");
                                                    sb.append("- ")
                                                        .append(statusCode)
                                                        .append(": ")
                                                        .append(
                                                            responseDesc.isMissingNode()
                                                                ? "无描述"
                                                                : responseDesc.asText())
                                                        .append("\n");
                                                });
                                        sb.append("\n");
                                    }

                                    sb.append("---\n\n");
                                });
                    });

            return sb.toString();
        } catch (Exception e) {
            return "错误: "
                + e.getMessage()
                + "\n\n"
                + "异常类型: "
                + e.getClass().getName()
                + "\n\n"
                + "提示: 请确保应用已完全启动";
        }
    }
}
