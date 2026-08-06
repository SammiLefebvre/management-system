package edu.cdut.aiback.controller;

import edu.cdut.aiback.service.AiChatService;
import edu.cdut.aiback.service.AiDispatchService;
import edu.cdut.aiback.util.JwtUtil;
import edu.cdut.aiback.vo.AiDispatchAdviceVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private AiChatService aiChatService;

    @MockBean
    private AiDispatchService aiDispatchService;

    @Test
    void chat_shouldReturnAnswer() throws Exception {
        when(aiChatService.chat(any(), any())).thenReturn("今天有 0 条超期工单");
        String token = jwtUtil.generateToken(1L, "test@gzgd.com", "演示项目组", "公司管理");

        mockMvc.perform(post("/api/ai/chat")
                        .header("Authorization", "Bearer " + token)
                        .header("X-HF-Token", "hf-test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"今天有几条超期工单？\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("今天有 0 条超期工单"));
    }

    @Test
    void dispatchAdvice_shouldReturnRecommendation() throws Exception {
        AiDispatchAdviceVO vo = new AiDispatchAdviceVO();
        vo.setWorkOrderId(1L);
        vo.setPersonnelId(2L);
        vo.setName("张三");
        vo.setReason("距离最近");
        when(aiDispatchService.advise(anyLong(), any())).thenReturn(vo);
        String token = jwtUtil.generateToken(1L, "test@gzgd.com", "演示项目组", "公司管理");

        mockMvc.perform(post("/api/ai/dispatch/advice?workOrderId=1")
                        .header("Authorization", "Bearer " + token)
                        .header("X-HF-Token", "hf-test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("张三"));
    }
}
