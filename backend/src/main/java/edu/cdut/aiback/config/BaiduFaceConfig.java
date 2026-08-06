package edu.cdut.aiback.config;

import com.baidu.aip.face.AipFace;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(BaiduFaceProperties.class)
public class BaiduFaceConfig {

    @Bean
    public AipFace aipFace(BaiduFaceProperties properties) {
        AipFace client = new AipFace(
                properties.getAppId(),
                properties.getApiKey(),
                properties.getSecretKey()
        );
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);
        return client;
    }
}
