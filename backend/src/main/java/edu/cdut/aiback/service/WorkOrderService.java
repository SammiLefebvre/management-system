package edu.cdut.aiback.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.cdut.aiback.common.BizException;
import edu.cdut.aiback.common.UserContext;
import edu.cdut.aiback.dto.WorkOrderCreateDTO;
import edu.cdut.aiback.dto.WorkOrderProcessDTO;
import edu.cdut.aiback.dto.WorkOrderQueryDTO;
import edu.cdut.aiback.entity.*;
import edu.cdut.aiback.enums.WorkOrderStatus;
import edu.cdut.aiback.enums.UserRole;
import edu.cdut.aiback.mapper.WorkOrderLogMapper;
import edu.cdut.aiback.mapper.WorkOrderMapper;
import edu.cdut.aiback.util.ImageWatermarkUtil;
import edu.cdut.aiback.util.WorkOrderCodeGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class WorkOrderService extends ServiceImpl<WorkOrderMapper, WorkOrder> {

    private final WorkOrderLogMapper workOrderLogMapper;
    private final WorkOrderCodeGenerator codeGenerator;
    private final DeviceService deviceService;
    private final PersonnelService personnelService;

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    public WorkOrderService(WorkOrderLogMapper workOrderLogMapper,
                            WorkOrderCodeGenerator codeGenerator,
                            DeviceService deviceService,
                            PersonnelService personnelService) {
        this.workOrderLogMapper = workOrderLogMapper;
        this.codeGenerator = codeGenerator;
        this.deviceService = deviceService;
        this.personnelService = personnelService;
    }

    /**
     * 创建工单（草稿或立即发布）
     */
    @Transactional
    public WorkOrder create(WorkOrderCreateDTO dto) {
        Device device = deviceService.getById(dto.getDeviceId());
        if (device == null) {
            throw new BizException("设备不存在");
        }

        WorkOrder wo = new WorkOrder();
        BeanUtil.copyProperties(dto, wo);
        wo.setProjectGroup(UserContext.getProjectGroup());
        wo.setIsPriority(0);

        if (Boolean.TRUE.equals(dto.getPublishNow())) {
            // 立即发布
            String code;
            try {
                code = codeGenerator.generate(dto.getWorkOrderType(), UserContext.getProjectGroup());
            } catch (Exception e) {
                throw new BizException("工单编号生成失败: " + e.getMessage());
            }
            wo.setWorkOrderCode(code);
            wo.setStatus(WorkOrderStatus.PUBLISHED.getCode());
            wo.setPublisherId(UserContext.getUserId());
            wo.setPublishTime(LocalDateTime.now());
        } else {
            // 保存草稿
            wo.setStatus(WorkOrderStatus.DRAFT.getCode());
        }

        save(wo);

        // 记录日志
        if (Boolean.TRUE.equals(dto.getPublishNow())) {
            addLog(wo.getId(), "publish", "发布工单");
        } else {
            addLog(wo.getId(), "draft", "保存草稿");
        }

        return wo;
    }

    /**
     * 草稿发布
     */
    @Transactional
    public void publish(Long id) {
        WorkOrder wo = getByIdCheck(id);
        if (!WorkOrderStatus.DRAFT.getCode().equals(wo.getStatus())) {
            throw new BizException("只有草稿状态可发布");
        }

        String code;
        try {
            code = codeGenerator.generate(wo.getWorkOrderType(), wo.getProjectGroup());
        } catch (Exception e) {
            throw new BizException("工单编号生成失败");
        }

        wo.setWorkOrderCode(code);
        wo.setStatus(WorkOrderStatus.PUBLISHED.getCode());
        wo.setPublisherId(UserContext.getUserId());
        wo.setPublishTime(LocalDateTime.now());
        updateById(wo);

        addLog(id, "publish", "发布工单");
    }

    /**
     * 认领工单
     */
    @Transactional
    public void claim(Long id) {
        WorkOrder wo = getByIdCheck(id);
        if (!WorkOrderStatus.PUBLISHED.getCode().equals(wo.getStatus())) {
            throw new BizException("当前状态不可认领");
        }
        String role = UserContext.getRole();
        if (!UserRole.EXTERNAL.getCode().equals(role)) {
            throw new BizException("仅外场工程师可认领工单");
        }

        wo.setStatus(WorkOrderStatus.CLAIMED.getCode());
        wo.setClaimerId(UserContext.getUserId());
        wo.setClaimTime(LocalDateTime.now());

        // 计算响应时长
        if (wo.getPublishTime() != null) {
            wo.setResponseDuration((int) ChronoUnit.MINUTES.between(wo.getPublishTime(), LocalDateTime.now()));
        }
        updateById(wo);

        addLog(id, "claim", "认领工单");
    }

    /**
     * 指派工单（管理端）
     */
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

    /**
     * 取消认领
     */
    @Transactional
    public void cancelClaim(Long id) {
        WorkOrder wo = getByIdCheck(id);
        if (!WorkOrderStatus.CLAIMED.getCode().equals(wo.getStatus())) {
            throw new BizException("当前状态不可取消认领");
        }
        if (!wo.getClaimerId().equals(UserContext.getUserId())) {
            throw new BizException("仅认领人本人可取消认领");
        }

        wo.setStatus(WorkOrderStatus.PUBLISHED.getCode());
        wo.setClaimerId(null);
        wo.setClaimTime(null);
        wo.setResponseDuration(null);
        updateById(wo);

        addLog(id, "cancel_claim", "取消认领");
    }

    /**
     * 签到（开始作业）
     */
    @Transactional
    public void checkin(Long id, WorkOrderProcessDTO dto) {
        WorkOrder wo = getByIdCheck(id);
        if (!WorkOrderStatus.CLAIMED.getCode().equals(wo.getStatus())) {
            throw new BizException("当前状态不可签到");
        }
        if (dto.getCheckinPhotos() == null || dto.getCheckinPhotos().isEmpty()) {
            throw new BizException("签到照片不能为空");
        }

        wo.setStatus(WorkOrderStatus.IN_PROGRESS.getCode());
        if (dto.getCheckinLat() != null) {
            wo.setCheckinLat(new java.math.BigDecimal(dto.getCheckinLat()));
        }
        if (dto.getCheckinLng() != null) {
            wo.setCheckinLng(new java.math.BigDecimal(dto.getCheckinLng()));
        }
        wo.setCheckinTime(LocalDateTime.now());
        wo.setCheckinPhotos(JSONUtil.toJsonStr(dto.getCheckinPhotos()));
        updateById(wo);

        addLog(id, "start", "签到 - 开始作业");
    }

    /**
     * 提交排查过程
     */
    @Transactional
    public void submitProcess(Long id, WorkOrderProcessDTO dto) {
        WorkOrder wo = getByIdCheck(id);
        if (!WorkOrderStatus.IN_PROGRESS.getCode().equals(wo.getStatus())) {
            throw new BizException("当前状态不可提交排查");
        }
        if (dto.getProcessPhotos() == null || dto.getProcessPhotos().isEmpty()) {
            throw new BizException("排查照片至少需要 1 张");
        }

        wo.setStatus(WorkOrderStatus.COMPLETING.getCode());
        wo.setProcessDesc(dto.getProcessDesc());
        wo.setProcessPhotos(JSONUtil.toJsonStr(dto.getProcessPhotos()));
        wo.setInProgressTime(LocalDateTime.now());
        updateById(wo);

        addLog(id, "in_progress", "提交排查过程");
    }

    /**
     * 提交完工
     */
    @Transactional
    public void submitComplete(Long id, WorkOrderProcessDTO dto) {
        WorkOrder wo = getByIdCheck(id);
        if (!WorkOrderStatus.COMPLETING.getCode().equals(wo.getStatus())) {
            throw new BizException("当前状态不可提交完工");
        }
        if (dto.getEndPhotos() == null || dto.getEndPhotos().isEmpty()) {
            throw new BizException("结束照片不能为空");
        }
        if (StrUtil.isBlank(dto.getRepairResult())) {
            throw new BizException("请选择维修结果");
        }

        // 对结束照片叠加地点+时间水印（已有工具类此前未接线）
        String location = resolveLocationText(wo);
        List<String> watermarkedPhotos = new ArrayList<>();
        for (String url : dto.getEndPhotos()) {
            watermarkedPhotos.add(applyEndPhotoWatermark(url, location));
        }

        wo.setStatus("pending_confirm");
        wo.setCompleteTime(LocalDateTime.now());
        wo.setEndPhotos(JSONUtil.toJsonStr(watermarkedPhotos));
        wo.setRepairResult(dto.getRepairResult());
        wo.setFaultDescription(dto.getFaultDescription());
        wo.setSpecialRequirements(dto.getSpecialRequirements());
        wo.setReplacedParts(dto.getReplacedParts());
        wo.setRepairerInfo(dto.getRepairerInfo());
        updateById(wo);

        addLog(id, "complete", "提交完工 - " + ("fixed".equals(dto.getRepairResult()) ? "已修复" : "未修复"));
    }

    private String resolveLocationText(WorkOrder wo) {
        if (wo.getDeviceId() != null) {
            try {
                Device device = deviceService.getById(wo.getDeviceId());
                if (device != null && StrUtil.isNotBlank(device.getDeviceName())) {
                    return device.getDeviceName();
                }
            } catch (Exception e) {
                log.warn("读取设备名称失败: {}", e.getMessage());
            }
        }
        return "现场";
    }

    /**
     * 将 /uploads/... URL 转为本地文件并加水印；失败时回退原 URL，避免阻断完工。
     */
    private String applyEndPhotoWatermark(String photoUrl, String location) {
        if (StrUtil.isBlank(photoUrl)) {
            return photoUrl;
        }
        try {
            String pathPart = photoUrl;
            int uploadsIdx = photoUrl.indexOf("/uploads/");
            if (uploadsIdx >= 0) {
                pathPart = photoUrl.substring(uploadsIdx);
            } else if (photoUrl.startsWith("http")) {
                return photoUrl;
            }
            String relative = pathPart.startsWith("/uploads/")
                    ? pathPart.substring("/uploads/".length())
                    : pathPart.replaceFirst("^/+", "");
            String sourcePath = uploadPath + File.separator + relative.replace("/", File.separator);
            if (!cn.hutool.core.io.FileUtil.exist(sourcePath)) {
                log.warn("完工照片本地文件不存在，跳过水印: {}", sourcePath);
                return pathPart.startsWith("/") ? pathPart : "/uploads/" + relative;
            }
            return ImageWatermarkUtil.addWatermark(sourcePath, location, uploadPath);
        } catch (Exception e) {
            log.warn("完工照片水印失败，使用原图: {}", e.getMessage());
            return photoUrl;
        }
    }

    /**
     * 内场确认完成
     */
    @Transactional
    public void confirm(Long id) {
        WorkOrder wo = getByIdCheck(id);
        if (!"pending_confirm".equals(wo.getStatus()) && !WorkOrderStatus.COMPLETING.getCode().equals(wo.getStatus())) {
            throw new BizException("当前状态不可确认");
        }
        String role = UserContext.getRole();
        if (!UserRole.INTERNAL.getCode().equals(role)
                && !UserRole.PROJECT_MANAGER.getCode().equals(role)
                && !UserRole.COMPANY_MANAGER.getCode().equals(role)) {
            throw new BizException("仅内场/管理人员可确认工单");
        }

        wo.setStatus(WorkOrderStatus.CONFIRMED.getCode());
        wo.setConfirmTime(LocalDateTime.now());

        // 计算修复时长
        if (wo.getClaimTime() != null) {
            wo.setRepairDuration((int) ChronoUnit.MINUTES.between(wo.getClaimTime(), LocalDateTime.now()));
        }
        updateById(wo);

        addLog(id, "confirm", "确认完成");
    }

    /**
     * 强制关闭 - 发起
     */
    @Transactional
    public void forceClose(Long id, String reason) {
        WorkOrder wo = getByIdCheck(id);
        String role = UserContext.getRole();
        if (!UserRole.COMPANY_MANAGER.getCode().equals(role)
                && !UserRole.PROJECT_MANAGER.getCode().equals(role)) {
            throw new BizException("仅公司管理/项目管理人员可发起强制关闭");
        }
        if (!WorkOrderStatus.fromCode(wo.getStatus()).canForceClose()) {
            throw new BizException("当前状态不可强制关闭");
        }

        wo.setStatus(WorkOrderStatus.PENDING_FORCE_CLOSE.getCode());
        wo.setForcedCloseReason(reason);
        wo.setForcedCloseBy(UserContext.getUserId());
        wo.setForcedCloseTime(LocalDateTime.now());
        updateById(wo);

        addLog(id, "force_close", "发起强制关闭: " + reason);
    }

    /**
     * 强制关闭 - 确认
     */
    @Transactional
    public void confirmForceClose(Long id) {
        WorkOrder wo = getByIdCheck(id);
        if (!WorkOrderStatus.PENDING_FORCE_CLOSE.getCode().equals(wo.getStatus())) {
            throw new BizException("当前状态不可确认强制关闭");
        }
        String role = UserContext.getRole();
        if (!UserRole.INTERNAL.getCode().equals(role)) {
            throw new BizException("仅内场人员可确认强制关闭");
        }

        wo.setStatus(WorkOrderStatus.CLOSED.getCode());
        wo.setForcedConfirmBy(UserContext.getUserId());
        wo.setForcedConfirmTime(LocalDateTime.now());
        updateById(wo);

        addLog(id, "confirm_force_close", "确认强制关闭");
    }

    /**
     * 置顶/取消置顶
     */
    @Transactional
    public void togglePriority(Long id) {
        WorkOrder wo = getByIdCheck(id);
        wo.setIsPriority(wo.getIsPriority() == 1 ? 0 : 1);
        updateById(wo);
        addLog(id, "priority", wo.getIsPriority() == 1 ? "置顶" : "取消置顶");
    }

    /**
     * 分页查询
     */
    public Page<WorkOrder> page(WorkOrderQueryDTO query) {
        LambdaQueryWrapper<WorkOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(query.getFaultType()), WorkOrder::getFaultType, query.getFaultType())
                .eq(StrUtil.isNotBlank(query.getEmergencyLevel()), WorkOrder::getEmergencyLevel, query.getEmergencyLevel())
                .eq(query.getClaimerId() != null, WorkOrder::getClaimerId, query.getClaimerId());

        // 多选状态
        if (StrUtil.isNotBlank(query.getStatus())) {
            List<String> statusList = Arrays.asList(query.getStatus().split(","));
            wrapper.in(WorkOrder::getStatus, statusList);
        }

        // 日期范围
        if (StrUtil.isNotBlank(query.getStartDate())) {
            wrapper.ge(WorkOrder::getPublishTime, LocalDateTime.parse(query.getStartDate() + "T00:00:00"));
        }
        if (StrUtil.isNotBlank(query.getEndDate())) {
            wrapper.le(WorkOrder::getPublishTime, LocalDateTime.parse(query.getEndDate() + "T23:59:59"));
        }

        // 排序: 置顶 → 紧急程度 → 发布时间
        wrapper.orderByDesc(WorkOrder::getIsPriority)
                .orderByAsc(WorkOrder::getEmergencyLevel)
                .orderByDesc(WorkOrder::getPublishTime);

        return page(new Page<>(query.getPage(), query.getSize()), wrapper);
    }

    /**
     * 获取工单时间线日志
     */
    public List<WorkOrderLog> getLogs(Long workOrderId) {
        LambdaQueryWrapper<WorkOrderLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkOrderLog::getWorkOrderId, workOrderId)
                .orderByAsc(WorkOrderLog::getActionTime);
        return workOrderLogMapper.selectList(wrapper);
    }

    // ===== 内部方法 =====

    private WorkOrder getByIdCheck(Long id) {
        WorkOrder wo = getById(id);
        if (wo == null) {
            throw new BizException("工单不存在");
        }
        return wo;
    }

    private void addLog(Long workOrderId, String action, String remark) {
        WorkOrderLog log = new WorkOrderLog();
        log.setWorkOrderId(workOrderId);
        log.setOperatorId(UserContext.getUserId());
        log.setOperatorName(UserContext.get() != null ? UserContext.get().getAccount() : "系统");
        log.setAction(action);
        log.setActionTime(LocalDateTime.now());
        log.setRemark(remark);
        workOrderLogMapper.insert(log);
    }
}
