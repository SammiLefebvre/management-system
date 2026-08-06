package edu.cdut.aiback.controller;

import edu.cdut.aiback.common.Result;
import edu.cdut.aiback.common.UserContext;
import edu.cdut.aiback.service.StatisticsService;
import edu.cdut.aiback.vo.DashboardStatisticsVO;
import edu.cdut.aiback.vo.DeviceMapVO;
import edu.cdut.aiback.vo.HeatmapVO;
import edu.cdut.aiback.vo.PersonnelWorkloadVO;
import edu.cdut.aiback.vo.WorkOrderTrendVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/dashboard")
    public Result<DashboardStatisticsVO> dashboard() {
        return Result.ok(statisticsService.getDashboardStatistics());
    }

    @GetMapping("/trends")
    public Result<WorkOrderTrendVO> trends(@RequestParam(defaultValue = "30") int days) {
        return Result.ok(statisticsService.getTrends(days, UserContext.getProjectGroup()));
    }

    @GetMapping("/heatmap")
    public Result<HeatmapVO> heatmap() {
        return Result.ok(statisticsService.getHeatmap(UserContext.getProjectGroup()));
    }

    @GetMapping("/workload")
    public Result<List<PersonnelWorkloadVO>> workload() {
        return Result.ok(statisticsService.getWorkload(UserContext.getProjectGroup()));
    }

    @GetMapping("/devices-with-location")
    public Result<List<DeviceMapVO>> devicesWithLocation() {
        return Result.ok(statisticsService.getDevicesWithLocation(UserContext.getProjectGroup()));
    }
}
