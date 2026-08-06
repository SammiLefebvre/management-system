package edu.cdut.aiback.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "huggingface")
public class HuggingFaceProperties {
    private String apiKey;
    private String model = "Qwen/Qwen2.5-7B-Instruct";
    private String fallbackModel = "meta-llama/Llama-3.2-3B-Instruct";
    private String endpoint = "https://api-inference.huggingface.co/models/";
    private int maxNewTokens = 512;
    private double temperature = 0.7;
}
