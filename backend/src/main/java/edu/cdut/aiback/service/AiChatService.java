package edu.cdut.aiback.service;

import edu.cdut.aiback.client.HuggingFaceClient;
import edu.cdut.aiback.util.AiPromptFormatter;
import org.springframework.stereotype.Service;

@Service
public class AiChatService {

    private final HuggingFaceClient huggingFaceClient;
    private final StatisticsService statisticsService;

    public AiChatService(HuggingFaceClient huggingFaceClient, StatisticsService statisticsService) {
        this.huggingFaceClient = huggingFaceClient;
        this.statisticsService = statisticsService;
    }

    public String chat(String message) {
        String system = "你是工单管理系统的 AI 助手。你只能基于下面提供的系统数据摘要回答，不要编造。如果数据不足，请说明。";
        String context = "当前系统数据摘要：\n" + statisticsService.summaryForAi();
        String user = context + "\n\n用户问题：" + message;
        String prompt = AiPromptFormatter.qwenPrompt(system, user);
        return huggingFaceClient.generate(prompt);
    }
}
