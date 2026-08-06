package edu.cdut.aiback.client;

import edu.cdut.aiback.client.impl.HuggingFaceClientImpl;
import edu.cdut.aiback.config.HuggingFaceProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class HuggingFaceClientTest {

    @Test
    void generate_shouldCallEndpointAndReturnGeneratedText() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        HuggingFaceProperties properties = new HuggingFaceProperties();
        properties.setApiKey("test-key");
        properties.setModel("Qwen/Qwen2.5-7B-Instruct");

        HuggingFaceClientImpl client = new HuggingFaceClientImpl(restTemplate, properties);

        String responseBody = "[{\"generated_text\":\" 你好 \"}]";
        ResponseEntity<String> response = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.exchange(
                eq("https://api-inference.huggingface.co/models/Qwen/Qwen2.5-7B-Instruct"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(response);

        String result = client.generate("prompt");

        assertEquals("你好", result);
    }
}
