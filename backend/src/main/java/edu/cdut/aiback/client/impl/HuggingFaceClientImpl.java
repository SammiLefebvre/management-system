package edu.cdut.aiback.client.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cdut.aiback.client.HuggingFaceClient;
import edu.cdut.aiback.common.BizException;
import edu.cdut.aiback.config.HuggingFaceProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class HuggingFaceClientImpl implements HuggingFaceClient {

    private final RestTemplate restTemplate;
    private final HuggingFaceProperties properties;
    private final ObjectMapper objectMapper;

    public HuggingFaceClientImpl(RestTemplate restTemplate, HuggingFaceProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String generate(String prompt, String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new BizException("请先提供 HuggingFace API Key");
        }

        String url = properties.getEndpoint() + properties.getModel();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("max_new_tokens", properties.getMaxNewTokens());
        parameters.put("temperature", properties.getTemperature());
        parameters.put("return_full_text", false);

        Map<String, Object> body = new HashMap<>();
        body.put("inputs", prompt);
        body.put("parameters", parameters);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, String.class);
            return parseResponse(response.getBody());
        } catch (Exception e) {
            throw new BizException("AI 服务调用失败: " + e.getMessage());
        }
    }

    private String parseResponse(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            if (root.isArray() && root.size() > 0) {
                return root.get(0).path("generated_text").asText("").trim();
            }
            if (root.has("generated_text")) {
                return root.path("generated_text").asText("").trim();
            }
            throw new BizException("AI 返回格式异常: " + body);
        } catch (Exception e) {
            throw new BizException("AI 返回解析失败: " + e.getMessage());
        }
    }
}
