package edu.cdut.aiback.service;

import edu.cdut.aiback.client.HuggingFaceClient;
import edu.cdut.aiback.common.BizException;
import edu.cdut.aiback.common.UserContext;
import edu.cdut.aiback.entity.Device;
import edu.cdut.aiback.entity.Personnel;
import edu.cdut.aiback.entity.WorkOrder;
import edu.cdut.aiback.mapper.PersonnelMapper;
import edu.cdut.aiback.util.AiPromptFormatter;
import edu.cdut.aiback.util.GeoUtil;
import edu.cdut.aiback.vo.AiDispatchAdviceVO;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    public AiDispatchAdviceVO advise(Long workOrderId, String apiKey) {
        WorkOrder wo = workOrderService.getById(workOrderId);
        if (wo == null) {
            throw new BizException("工单不存在");
        }
        Device device = deviceService.getById(wo.getDeviceId());
        if (device == null || device.getLatitude() == null || device.getLongitude() == null) {
            throw new BizException("工单关联设备缺少坐标");
        }

        String projectGroup = UserContext.getProjectGroup();
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
        List<Personnel> candidates = personnelMapper.selectCandidates(projectGroup, weekStart);
        if (candidates == null || candidates.isEmpty()) {
            throw new BizException("没有可用的外场工程师");
        }

        String prompt = buildPrompt(wo, device, candidates);
        String answer = huggingFaceClient.generate(prompt, apiKey);
        return parseAnswer(workOrderId, answer, candidates, device);
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

    private AiDispatchAdviceVO parseAnswer(Long workOrderId, String answer, List<Personnel> candidates, Device device) {
        try {
            String json = extractJson(answer);
            JSONObject obj = new JSONObject(json);
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
            Personnel first = candidates.get(0);
            AiDispatchAdviceVO vo = new AiDispatchAdviceVO();
            vo.setWorkOrderId(workOrderId);
            vo.setPersonnelId(first.getId());
            vo.setName(first.getName());
            vo.setReason("模型未返回有效 JSON，已按列表顺序推荐：" + first.getName());
            return vo;
        }
    }

    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }
}
