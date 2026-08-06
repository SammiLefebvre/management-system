package edu.cdut.aiback.controller;

import edu.cdut.aiback.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class StatisticsControllerTest {

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
    void dashboardShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/statistics/dashboard")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void trendsShouldReturnDatesAndSeries() throws Exception {
        mockMvc.perform(get("/api/statistics/trends?days=7")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.dates").isArray())
            .andExpect(jsonPath("$.data.series").isArray());
    }

    @Test
    void heatmapShouldReturnAxisAndData() throws Exception {
        mockMvc.perform(get("/api/statistics/heatmap")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.xAxis").isArray())
            .andExpect(jsonPath("$.data.yAxis").isArray());
    }

    @Test
    void workloadShouldReturnList() throws Exception {
        mockMvc.perform(get("/api/statistics/workload")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void devicesWithLocationShouldReturnList() throws Exception {
        mockMvc.perform(get("/api/statistics/devices-with-location")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").isArray());
    }
}
