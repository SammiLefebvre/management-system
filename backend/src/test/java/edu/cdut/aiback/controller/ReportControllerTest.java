package edu.cdut.aiback.controller;

import edu.cdut.aiback.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    private String token;

    @BeforeEach
    void setUp() {
        token = jwtUtil.generateToken(1L, "test@gzgd.com", "演示项目组", "公司管理");
    }

    @Test
    void exportExcelShouldReturn200() throws Exception {
        String body = "{\"dataType\":\"device\",\"startDate\":\"2026-07-01\",\"endDate\":\"2026-08-01\"}";
        mockMvc.perform(post("/api/reports/export-excel")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk());
    }

    @Test
    void exportPdfShouldReturn200() throws Exception {
        String body = "{\"dataType\":\"device\",\"startDate\":\"2026-07-01\",\"endDate\":\"2026-08-01\"}";
        mockMvc.perform(post("/api/reports/export-pdf")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk());
    }
}
