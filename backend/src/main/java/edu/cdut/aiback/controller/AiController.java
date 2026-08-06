package edu.cdut.aiback.controller;

import edu.cdut.aiback.common.Result;
import edu.cdut.aiback.service.AiChatService;
import edu.cdut.aiback.service.AiDispatchService;
import edu.cdut.aiback.vo.AiDispatchAdviceVO;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin
public class AiController {

    private final AiChatService aiChatService;
    private final AiDispatchService aiDispatchService;

    public AiController(AiChatService aiChatService, AiDispatchService aiDispatchService) {
        this.aiChatService = aiChatService;
        this.aiDispatchService = aiDispatchService;
    }

    @PostMapping("/chat")
    public Result<String> chat(@RequestBody Map<String, String> body) {
        String message = body.get("message");
        if (message == null || message.isBlank()) {
            return Result.fail("消息不能为空");
        }
        String answer = aiChatService.chat(message);
        return Result.ok(answer);
    }

    @PostMapping("/dispatch/advice")
    public Result<AiDispatchAdviceVO> dispatchAdvice(@RequestParam Long workOrderId) {
        return Result.ok(aiDispatchService.advise(workOrderId));
    }
}
