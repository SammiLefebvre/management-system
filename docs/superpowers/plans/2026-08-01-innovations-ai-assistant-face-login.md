# 创新功能实现计划：AI 助手 + 人脸识别登录

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox syntax for tracking.

**Goal:** 完成后端 Hugging Face AI 助手/派单建议、人脸识别登录、前端对应 UI，并推送到 GitHub。

**Architecture:** 后端通过 RestTemplate 调用 Hugging Face Serverless Inference API；百度人脸识别复用现有 `AipFace`；前端用 `<video>` + `<canvas>` 实现摄像头抓拍。

**Tech Stack:** Spring Boot 3, RestTemplate, Hugging Face Inference API, Baidu AipFace, Vue 3, Element Plus.

## Global Constraints
- 不得将 Hugging Face API Token 提交到 git，统一通过环境变量 `HUGGINGFACE_API_KEY` 注入。
- 保持现有业务逻辑不变。
- 每个后端 Task 必须附带测试。
- 每次修改后运行 `mvnw.cmd test` 和 `npm run build`。

---

## Task 1: 后端 HuggingFace 配置与通用 AI 对话接口

**Files:**
- Create: `backend/src/main/java/edu/cdut/aiback/config/HuggingFaceProperties.java`
- Create: `backend/src/main/java/edu/cdut/aiback/client/HuggingFaceClient.java`
- Create: `backend/src/main/java/edu/cdut/aiback/client/impl/HuggingFaceClientImpl.java`
- Create: `backend/src/main/java/edu/cdut/aiback/service/AiChatService.java`
- Create: `backend/src/main/java/edu/cdut/aiback/controller/AiController.java`
- Create: `backend/src/test/java/edu/cdut/aiback/client/HuggingFaceClientTest.java`
- Create: `backend/src/test/java/edu/cdut/aiback/controller/AiControllerTest.java`
- Modify: `backend/src/main/resources/application.yml`
- Modify: `backend/src/test/resources/application.yml`

**Interfaces:**
- `HuggingFaceClient.generate(String prompt)` → `String`
- `AiChatService.chat(String message, String projectGroup)` → `String`
- `POST /api/ai/chat` → `Result<String>`

- [ ] **Step 1: Add config class**

```java
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
```

- [ ] **Step 2: Add RestTemplate bean and client interface/impl**

Create `RestTemplateConfig.java` if not exists:

```java
package edu.cdut.aiback.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

`HuggingFaceClient.java`:

```java
package edu.cdut.aiback.client;

public interface HuggingFaceClient {
    String generate(String prompt);
}
```

`HuggingFaceClientImpl.java`:

```java
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
    public String generate(String prompt) {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new BizException("HUGGINGFACE_API_KEY 未配置");
        }

        String url = properties.getEndpoint() + properties.getModel();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(properties.getApiKey());
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
```

- [ ] **Step 3: Add prompt formatter utility**

```java
package edu.cdut.aiback.util;

public final class AiPromptFormatter {
    private AiPromptFormatter() {}

    public static String qwenPrompt(String system, String user) {
        return "<|im_start|>system\n" + system + "<|im_end|>\n"
                + "<|im_start|>user\n" + user + "<|im_end|>\n"
                + "<|im_start|>assistant\n";
    }
}
```

- [ ] **Step 4: Add AiChatService**

```java
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

    public String chat(String message, String projectGroup) {
        String system = "你是工单管理系统的 AI 助手。你只能基于下面提供的系统数据回答，不要编造。如果数据不足，请说明。";
        String context = "当前系统数据摘要：\n" + statisticsService.summaryForAi(projectGroup);
        String user = context + "\n\n用户问题：" + message;
        String prompt = AiPromptFormatter.qwenPrompt(system, user);
        return huggingFaceClient.generate(prompt);
    }
}
```

- [ ] **Step 5: Add AiController**

```java
package edu.cdut.aiback.controller;

import edu.cdut.aiback.common.Result;
import edu.cdut.aiback.common.UserContext;
import edu.cdut.aiback.service.AiChatService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin
public class AiController {

    private final AiChatService aiChatService;

    public AiController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @PostMapping("/chat")
    public Result<String> chat(@RequestBody Map<String, String> body) {
        String message = body.get("message");
        if (message == null || message.isBlank()) {
            return Result.error("消息不能为空");
        }
        String answer = aiChatService.chat(message, UserContext.getProjectGroup());
        return Result.ok(answer);
    }
}
```

- [ ] **Step 6: Update application.yml**

Add at the end of `backend/src/main/resources/application.yml`:

```yaml
huggingface:
  api-key: ${HUGGINGFACE_API_KEY:}
  model: Qwen/Qwen2.5-7B-Instruct
  fallback-model: meta-llama/Llama-3.2-3B-Instruct
  endpoint: https://api-inference.huggingface.co/models/
  max-new-tokens: 512
  temperature: 0.7
```

- [ ] **Step 7: Add StatisticsService.summaryForAi()**

Modify `StatisticsService.java`:

```java
String summaryForAi(String projectGroup);
```

Modify `StatisticsServiceImpl.java`:

```java
@Override
public String summaryForAi(String projectGroup) {
    DashboardStatisticsVO stats = statistics(projectGroup);
    return String.format(
        "总工单数：%d，待认领：%d，进行中：%d，超期工单：%d，今日新增：%d，本周新增：%d，人员总数：%d，设备总数：%d。",
        stats.getTotalWorkOrders(), stats.getPendingWorkOrders(), stats.getInProgressWorkOrders(),
        stats.getOverdueWorkOrders(), stats.getTodayNewWorkOrders(), stats.getWeekNewWorkOrders(),
        stats.getTotalPersonnel(), stats.getTotalDevices()
    );
}
```

- [ ] **Step 8: Add tests**

`HuggingFaceClientTest.java` uses Mockito to mock `RestTemplate`. `AiControllerTest.java` mocks `AiChatService`. Tests verify request body shape and endpoint path.

- [ ] **Step 9: Run backend tests**

```bash
cd backend
set HUGGINGFACE_API_KEY=你的HuggingFaceToken
mvnw.cmd test
```

Expected: PASS

---

## Task 2: 后端 AI 智能派单建议

**Files:**
- Create: `backend/src/main/java/edu/cdut/aiback/service/AiDispatchService.java`
- Create: `backend/src/main/java/edu/cdut/aiback/vo/AiDispatchAdviceVO.java`
- Create: `backend/src/main/java/edu/cdut/aiback/vo/CandidateEngineerVO.java`
- Modify: `backend/src/main/java/edu/cdut/aiback/controller/AiController.java`
- Create: `backend/src/test/java/edu/cdut/aiback/service/AiDispatchServiceTest.java`

**Interfaces:**
- `AiDispatchService.advise(Long workOrderId, String projectGroup)` → `AiDispatchAdviceVO`
- `POST /api/ai/dispatch/advice?workOrderId=...` → `Result<AiDispatchAdviceVO>`

- [ ] **Step 1: Add VO classes**

`CandidateEngineerVO.java`:

```java
package edu.cdut.aiback.vo;

import lombok.Data;

@Data
public class CandidateEngineerVO {
    private Long personnelId;
    private String name;
    private String phone;
    private Double distanceKm;
    private Integer pendingWorkOrders;
    private Double avgResponseMinutes;
    private Integer completedThisWeek;
}
```

`AiDispatchAdviceVO.java`:

```java
package edu.cdut.aiback.vo;

import lombok.Data;

@Data
public class AiDispatchAdviceVO {
    private Long workOrderId;
    private Long personnelId;
    private String name;
    private String reason;
}
```

- [ ] **Step 2: Add mapper/service queries needed**

Modify `PersonnelMapper.java` to add:

```java
@Select("SELECT p.*, " +
        "  (SELECT COUNT(*) FROM work_order w WHERE w.claimer_id = p.id AND w.status IN ('claimed','in_progress','completing','pending_confirm')) AS pending_count, " +
        "  (SELECT AVG(response_duration) FROM work_order w WHERE w.claimer_id = p.id AND w.response_duration IS NOT NULL) AS avg_response, " +
        "  (SELECT COUNT(*) FROM work_order w WHERE w.claimer_id = p.id AND w.status = 'confirmed' AND w.confirm_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)) AS completed_week " +
        "FROM personnel p " +
        "WHERE p.role = '外场' AND p.status = 1 AND p.project_group = #{projectGroup} AND p.latitude IS NOT NULL AND p.longitude IS NOT NULL")
List<Personnel> selectCandidates(@Param("projectGroup") String projectGroup);
```

Add `PersonnelMapper.selectCandidates` result handler or map via `WorkOrderMapper.xml`? Simpler: create a `CandidateEngineerVO` mapper in XML or use `@Results`. Since fields are computed, easier to write XML in `resources/mapper/PersonnelMapper.xml`. Check existing XML. If none, create.

- [ ] **Step 3: Add haversine utility**

```java
package edu.cdut.aiback.util;

public final class GeoUtil {
    private GeoUtil() {}
    public static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
```

- [ ] **Step 4: Add AiDispatchService**

```java
package edu.cdut.aiback.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.cdut.aiback.client.HuggingFaceClient;
import edu.cdut.aiback.common.BizException;
import edu.cdut.aiback.entity.Device;
import edu.cdut.aiback.entity.Personnel;
import edu.cdut.aiback.entity.WorkOrder;
import edu.cdut.aiback.mapper.PersonnelMapper;
import edu.cdut.aiback.util.AiPromptFormatter;
import edu.cdut.aiback.util.GeoUtil;
import edu.cdut.aiback.vo.AiDispatchAdviceVO;
import edu.cdut.aiback.vo.CandidateEngineerVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AiDispatchService {

    private final HuggingFaceClient huggingFaceClient;
    private final WorkOrderService workOrderService;
    private final DeviceService deviceService;
    private final PersonnelMapper personnelMapper;

    public AiDispatchService(HuggingFaceClient huggingFaceClient,
                             WorkOrderService workOrderService,
                             DeviceService deviceService,
                             PersonnelMapper personnelMapper) {
        this.huggingFaceClient = huggingFaceClient;
        this.workOrderService = workOrderService;
        this.deviceService = deviceService;
        this.personnelMapper = personnelMapper;
    }

    public AiDispatchAdviceVO advise(Long workOrderId, String projectGroup) {
        WorkOrder wo = workOrderService.getById(workOrderId);
        if (wo == null) {
            throw new BizException("工单不存在");
        }
        Device device = deviceService.getById(wo.getDeviceId());
        if (device == null || device.getLatitude() == null || device.getLongitude() == null) {
            throw new BizException("工单关联设备缺少坐标");
        }

        List<Personnel> candidates = personnelMapper.selectCandidates(projectGroup);
        if (candidates == null || candidates.isEmpty()) {
            throw new BizException("没有可用的外场工程师");
        }

        String prompt = buildPrompt(wo, device, candidates);
        String answer = huggingFaceClient.generate(prompt);
        return parseAnswer(workOrderId, answer, candidates);
    }

    private String buildPrompt(WorkOrder wo, Device device, List<Personnel> candidates) {
        StringBuilder sb = new StringBuilder();
        sb.append("工单信息：\n");
        sb.append("- 工单编号：").append(wo.getWorkOrderCode()).append("\n");
        sb.append("- 紧急程度：").append(wo.getEmergencyLevel()).append("\n");
        sb.append("- 故障类型：").append(wo.getFaultType()).append("\n");
        sb.append("- 设备位置：纬度 ").append(device.getLatitude())
          .append(", 经度 ").append(device.getLongitude()).append("\n\n");
        sb.append("候选外场工程师（只从下面人选中选择一位）：\n");
        for (Personnel p : candidates) {
            double dist = GeoUtil.distanceKm(device.getLatitude().doubleValue(), device.getLongitude().doubleValue(),
                    p.getLatitude().doubleValue(), p.getLongitude().doubleValue());
            sb.append("- ID=").append(p.getId())
              .append(" 姓名=").append(p.getName())
              .append(" 距离=").append(String.format("%.2f", dist)).append("km")
              .append(" 未完工单=").append(p.getPendingCount() == null ? 0 : p.getPendingCount())
              .append(" 平均响应分钟=").append(p.getAvgResponse() == null ? "无" : String.format("%.1f", p.getAvgResponse()))
              .append(" 本周完成=").append(p.getCompletedWeek() == null ? 0 : p.getCompletedWeek())
              .append("\n");
        }
        sb.append("\n请推荐最合适的一位工程师，输出严格 JSON：{\"personnelId\":数字,\"name\":\"姓名\",\"reason\":\"推荐理由\"}。不要输出任何其他内容。");
        String system = "你是工单调度专家，擅长根据距离、负载、响应速度推荐外场工程师。只输出 JSON。";
        return AiPromptFormatter.qwenPrompt(system, sb.toString());
    }

    private AiDispatchAdviceVO parseAnswer(Long workOrderId, String answer, List<Personnel> candidates) {
        try {
            // extract JSON from possible markdown
            String json = answer.replaceAll(".*\\{", "{").replaceAll("\\}.*", "}");
            org.json.JSONObject obj = new org.json.JSONObject(json);
            Long pid = obj.getLong("personnelId");
            String name = obj.getString("name");
            String reason = obj.getString("reason");
            AiDispatchAdviceVO vo = new AiDispatchAdviceVO();
            vo.setWorkOrderId(workOrderId);
            vo.setPersonnelId(pid);
            vo.setName(name);
            vo.setReason(reason);
            return vo;
        } catch (Exception e) {
            // fallback to first candidate
            Personnel first = candidates.get(0);
            AiDispatchAdviceVO vo = new AiDispatchAdviceVO();
            vo.setWorkOrderId(workOrderId);
            vo.setPersonnelId(first.getId());
            vo.setName(first.getName());
            vo.setReason("模型未返回有效 JSON，已按列表顺序推荐：" + first.getName());
            return vo;
        }
    }
}
```

Note: Add `pendingCount`, `avgResponse`, `completedWeek` fields to `Personnel` entity as `@TableField(exist = false)` or create a wrapper DTO.

- [ ] **Step 5: Add endpoint to AiController**

```java
@PostMapping("/dispatch/advice")
public Result<AiDispatchAdviceVO> dispatchAdvice(@RequestParam Long workOrderId) {
    return Result.ok(aiDispatchService.advise(workOrderId, UserContext.getProjectGroup()));
}
```

- [ ] **Step 6: Run tests**

- [ ] **Step 7: Commit**

---

## Task 3: 后端一键指派工单

**Files:**
- Modify: `backend/src/main/java/edu/cdut/aiback/service/WorkOrderService.java`
- Modify: `backend/src/main/java/edu/cdut/aiback/controller/WorkOrderController.java`
- Create: `backend/src/test/java/edu/cdut/aiback/service/WorkOrderServiceAssignTest.java`

**Interfaces:**
- `WorkOrderService.assign(Long workOrderId, Long personnelId)`
- `PUT /api/work-order/{id}/assign?personnelId=...`

- [ ] **Step 1: Add assign method**

```java
@Transactional
public void assign(Long workOrderId, Long personnelId) {
    WorkOrder wo = getByIdCheck(workOrderId);
    if (!WorkOrderStatus.PUBLISHED.getCode().equals(wo.getStatus())) {
        throw new BizException("只有待认领工单可指派");
    }
    String role = UserContext.getRole();
    if (!UserRole.PROJECT_MANAGER.getCode().equals(role)
            && !UserRole.COMPANY_MANAGER.getCode().equals(role)) {
        throw new BizException("仅项目管理/公司管理人员可指派工单");
    }
    Personnel personnel = personnelService.getById(personnelId);
    if (personnel == null) {
        throw new BizException("人员不存在");
    }
    if (!UserRole.EXTERNAL.getCode().equals(personnel.getRole())) {
        throw new BizException("只能指派给外场工程师");
    }

    wo.setStatus(WorkOrderStatus.CLAIMED.getCode());
    wo.setClaimerId(personnelId);
    wo.setClaimTime(LocalDateTime.now());
    if (wo.getPublishTime() != null) {
        wo.setResponseDuration((int) ChronoUnit.MINUTES.between(wo.getPublishTime(), LocalDateTime.now()));
    }
    updateById(wo);
    addLog(workOrderId, "assign", "AI 指派给 " + personnel.getName());
}
```

- [ ] **Step 2: Inject PersonnelService into WorkOrderService**

Add constructor dependency:

```java
private final PersonnelService personnelService;

public WorkOrderService(WorkOrderLogMapper workOrderLogMapper,
                        WorkOrderCodeGenerator codeGenerator,
                        DeviceService deviceService,
                        PersonnelService personnelService) {
    ...
    this.personnelService = personnelService;
}
```

- [ ] **Step 3: Add controller endpoint**

```java
@PutMapping("/{id}/assign")
public Result<Void> assign(@PathVariable Long id, @RequestParam Long personnelId) {
    workOrderService.assign(id, personnelId);
    return Result.ok();
}
```

- [ ] **Step 4: Run tests and commit**

---

## Task 4: 人员坐标 + 数据库初始化

**Files:**
- Modify: `backend/src/main/java/edu/cdut/aiback/entity/Personnel.java`
- Modify: `backend/docs/sql/init.sql`

- [ ] **Step 1: Add lat/lng and computed fields to Personnel**

```java
private BigDecimal latitude;
private BigDecimal longitude;

@TableField(exist = false)
private Integer pendingCount;
@TableField(exist = false)
private Double avgResponse;
@TableField(exist = false)
private Integer completedWeek;
```

- [ ] **Step 2: Update init.sql to add columns and demo coordinates**

```sql
ALTER TABLE personnel ADD COLUMN latitude DECIMAL(10,7) DEFAULT NULL AFTER project_group;
ALTER TABLE personnel ADD COLUMN longitude DECIMAL(10,7) DEFAULT NULL AFTER latitude;
```

Update INSERT for field personnel to set coordinates near Guangzhou.

- [ ] **Step 3: Run backend tests to ensure schema compatible**

Note: tests use H2; update `schema.sql` if necessary.

---

## Task 5: 后端人脸识别登录

**Files:**
- Modify: `backend/src/main/java/edu/cdut/aiback/service/AuthService.java`
- Modify: `backend/src/main/java/edu/cdut/aiback/controller/AuthController.java`
- Create: `backend/src/test/java/edu/cdut/aiback/service/AuthServiceFaceLoginTest.java`

**Interfaces:**
- `AuthService.faceLogin(String imageBase64)` → `LoginResponse`
- `POST /api/auth/face-login`

- [ ] **Step 1: Add faceLogin method**

```java
public LoginResponse faceLogin(String imageBase64) {
    if (imageBase64 == null || imageBase64.isBlank()) {
        throw new BizException("人脸图片不能为空");
    }
    FaceRecognizeResponse recognize = faceService.recognizeFaceBase64(imageBase64, "gzgd_users");
    if (!recognize.isSuccess()) {
        throw new BizException("人脸识别失败: " + recognize.getErrorMessage());
    }
    String account = recognize.getUserId();
    Personnel personnel = personnelService.getOne(
            new LambdaQueryWrapper<Personnel>().eq(Personnel::getAccount, account));
    if (personnel == null) {
        throw new BizException("未找到匹配账号: " + account);
    }

    String token = jwtUtil.generateToken(
            personnel.getId(), personnel.getAccount(),
            personnel.getProjectGroup(), personnel.getRole());

    LoginResponse resp = new LoginResponse(true, token, "登录成功");
    resp.setUserId(personnel.getId());
    resp.setAccount(personnel.getAccount());
    resp.setRole(personnel.getRole());
    resp.setProjectGroup(personnel.getProjectGroup());
    return resp;
}
```

- [ ] **Step 2: Add recognizeFaceBase64 to FaceService**

```java
public FaceRecognizeResponse recognizeFaceBase64(String imageBase64, String groupIdList) {
    String base64Image = FaceImageUtil.normalizeBase64(imageBase64);
    JSONObject response = client.search(base64Image, IMAGE_TYPE_BASE64, groupIdList, null);
    if (response.optInt("error_code", -1) != 0) {
        return new FaceRecognizeResponse(false, 0, "", "", response.toString());
    }
    JSONObject result = response.optJSONObject("result");
    if (result == null) {
        return new FaceRecognizeResponse(false, 0, "", "", response.toString());
    }
    org.json.JSONArray userList = result.optJSONArray("user_list");
    if (userList == null || userList.length() == 0) {
        return new FaceRecognizeResponse(false, 0, "", "", response.toString());
    }
    JSONObject firstUser = userList.getJSONObject(0);
    double score = firstUser.optDouble("score", 0);
    String userId = firstUser.optString("user_id", "");
    String groupId = firstUser.optString("group_id", "");
    boolean same = isSamePerson(score);
    return new FaceRecognizeResponse(same, score, userId, groupId, response.toString());
}
```

Adjust `FaceRecognizeResponse` to have `isSuccess`, `errorMessage` fields if not exists. Read DTO.

- [ ] **Step 3: Inject FaceService and PersonnelService into AuthService**

- [ ] **Step 4: Add controller endpoint**

```java
@PostMapping("/face-login")
public Result<LoginResponse> faceLogin(@RequestBody Map<String, String> body) {
    return Result.ok(authService.faceLogin(body.get("imageBase64")));
}
```

- [ ] **Step 5: Run tests and commit**

---

## Task 6: 前端 AI 助手聊天组件

**Files:**
- Create: `admin-frontend/src/components/AiChatWidget.vue`
- Create: `admin-frontend/src/api/ai.ts`
- Modify: `admin-frontend/src/layout/index.vue`

- [ ] **Step 1: Add api/ai.ts**

```ts
import request from './request'

export function chat(message: string) {
  return request.post<string>('/api/ai/chat', { message })
}

export function dispatchAdvice(workOrderId: number) {
  return request.post<{ workOrderId: number; personnelId: number; name: string; reason: string }>(
    '/api/ai/dispatch/advice?workOrderId=' + workOrderId
  )
}

export function assignWorkOrder(id: number, personnelId: number) {
  return request.put(`/api/work-order/${id}/assign?personnelId=${personnelId}`)
}
```

- [ ] **Step 2: Add AiChatWidget component**

Use Element Plus components and scoped Apple-style CSS. Support:
- floating action button bottom-right
- expandable chat drawer
- messages array, loading state
- send message, display AI reply

- [ ] **Step 3: Add to layout**

Place `<AiChatWidget />` after router-view in `layout/index.vue`.

- [ ] **Step 4: Run build**

---

## Task 7: 前端 AI 派单建议（工单详情页）

**Files:**
- Modify: `admin-frontend/src/views/workorder/detail.vue`
- Modify: `admin-frontend/src/views/workorder/list.vue`

- [ ] **Step 1: Add "AI 派单建议" button**

In work order detail, if status is `published` and user role is PM/company manager, show button.

- [ ] **Step 2: Call dispatchAdvice, show dialog with recommendation and reason**

- [ ] **Step 3: Add "一键指派" button that calls assignWorkOrder and refreshes detail**

- [ ] **Step 4: Run build**

---

## Task 8: 前端人脸识别登录

**Files:**
- Modify: `admin-frontend/src/views/login/index.vue`
- Create: `admin-frontend/src/components/FaceCapture.vue`
- Modify: `admin-frontend/src/api/auth.ts` or `store/user.ts`

- [ ] **Step 1: Add FaceCapture component**

Capture camera feed and emit `capture(base64)` on button click. Use `<video autoplay>` and `<canvas>`.

- [ ] **Step 2: Add login tabs**

Add two tabs: 验证码登录 / 人脸登录.

- [ ] **Step 3: Add faceLogin action in user store**

```ts
async faceLogin(imageBase64: string) {
  const res = await request.post<LoginResponse>('/api/auth/face-login', { imageBase64 })
  localStorage.setItem('token', res.data.token)
  this.token = res.data.token
  this.userInfo = res.data
}
```

- [ ] **Step 4: Run build**

---

## Task 9: 前端人脸录入（人员管理页）

**Files:**
- Modify: `admin-frontend/src/views/personnel/index.vue`
- Add: use FaceCapture component and call `/api/face/register/base64`

- [ ] **Step 1: Add "录入人脸" button per personnel row**

- [ ] **Step 2: Open dialog with FaceCapture, capture and call register API**

```ts
import request from '@/api/request'

export function registerFace(imageBase64: string, userId: string) {
  return request.post('/api/face/register/base64', { image, groupId: 'gzgd_users', userId })
}
```

- [ ] **Step 3: Run build and commit**

---

## Task 10: 文档、Git 初始化与 GitHub 推送

**Files:**
- Modify: `README.md`
- Modify: `.gitignore`
- Create: `admin-frontend/.env.example`
- Create: `backend/.env.example`

- [ ] **Step 1: Update README**

Add sections:
- 功能特性
- 技术栈
- 环境要求 & 启动方式（双击运行演示.exe）
- 环境变量：HUGGINGFACE_API_KEY、VITE_AMAP_KEY
- 演示账号
- 创新点说明

- [ ] **Step 2: Update .gitignore**

Ensure:
```gitignore
# Secrets
backend/.env
admin-frontend/.env
application-local.yml
```

- [ ] **Step 3: Add .env.example files**

`admin-frontend/.env.example`:
```env
VITE_AMAP_KEY=
```

`backend/.env.example`:
```env
HUGGINGFACE_API_KEY=
```

- [ ] **Step 4: Initialize git and commit**

```bash
git init -b main
git add .
git commit -m "feat: 完成 AI 大模型助手、智能派单建议、人脸识别登录及数据可视化中心"
```

- [ ] **Step 5: Add GitHub remote and push**

User provides repo URL. Run:

```bash
git remote add origin <user-repo-url>
git push -u origin main
```

- [ ] **Step 6: Final full verification**

```bash
cd backend && mvnw.cmd test
cd ../admin-frontend && npm run build
```

Both must pass.
