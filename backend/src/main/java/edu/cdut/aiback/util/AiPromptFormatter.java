package edu.cdut.aiback.util;

public final class AiPromptFormatter {
    private AiPromptFormatter() {}

    public static String qwenPrompt(String system, String user) {
        return "<|im_start|>system\n" + system + "<|im_end|>\n"
                + "<|im_start|>user\n" + user + "<|im_end|>\n"
                + "<|im_start|>assistant\n";
    }
}
