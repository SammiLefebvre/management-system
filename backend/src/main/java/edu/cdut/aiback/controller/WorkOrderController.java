package edu.cdut.aiback.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.cdut.aiback.common.Result;
import edu.cdut.aiback.common.UserContext;
import edu.cdut.aiback.dto.WorkOrderCreateDTO;
import edu.cdut.aiback.dto.WorkOrderProcessDTO;
import edu.cdut.aiback.dto.WorkOrderQueryDTO;
import edu.cdut.aiback.entity.WorkOrder;
import edu.cdut.aiback.entity.WorkOrderLog;
import edu.cdut.aiback.service.WorkOrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/work-order")
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    public WorkOrderController(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    /**
     * 创建工单（草稿/发布）
     */
    @PostMapping
    public Result<WorkOrder> create(@Valid @RequestBody WorkOrderCreateDTO dto) {
        return Result.ok(workOrderService.create(dto));
    }

    /**
     * 草稿发布
     */
    @PutMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        workOrderService.publish(id);
        return Result.ok();
    }

    /**
     * 分页列表
     */
    @GetMapping("/page")
    public Result<Page<WorkOrder>> page(WorkOrderQueryDTO query) {
        return Result.ok(workOrderService.page(query));
    }

    /**
     * 工单详情
     */
    @GetMapping("/{id}")
    public Result<WorkOrder> detail(@PathVariable Long id) {
        return Result.ok(workOrderService.getById(id));
    }

    /**
     * 工单时间线日志
     */
    @GetMapping("/{id}/logs")
    public Result<List<WorkOrderLog>> logs(@PathVariable Long id) {
        return Result.ok(workOrderService.getLogs(id));
    }

    /**
     * 认领工单
     */
    @PutMapping("/{id}/claim")
    public Result<Void> claim(@PathVariable Long id) {
        workOrderService.claim(id);
        return Result.ok();
    }

    /**
     * 指派工单（管理端）
     */
    @PutMapping("/{id}/assign")
    public Result<Void> assign(@PathVariable Long id, @RequestParam Long personnelId) {
        workOrderService.assign(id, personnelId);
        return Result.ok();
    }

    /**
     * 取消认领
     */
    @PutMapping("/{id}/cancel-claim")
    public Result<Void> cancelClaim(@PathVariable Long id) {
        workOrderService.cancelClaim(id);
        return Result.ok();
    }

    /**
     * 签到（开始作业）
     */
    @PutMapping("/{id}/checkin")
    public Result<Void> checkin(@PathVariable Long id, @RequestBody WorkOrderProcessDTO dto) {
        workOrderService.checkin(id, dto);
        return Result.ok();
    }

    /**
     * 提交排查过程
     */
    @PutMapping("/{id}/process")
    public Result<Void> submitProcess(@PathVariable Long id, @RequestBody WorkOrderProcessDTO dto) {
        workOrderService.submitProcess(id, dto);
        return Result.ok();
    }

    /**
     * 提交完工
     */
    @PutMapping("/{id}/complete")
    public Result<Void> submitComplete(@PathVariable Long id, @RequestBody WorkOrderProcessDTO dto) {
        workOrderService.submitComplete(id, dto);
        return Result.ok();
    }

    /**
     * 内场确认完成
     */
    @PutMapping("/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id) {
        workOrderService.confirm(id);
        return Result.ok();
    }

    /**
     * 置顶/取消置顶
     */
    @PutMapping("/{id}/toggle-priority")
    public Result<Void> togglePriority(@PathVariable Long id) {
        workOrderService.togglePriority(id);
        return Result.ok();
    }

    /**
     * 发起强制关闭
     */
    @PutMapping("/{id}/force-close")
    public Result<Void> forceClose(@PathVariable Long id, @RequestParam String reason) {
        workOrderService.forceClose(id, reason);
        return Result.ok();
    }

    /**
     * 确认强制关闭
     */
    @PutMapping("/{id}/confirm-force-close")
    public Result<Void> confirmForceClose(@PathVariable Long id) {
        workOrderService.confirmForceClose(id);
        return Result.ok();
    }
}
