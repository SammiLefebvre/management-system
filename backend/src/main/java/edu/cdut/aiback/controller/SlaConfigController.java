package edu.cdut.aiback.controller;

import edu.cdut.aiback.common.Result;
import edu.cdut.aiback.entity.SlaConfig;
import edu.cdut.aiback.service.SlaConfigService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sla-config")
public class SlaConfigController {

    private final SlaConfigService slaConfigService;

    public SlaConfigController(SlaConfigService slaConfigService) {
        this.slaConfigService = slaConfigService;
    }

    /**
     * 获取全部 SLA 配置
     */
    @GetMapping
    public Result<List<SlaConfig>> listAll() {
        return Result.ok(slaConfigService.listAll());
    }

    /**
     * 修改 SLA 配置
     */
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody SlaConfig config) {
        config.setId(id);
        return Result.ok(slaConfigService.updateById(config));
    }
}
