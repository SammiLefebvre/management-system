package edu.cdut.aiback.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import edu.cdut.aiback.common.UserContext;
import edu.cdut.aiback.entity.Device;
import edu.cdut.aiback.entity.Personnel;
import edu.cdut.aiback.entity.WorkOrder;
import edu.cdut.aiback.mapper.DeviceMapper;
import edu.cdut.aiback.mapper.PersonnelMapper;
import edu.cdut.aiback.mapper.TeamMapper;
import edu.cdut.aiback.mapper.WorkOrderMapper;
import edu.cdut.aiback.service.StatisticsService;
import edu.cdut.aiback.vo.DashboardStatisticsVO;
import edu.cdut.aiback.vo.DeviceMapVO;
import edu.cdut.aiback.vo.HeatmapVO;
import edu.cdut.aiback.vo.PersonnelWorkloadVO;
import edu.cdut.aiback.vo.WorkOrderTrendVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final WorkOrderMapper workOrderMapper;
    private final DeviceMapper deviceMapper;
    private final PersonnelMapper personnelMapper;
    private final TeamMapper teamMapper;

    @Override
    public DashboardStatisticsVO getDashboardStatistics() {
        String projectGroup = UserContext.getProjectGroup();
        DashboardStatisticsVO vo = new DashboardStatisticsVO();

        vo.setWorkOrderTotal(countByProjectGroup(projectGroup));

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        QueryWrapper<WorkOrder> todayWrapper = new QueryWrapper<>();
        todayWrapper.ge("created_at", todayStart).eq("project_group", projectGroup);
        vo.setWorkOrderToday(workOrderMapper.selectCount(todayWrapper));

        Map<String, Long> statusCounts = new LinkedHashMap<>();
        String[] statuses = {
                "published", "claimed", "in_progress", "completing",
                "pending_confirm", "confirmed", "closed", "pending_force_close"
        };
        for (String status : statuses) {
            QueryWrapper<WorkOrder> sw = new QueryWrapper<>();
            sw.eq("status", status).eq("project_group", projectGroup);
            statusCounts.put(status, workOrderMapper.selectCount(sw));
        }
        vo.setStatusCounts(statusCounts);

        vo.setSlaOverdueCount(countSlaOverdue(projectGroup));

        QueryWrapper<Device> deviceWrapper = new QueryWrapper<>();
        deviceWrapper.eq("project_group", projectGroup);
        vo.setDeviceTotal(deviceMapper.selectCount(deviceWrapper));

        QueryWrapper<Personnel> personnelWrapper = new QueryWrapper<>();
        personnelWrapper.eq("project_group", projectGroup);
        vo.setPersonnelTotal(personnelMapper.selectCount(personnelWrapper));

        vo.setTeamTotal(teamMapper.selectCount(null));

        vo.setLast7Days(buildLast7DaysTrend(projectGroup));

        return vo;
    }

    @Override
    public WorkOrderTrendVO getTrends(int days, String projectGroup) {
        WorkOrderTrendVO vo = new WorkOrderTrendVO();
        List<String> dates = new ArrayList<>();
        List<Long> created = new ArrayList<>();
        List<Long> completed = new ArrayList<>();
        List<Long> overdue = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        LocalDateTime now = LocalDateTime.now();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            dates.add(date.format(fmt));
            created.add(countByDate(date, "created_at", projectGroup));
            completed.add(countByDate(date, "complete_time", projectGroup));
            overdue.add(countOverdueOnDate(date, projectGroup, now));
        }
        vo.setDates(dates);
        vo.setSeries(List.of(
                new WorkOrderTrendVO.TrendSeries("新增工单", created),
                new WorkOrderTrendVO.TrendSeries("完成工单", completed),
                new WorkOrderTrendVO.TrendSeries("超期工单", overdue)
        ));
        return vo;
    }

    @Override
    public HeatmapVO getHeatmap(String projectGroup) {
        List<Map<String, Object>> rows = workOrderMapper.selectFaultHeatmap(projectGroup);
        Set<String> xSet = new LinkedHashSet<>();
        Set<String> ySet = new LinkedHashSet<>();
        rows.forEach(r -> {
            xSet.add((String) r.get("faultType"));
            ySet.add((String) r.get("area"));
        });
        List<String> xAxis = new ArrayList<>(xSet);
        List<String> yAxis = new ArrayList<>(ySet);
        List<int[]> data = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            int x = xAxis.indexOf(r.get("faultType"));
            int y = yAxis.indexOf(r.get("area"));
            int c = ((Number) r.get("cnt")).intValue();
            data.add(new int[]{x, y, c});
        }
        HeatmapVO vo = new HeatmapVO();
        vo.setXAxis(xAxis);
        vo.setYAxis(yAxis);
        vo.setData(data);
        return vo;
    }

    @Override
    public List<PersonnelWorkloadVO> getWorkload(String projectGroup) {
        LocalDateTime weekStart = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
        List<Map<String, Object>> rows = personnelMapper.selectWorkload(projectGroup, weekStart);
        List<PersonnelWorkloadVO> list = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            PersonnelWorkloadVO vo = new PersonnelWorkloadVO();
            vo.setPersonnelId(((Number) r.get("personnelId")).longValue());
            vo.setName((String) r.get("name"));
            vo.setRole((String) r.get("role"));
            vo.setPendingCount(((Number) r.get("pendingCount")).longValue());
            vo.setCompletedThisWeek(((Number) r.get("completedThisWeek")).longValue());
            Number avg = (Number) r.get("avgResponseMinutes");
            vo.setAvgResponseMinutes(avg == null ? 0.0 : avg.doubleValue());
            list.add(vo);
        }
        return list;
    }

    @Override
    public List<DeviceMapVO> getDevicesWithLocation(String projectGroup) {
        return deviceMapper.selectDevicesWithLocation(projectGroup);
    }

    private long countByProjectGroup(String projectGroup) {
        QueryWrapper<WorkOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("project_group", projectGroup);
        return workOrderMapper.selectCount(wrapper);
    }

    private long countByDate(LocalDate date, String column, String projectGroup) {
        QueryWrapper<WorkOrder> wrapper = new QueryWrapper<>();
        wrapper.apply("DATE(" + column + ") = {0}", date.toString());
        wrapper.eq("project_group", projectGroup);
        return workOrderMapper.selectCount(wrapper);
    }

    private long countOverdueOnDate(LocalDate date, String projectGroup, LocalDateTime now) {
        QueryWrapper<WorkOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "published");
        wrapper.eq("project_group", projectGroup);
        wrapper.apply("DATE(publish_time) = {0}", date.toString());
        LocalDateTime threshold = now.minusMinutes(60);
        wrapper.lt("publish_time", threshold);
        return workOrderMapper.selectCount(wrapper);
    }

    private Long countSlaOverdue(String projectGroup) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusMinutes(60);
        QueryWrapper<WorkOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "published").eq("project_group", projectGroup).lt("created_at", oneHourAgo);
        return workOrderMapper.selectCount(wrapper);
    }

    private List<DashboardStatisticsVO.DailyTrend> buildLast7DaysTrend(String projectGroup) {
        List<DashboardStatisticsVO.DailyTrend> list = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateStr = date.format(fmt);

            QueryWrapper<WorkOrder> createdWrapper = new QueryWrapper<>();
            createdWrapper.apply("DATE(created_at) = {0}", dateStr).eq("project_group", projectGroup);
            long created = workOrderMapper.selectCount(createdWrapper);

            QueryWrapper<WorkOrder> completedWrapper = new QueryWrapper<>();
            completedWrapper.apply("DATE(complete_time) = {0}", dateStr).eq("project_group", projectGroup);
            long completed = workOrderMapper.selectCount(completedWrapper);

            DashboardStatisticsVO.DailyTrend trend = new DashboardStatisticsVO.DailyTrend();
            trend.setDate(dateStr);
            trend.setCreated(created);
            trend.setCompleted(completed);
            list.add(trend);
        }
        return list;
    }

    @Override
    public String summaryForAi() {
        String projectGroup = UserContext.getProjectGroup();
        if (projectGroup == null) {
            projectGroup = "未知项目组";
        }
        DashboardStatisticsVO stats = getDashboardStatistics();
        long pending = stats.getStatusCounts().getOrDefault("published", 0L);
        long inProgress = stats.getStatusCounts().getOrDefault("claimed", 0L)
                + stats.getStatusCounts().getOrDefault("in_progress", 0L)
                + stats.getStatusCounts().getOrDefault("completing", 0L)
                + stats.getStatusCounts().getOrDefault("pending_confirm", 0L);
        return String.format(
                "项目组：%s。总工单数：%d，今日新增：%d，待认领：%d，进行中：%d，超期工单：%d，设备总数：%d，人员总数：%d。",
                projectGroup, stats.getWorkOrderTotal(), stats.getWorkOrderToday(),
                pending, inProgress, stats.getSlaOverdueCount(),
                stats.getDeviceTotal(), stats.getPersonnelTotal()
        );
    }
}
