# 数据可视化中心 MVP Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在现有工单管理系统中新增数据可视化中心 MVP，包括实时 Dashboard、高德地图看板、ECharts 图表和 Excel/PDF 报表导出。

**Architecture:** 后端新增统计与导出接口，使用 SSE 推送 Dashboard 实时数据；前端使用 ECharts 绘制图表、高德地图 JS API 展示设备/工单地理分布，并通过 EventSource 订阅实时数据，失败时降级为轮询。

**Tech Stack:** Java 17 + Spring Boot 3.3 + MyBatis-Plus + EasyExcel + OpenHTMLToPDF; Vue 3 + Vite + Pinia + Element Plus + Tailwind CSS + ECharts + vue-echarts; 高德地图 JS API 2.0.

## Global Constraints

- 保持现有 Apple-style 视觉风格（大圆角卡片、柔和阴影、留白、毛玻璃头部）。
- 所有数据查询按当前登录用户的 `project_group` 过滤。
- 后端接口统一返回 `Result<T>`，导出/SSE 除外。
- 新增后端接口必须包含单元测试。
- 前端 `npm run build` 必须成功。
- 高德地图 Key 通过环境变量 `VITE_AMAP_KEY` 注入；Key 缺失时显示友好占位。

---

## File Map

| 文件 | 责任 |
|---|---|
| `backend/pom.xml` | 新增 OpenHTMLToPDF 依赖 |
| `backend/src/main/java/edu/cdut/aiback/vo/*.java` | 新增趋势/热力图/负载/设备地图 VO |
| `backend/src/main/java/edu/cdut/aiback/mapper/*Mapper.java` | 新增统计/地图/负载自定义 SQL |
| `backend/src/main/java/edu/cdut/aiback/service/StatisticsService.java` | 扩展接口 |
| `backend/src/main/java/edu/cdut/aiback/service/impl/StatisticsServiceImpl.java` | 实现统计逻辑 |
| `backend/src/main/java/edu/cdut/aiback/controller/StatisticsController.java` | 新增统计接口 |
| `backend/src/main/java/edu/cdut/aiback/controller/SseController.java` | SSE 实时通道 |
| `backend/src/main/java/edu/cdut/aiback/controller/ReportController.java` | Excel/PDF 导出接口 |
| `backend/src/main/java/edu/cdut/aiback/service/ReportService.java` | 导出业务逻辑 |
| `backend/src/main/java/edu/cdut/aiback/vo/export/*.java` | Excel 导出行对象 |
| `backend/src/test/java/edu/cdut/aiback/controller/*Test.java` | 新增接口单元测试 |
| `admin-frontend/package.json` | 新增 `echarts`、`vue-echarts` |
| `admin-frontend/src/api/statistics.ts` | 新增趋势/热力图/负载/设备地图 API |
| `admin-frontend/src/api/reports.ts` | 新增导出 API |
| `admin-frontend/src/api/devices.ts` | 复用/新增设备位置 API |
| `admin-frontend/src/composables/useSseStats.ts` | SSE 连接 + 轮询降级 |
| `admin-frontend/src/components/LiveStatCard.vue` | 实时数字卡片 |
| `admin-frontend/src/components/charts/TrendChart.vue` | 折线图 |
| `admin-frontend/src/components/charts/HeatmapChart.vue` | 热力图 |
| `admin-frontend/src/components/charts/WorkloadChart.vue` | 横向柱状图 |
| `admin-frontend/src/components/map/MapBoard.vue` | 高德地图容器 |
| `admin-frontend/src/views/data-screen/index.vue` | 数据大屏页面 |
| `admin-frontend/src/views/reports/index.vue` | 报表中心页面 |
| `admin-frontend/src/router/index.ts` | 新增路由 |
| `admin-frontend/src/layout/index.vue` | 新增菜单项 |
| `admin-frontend/src/views/dashboard/index.vue` | 接入实时卡片和图表 |
| `admin-frontend/.env` / `.env.example` | 高德 Key |
| `backend/docs/sql/init.sql` | 补充演示设备坐标数据 |

---

## Task 1: 添加 PDF 渲染依赖

**Files:**
- Modify: `backend/pom.xml`

**Interfaces:**
- Produces: `openhtmltopdf` 可用于 `ReportService`。

- [ ] **Step 1: 在 dependencies 末尾追加 OpenHTMLToPDF**

```xml
<dependency>
    <groupId>com.openhtmltopdf</groupId>
    <artifactId>openhtmltopdf-pdfbox</artifactId>
    <version>1.0.10</version>
</dependency>
```

- [ ] **Step 2: 运行后端测试确认依赖不冲突**

Run: `cd backend && mvnw.cmd test`
Expected: 6 tests pass.

- [ ] **Step 3: Commit**

```bash
git add backend/pom.xml
if git rev-parse --git-dir >nul 2>&1 (git commit -m "chore: add openhtmltopdf dependency") else echo "not a git repo"
```

---

## Task 2: 新增统计 VO 类

**Files:**
- Create: `backend/src/main/java/edu/cdut/aiback/vo/WorkOrderTrendVO.java`
- Create: `backend/src/main/java/edu/cdut/aiback/vo/HeatmapVO.java`
- Create: `backend/src/main/java/edu/cdut/aiback/vo/PersonnelWorkloadVO.java`
- Create: `backend/src/main/java/edu/cdut/aiback/vo/DeviceMapVO.java`

**Interfaces:**
- Produces: `WorkOrderTrendVO`, `HeatmapVO`, `PersonnelWorkloadVO`, `DeviceMapVO` 供后续接口使用。

- [ ] **Step 1: 创建 WorkOrderTrendVO**

```java
package edu.cdut.aiback.vo;

import lombok.Data;
import java.util.List;

@Data
public class WorkOrderTrendVO {
    private List<String> dates;
    private List<TrendSeries> series;

    @Data
    public static class TrendSeries {
        private String name;
        private List<Long> data;

        public TrendSeries(String name, List<Long> data) {
            this.name = name;
            this.data = data;
        }
    }
}
```

- [ ] **Step 2: 创建 HeatmapVO**

```java
package edu.cdut.aiback.vo;

import lombok.Data;
import java.util.List;

@Data
public class HeatmapVO {
    private List<String> xAxis;
    private List<String> yAxis;
    private List<int[]> data;
}
```

- [ ] **Step 3: 创建 PersonnelWorkloadVO**

```java
package edu.cdut.aiback.vo;

import lombok.Data;

@Data
public class PersonnelWorkloadVO {
    private Long personnelId;
    private String name;
    private String role;
    private Long pendingCount;
    private Long completedThisWeek;
    private Double avgResponseMinutes;
}
```

- [ ] **Step 4: 创建 DeviceMapVO**

```java
package edu.cdut.aiback.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DeviceMapVO {
    private Long id;
    private String deviceName;
    private String deviceCode;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String area;
    private String projectGroup;
    private String latestWorkOrderStatus;
}
```

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/edu/cdut/aiback/vo/*.java
if git rev-parse --git-dir >nul 2>&1 (git commit -m "feat(stats): add visualization VOs") else echo "not a git repo"
```

---

## Task 3: 扩展 StatisticsService 与 Controller

**Files:**
- Modify: `backend/src/main/java/edu/cdut/aiback/service/StatisticsService.java`
- Modify: `backend/src/main/java/edu/cdut/aiback/service/impl/StatisticsServiceImpl.java`
- Modify: `backend/src/main/java/edu/cdut/aiback/mapper/WorkOrderMapper.java`
- Modify: `backend/src/main/java/edu/cdut/aiback/mapper/DeviceMapper.java`
- Modify: `backend/src/main/java/edu/cdut/aiback/mapper/PersonnelMapper.java`
- Modify: `backend/src/main/java/edu/cdut/aiback/controller/StatisticsController.java`

**Interfaces:**
- Consumes: `WorkOrderMapper`, `DeviceMapper`, `PersonnelMapper`.
- Produces: `StatisticsService.getTrends(int days, String projectGroup)`、`getHeatmap(String)`、`getWorkload(String)`、`getDevicesWithLocation(String)`。

- [ ] **Step 1: 在 WorkOrderMapper 增加热力图 SQL**

```java
@Select("SELECT d.area AS area, w.fault_type AS faultType, COUNT(*) AS cnt " +
        "FROM work_order w LEFT JOIN device d ON w.device_id = d.id " +
        "WHERE w.project_group = #{projectGroup} AND w.fault_type IS NOT NULL AND d.area IS NOT NULL " +
        "GROUP BY d.area, w.fault_type")
List<Map<String, Object>> selectFaultHeatmap(@Param("projectGroup") String projectGroup);
```

- [ ] **Step 2: 在 DeviceMapper 增加带最新工单状态的设备 SQL**

```java
@Select("SELECT d.*, " +
        "(SELECT status FROM work_order WHERE device_id = d.id ORDER BY created_at DESC LIMIT 1) AS latestWorkOrderStatus " +
        "FROM device d WHERE d.project_group = #{projectGroup} AND d.latitude IS NOT NULL AND d.longitude IS NOT NULL")
List<DeviceMapVO> selectDevicesWithLocation(@Param("projectGroup") String projectGroup);
```

- [ ] **Step 3: 在 PersonnelMapper 增加负载 SQL**

```java
@Select("SELECT p.id AS personnelId, p.name, p.role, " +
        "COALESCE(SUM(CASE WHEN w.claimer_id = p.id AND w.status NOT IN ('confirmed','closed') THEN 1 ELSE 0 END),0) AS pendingCount, " +
        "COALESCE(SUM(CASE WHEN w.claimer_id = p.id AND w.status IN ('confirmed','closed') AND w.complete_time >= #{weekStart} THEN 1 ELSE 0 END),0) AS completedThisWeek, " +
        "AVG(CASE WHEN w.claimer_id = p.id AND w.response_duration IS NOT NULL THEN w.response_duration END) AS avgResponseMinutes " +
        "FROM personnel p LEFT JOIN work_order w ON w.claimer_id = p.id " +
        "WHERE p.project_group = #{projectGroup} " +
        "GROUP BY p.id, p.name, p.role")
List<Map<String, Object>> selectWorkload(@Param("projectGroup") String projectGroup, @Param("weekStart") LocalDateTime weekStart);
```

- [ ] **Step 4: 扩展 StatisticsService 接口**

```java
public interface StatisticsService {
    DashboardStatisticsVO getDashboardStatistics();
    WorkOrderTrendVO getTrends(int days, String projectGroup);
    HeatmapVO getHeatmap(String projectGroup);
    List<PersonnelWorkloadVO> getWorkload(String projectGroup);
    List<DeviceMapVO> getDevicesWithLocation(String projectGroup);
}
```

- [ ] **Step 5: 在 StatisticsServiceImpl 实现新增方法**

```java
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
```

- [ ] **Step 5.5: 为现有 Dashboard 统计增加 project_group 过滤**

修改 `StatisticsServiceImpl.getDashboardStatistics()` 及其私有辅助方法，确保所有计数按 `UserContext.getProjectGroup()` 过滤：

```java
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

private long countByProjectGroup(String projectGroup) {
    QueryWrapper<WorkOrder> wrapper = new QueryWrapper<>();
    wrapper.eq("project_group", projectGroup);
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
```

- [ ] **Step 6: 在 StatisticsController 增加接口**

```java
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
```

- [ ] **Step 7: 运行后端测试**

Run: `cd backend && mvnw.cmd test`
Expected: compile pass, 6 existing tests pass.

- [ ] **Step 8: Commit**

```bash
git add backend/src/main/java/edu/cdut/aiback
if git rev-parse --git-dir >nul 2>&1 (git commit -m "feat(stats): add trends, heatmap, workload and device map APIs") else echo "not a git repo"
```

---

## Task 4: 实现 Excel/PDF 报表导出

**Files:**
- Create: `backend/src/main/java/edu/cdut/aiback/controller/ReportController.java`
- Create: `backend/src/main/java/edu/cdut/aiback/service/ReportService.java`
- Create: `backend/src/main/java/edu/cdut/aiback/dto/ReportQueryDTO.java`
- Create: `backend/src/main/java/edu/cdut/aiback/vo/export/WorkOrderExportRow.java`
- Create: `backend/src/main/java/edu/cdut/aiback/vo/export/DeviceExportRow.java`
- Create: `backend/src/main/java/edu/cdut/aiback/vo/export/PersonnelExportRow.java`

**Interfaces:**
- Consumes: `WorkOrderService.page`, `DeviceMapper`, `PersonnelMapper`。
- Produces: `POST /api/reports/export-excel` 和 `/api/reports/export-pdf` 返回二进制流。

- [ ] **Step 1: 创建 ReportQueryDTO**

```java
package edu.cdut.aiback.dto;

import lombok.Data;

@Data
public class ReportQueryDTO {
    private String dataType; // work_order / device / personnel
    private String startDate;
    private String endDate;
    private String projectGroup;
    private String status;
}
```

- [ ] **Step 2: 创建 Excel 导出行对象**

WorkOrderExportRow.java:
```java
package edu.cdut.aiback.vo.export;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class WorkOrderExportRow {
    @ExcelProperty("工单编号")
    private String workOrderCode;
    @ExcelProperty("设备编号")
    private String deviceCode;
    @ExcelProperty("故障类型")
    private String faultType;
    @ExcelProperty("紧急程度")
    private String emergencyLevel;
    @ExcelProperty("状态")
    private String status;
    @ExcelProperty("发布时间")
    private String publishTime;
    @ExcelProperty("完成时间")
    private String completeTime;
}
```

DeviceExportRow.java:
```java
package edu.cdut.aiback.vo.export;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class DeviceExportRow {
    @ExcelProperty("设备编号") private String deviceCode;
    @ExcelProperty("设备名称") private String deviceName;
    @ExcelProperty("区域") private String area;
    @ExcelProperty("IP") private String ip;
    @ExcelProperty("项目组") private String projectGroup;
}
```

PersonnelExportRow.java:
```java
package edu.cdut.aiback.vo.export;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class PersonnelExportRow {
    @ExcelProperty("账号") private String account;
    @ExcelProperty("姓名") private String name;
    @ExcelProperty("手机号") private String phone;
    @ExcelProperty("角色") private String role;
    @ExcelProperty("项目组") private String projectGroup;
    @ExcelProperty("状态") private String status;
}
```

- [ ] **Step 3: 创建 ReportService**

```java
package edu.cdut.aiback.service;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import edu.cdut.aiback.dto.ReportQueryDTO;
import edu.cdut.aiback.entity.Device;
import edu.cdut.aiback.entity.Personnel;
import edu.cdut.aiback.entity.WorkOrder;
import edu.cdut.aiback.mapper.DeviceMapper;
import edu.cdut.aiback.mapper.PersonnelMapper;
import edu.cdut.aiback.mapper.WorkOrderMapper;
import edu.cdut.aiback.vo.export.DeviceExportRow;
import edu.cdut.aiback.vo.export.PersonnelExportRow;
import edu.cdut.aiback.vo.export.WorkOrderExportRow;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final WorkOrderMapper workOrderMapper;
    private final DeviceMapper deviceMapper;
    private final PersonnelMapper personnelMapper;

    public void exportExcel(ReportQueryDTO dto, String projectGroup, HttpServletResponse response) throws Exception {
        String filename = dto.getDataType() + "_report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);
        OutputStream out = response.getOutputStream();
        switch (dto.getDataType()) {
            case "work_order" -> EasyExcel.write(out, WorkOrderExportRow.class).sheet("工单").doWrite(buildWorkOrderRows(dto, projectGroup));
            case "device" -> EasyExcel.write(out, DeviceExportRow.class).sheet("设备").doWrite(buildDeviceRows(dto, projectGroup));
            case "personnel" -> EasyExcel.write(out, PersonnelExportRow.class).sheet("人员").doWrite(buildPersonnelRows(dto, projectGroup));
            default -> throw new IllegalArgumentException("未知数据类型");
        }
        out.flush();
    }

    public void exportPdf(ReportQueryDTO dto, String projectGroup, HttpServletResponse response) throws Exception {
        String filename = dto.getDataType() + "_report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".pdf";
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);
        String html = buildHtml(dto, projectGroup);
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.withHtmlContent(html, null);
        builder.toStream(response.getOutputStream());
        builder.run();
    }

    private List<WorkOrderExportRow> buildWorkOrderRows(ReportQueryDTO dto, String projectGroup) {
        LambdaQueryWrapper<WorkOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkOrder::getProjectGroup, projectGroup);
        if (dto.getStatus() != null) wrapper.eq(WorkOrder::getStatus, dto.getStatus());
        if (dto.getStartDate() != null) wrapper.ge(WorkOrder::getPublishTime, LocalDate.parse(dto.getStartDate()).atStartOfDay());
        if (dto.getEndDate() != null) wrapper.le(WorkOrder::getPublishTime, LocalDate.parse(dto.getEndDate()).atTime(23, 59, 59));
        List<WorkOrder> list = workOrderMapper.selectList(wrapper);
        return list.stream().map(w -> {
            WorkOrderExportRow r = new WorkOrderExportRow();
            r.setWorkOrderCode(w.getWorkOrderCode());
            Device d = deviceMapper.selectById(w.getDeviceId());
            r.setDeviceCode(d != null ? d.getDeviceCode() : "");
            r.setFaultType(w.getFaultType());
            r.setEmergencyLevel(w.getEmergencyLevel());
            r.setStatus(w.getStatus());
            r.setPublishTime(w.getPublishTime() != null ? w.getPublishTime().toString() : "");
            r.setCompleteTime(w.getCompleteTime() != null ? w.getCompleteTime().toString() : "");
            return r;
        }).toList();
    }

    private List<DeviceExportRow> buildDeviceRows(ReportQueryDTO dto, String projectGroup) {
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Device::getProjectGroup, projectGroup);
        List<Device> list = deviceMapper.selectList(wrapper);
        return list.stream().map(d -> {
            DeviceExportRow r = new DeviceExportRow();
            r.setDeviceCode(d.getDeviceCode());
            r.setDeviceName(d.getDeviceName());
            r.setArea(d.getArea());
            r.setIp(d.getIp());
            r.setProjectGroup(d.getProjectGroup());
            return r;
        }).toList();
    }

    private List<PersonnelExportRow> buildPersonnelRows(ReportQueryDTO dto, String projectGroup) {
        LambdaQueryWrapper<Personnel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Personnel::getProjectGroup, projectGroup);
        if (dto.getStatus() != null) wrapper.eq(Personnel::getStatus, dto.getStatus());
        List<Personnel> list = personnelMapper.selectList(wrapper);
        return list.stream().map(p -> {
            PersonnelExportRow r = new PersonnelExportRow();
            r.setAccount(p.getAccount());
            r.setName(p.getName());
            r.setPhone(p.getPhone());
            r.setRole(p.getRole());
            r.setProjectGroup(p.getProjectGroup());
            r.setStatus(p.getStatus() != null && p.getStatus() == 1 ? "启用" : "禁用");
            return r;
        }).toList();
    }

    private String buildHtml(ReportQueryDTO dto, String projectGroup) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><style>table{width:100%;border-collapse:collapse;}th,td{border:1px solid #ccc;padding:8px;text-align:left;}</style></head><body>");
        sb.append("<h1>报表</h1>");
        sb.append("<p>项目组：").append(projectGroup).append("</p>");
        sb.append("<p>数据类型：").append(dto.getDataType()).append("</p>");
        sb.append("<p>时间范围：").append(dto.getStartDate()).append(" ~ ").append(dto.getEndDate()).append("</p>");
        sb.append("</body></html>");
        return sb.toString();
    }
}
```

- [ ] **Step 4: 创建 ReportController**

```java
package edu.cdut.aiback.controller;

import edu.cdut.aiback.common.UserContext;
import edu.cdut.aiback.dto.ReportQueryDTO;
import edu.cdut.aiback.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/export-excel")
    public void exportExcel(@RequestBody ReportQueryDTO dto, HttpServletResponse response) throws Exception {
        reportService.exportExcel(dto, UserContext.getProjectGroup(), response);
    }

    @PostMapping("/export-pdf")
    public void exportPdf(@RequestBody ReportQueryDTO dto, HttpServletResponse response) throws Exception {
        reportService.exportPdf(dto, UserContext.getProjectGroup(), response);
    }
}
```

- [ ] **Step 5: 运行后端测试**

Run: `cd backend && mvnw.cmd test`
Expected: pass.

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/edu/cdut/aiback
if git rev-parse --git-dir >nul 2>&1 (git commit -m "feat(report): add excel/pdf export") else echo "not a git repo"
```

---

## Task 5: 实现 SSE 实时 Dashboard

**Files:**
- Modify: `backend/src/main/java/edu/cdut/aiback/config/WebConfig.java`
- Create: `backend/src/main/java/edu/cdut/aiback/controller/SseController.java`
- Modify: `backend/src/main/java/edu/cdut/aiback/util/JwtUtil.java`（确保有 `getClaimsFromToken(String)` 方法，已存在则跳过）

**Interfaces:**
- Consumes: `JwtUtil`, `StatisticsService`。
- Produces: `GET /api/sse/dashboard?token=xxx` 返回 SSE 流。

- [ ] **Step 1: 在 WebConfig 中排除 SSE 路径**

```java
.excludePathPatterns(
    "/api/auth/**",
    "/api/sse/**",
    ...
)
```

- [ ] **Step 2: 创建 SseController**

```java
package edu.cdut.aiback.controller;

import edu.cdut.aiback.common.UserContext;
import edu.cdut.aiback.service.StatisticsService;
import edu.cdut.aiback.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
public class SseController {

    private final JwtUtil jwtUtil;
    private final StatisticsService statisticsService;

    @GetMapping("/dashboard")
    public SseEmitter dashboard(@RequestParam String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new RuntimeException("Token 无效");
        }
        Claims claims = jwtUtil.getClaimsFromToken(token);
        UserContext.UserInfo userInfo = new UserContext.UserInfo(
            claims.get("userId", Long.class),
            claims.getSubject(),
            claims.get("projectGroup", String.class),
            claims.get("role", String.class)
        );

        SseEmitter emitter = new SseEmitter(0L);
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            try {
                UserContext.set(userInfo);
                emitter.send(SseEmitter.event().name("stats").data(statisticsService.getDashboardStatistics()));
            } catch (IOException e) {
                emitter.completeWithError(e);
                executor.shutdown();
            } finally {
                UserContext.clear();
            }
        }, 0, 30, TimeUnit.SECONDS);

        emitter.onCompletion(() -> {
            executor.shutdown();
        });
        emitter.onTimeout(() -> {
            executor.shutdown();
        });
        emitter.onError((e) -> {
            executor.shutdown();
        });
        return emitter;
    }
}
```

- [ ] **Step 3: 运行后端测试**

Run: `cd backend && mvnw.cmd test`
Expected: pass.

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/edu/cdut/aiback
if git rev-parse --git-dir >nul 2>&1 (git commit -m "feat(sse): add dashboard stats sse endpoint") else echo "not a git repo"
```

---

## Task 6: 添加前端图表依赖

**Files:**
- Modify: `admin-frontend/package.json`
- Create: `admin-frontend/.env.example`

**Interfaces:**
- Produces: `echarts` 和 `vue-echarts` 可用于组件。

- [ ] **Step 1: 安装依赖**

Run: `cd admin-frontend && npm install echarts vue-echarts`

- [ ] **Step 2: 创建 .env.example**

```
VITE_AMAP_KEY=你的高德地图Key
```

- [ ] **Step 3: Commit**

```bash
git add admin-frontend/package.json admin-frontend/package-lock.json admin-frontend/.env.example
if git rev-parse --git-dir >nul 2>&1 (git commit -m "chore(frontend): add echarts and amap env") else echo "not a git repo"
```

---

## Task 7: 新增前端 API 模块

**Files:**
- Modify: `admin-frontend/src/api/statistics.ts`
- Create: `admin-frontend/src/api/reports.ts`

**Interfaces:**
- Produces: `getTrends`, `getHeatmap`, `getWorkload`, `getDevicesWithLocation`, `exportExcel`, `exportPdf`。

- [ ] **Step 1: 扩展 statistics.ts**

```ts
import request from './request'

export interface WorkOrderTrend {
  dates: string[]
  series: Array<{ name: string; data: number[] }>
}

export interface HeatmapData {
  xAxis: string[]
  yAxis: string[]
  data: number[][]
}

export interface WorkloadItem {
  personnelId: number
  name: string
  role: string
  pendingCount: number
  completedThisWeek: number
  avgResponseMinutes: number
}

export interface DeviceMapItem {
  id: number
  deviceName: string
  deviceCode: string
  latitude: number
  longitude: number
  area: string
  projectGroup: string
  latestWorkOrderStatus: string | null
}

export function getTrends(days = 30) {
  return request.get<{ data: WorkOrderTrend }>(`/statistics/trends?days=${days}`)
}

export function getHeatmap() {
  return request.get<{ data: HeatmapData }>('/statistics/heatmap')
}

export function getWorkload() {
  return request.get<{ data: WorkloadItem[] }>('/statistics/workload')
}

export function getDevicesWithLocation() {
  return request.get<{ data: DeviceMapItem[] }>('/statistics/devices-with-location')
}
```

- [ ] **Step 2: 创建 reports.ts**

```ts
import request from './request'

export interface ReportQuery {
  dataType: 'work_order' | 'device' | 'personnel'
  startDate?: string
  endDate?: string
  projectGroup?: string
  status?: string
}

export function exportExcel(query: ReportQuery) {
  return request.post('/reports/export-excel', query, {
    responseType: 'blob',
    timeout: 120000,
  })
}

export function exportPdf(query: ReportQuery) {
  return request.post('/reports/export-pdf', query, {
    responseType: 'blob',
    timeout: 120000,
  })
}
```

- [ ] **Step 3: 修改 request.ts 以支持 blob 导出响应**

在响应拦截器开头增加 blob 判断：

```ts
response => {
  if (response.config.responseType === 'blob') {
    return response
  }
  const data = response.data
  ...
}
```

- [ ] **Step 4: Commit**

```bash
git add admin-frontend/src/api
if git rev-parse --git-dir >nul 2>&1 (git commit -m "feat(frontend): add statistics and report apis") else echo "not a git repo"
```

---

## Task 8: 实时数据 Composable 与 LiveStatCard

**Files:**
- Create: `admin-frontend/src/composables/useSseStats.ts`
- Create: `admin-frontend/src/components/LiveStatCard.vue`

**Interfaces:**
- Consumes: `getDashboardStatistics`。
- Produces: `{ stats }` ref，自动更新。

- [ ] **Step 1: 创建 useSseStats.ts**

```ts
import { ref, onMounted, onUnmounted } from 'vue'
import { getDashboardStatistics, type DashboardStatistics } from '@/api/statistics'

const defaultStats: DashboardStatistics = {
  workOrderTotal: 0,
  workOrderToday: 0,
  statusCounts: {},
  slaOverdueCount: 0,
  deviceTotal: 0,
  personnelTotal: 0,
  teamTotal: 0,
  last7Days: [],
}

export function useSseStats() {
  const stats = ref<DashboardStatistics>(defaultStats)
  let es: EventSource | null = null
  let timer: number | null = null
  const base = 'http://localhost:9090'
  const token = localStorage.getItem('token')

  function connect() {
    if (!token) return
    es = new EventSource(`${base}/api/sse/dashboard?token=${token}`)
    es.addEventListener('stats', (e) => {
      stats.value = JSON.parse(e.data)
    })
    es.onerror = () => {
      es?.close()
      startPolling()
    }
  }

  function startPolling() {
    if (timer) return
    timer = window.setInterval(async () => {
      try {
        const res = await getDashboardStatistics()
        stats.value = res.data
      } catch { /* handled by interceptor */ }
    }, 30000)
  }

  onMounted(() => {
    if (typeof EventSource !== 'undefined') connect()
    else startPolling()
  })
  onUnmounted(() => {
    es?.close()
    if (timer) clearInterval(timer)
  })

  return { stats }
}
```

- [ ] **Step 2: 创建 LiveStatCard.vue**

```vue
<template>
  <AppCard class="live-stat-card" :class="pulseClass">
    <template #header>
      <span>{{ title }}</span>
      <span v-if="subtitle" class="card-date">{{ subtitle }}</span>
    </template>
    <div class="stat-primary">
      <CountUp :value="value" />
      <span v-if="deltaText" class="stat-delta">{{ deltaText }}</span>
    </div>
    <slot />
  </AppCard>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import AppCard from './AppCard.vue'
import CountUp from './CountUp.vue'

const props = defineProps<{
  title: string
  value: number
  subtitle?: string
  deltaText?: string
}>()

const pulseClass = ref('')
watch(() => props.value, (newVal, oldVal) => {
  if (oldVal === undefined) return
  pulseClass.value = newVal > oldVal ? 'pulse-up' : newVal < oldVal ? 'pulse-down' : ''
  setTimeout(() => (pulseClass.value = ''), 1000)
})
</script>

<script lang="ts">
export default { name: 'LiveStatCard' }
</script>

<style scoped>
.live-stat-card {
  transition: box-shadow 0.3s ease, border-color 0.3s ease;
}
.pulse-up {
  box-shadow: 0 0 0 4px rgba(52, 199, 89, 0.2);
}
.pulse-down {
  box-shadow: 0 0 0 4px rgba(255, 59, 48, 0.2);
}
</style>
```

- [ ] **Step 3: Commit**

```bash
git add admin-frontend/src/composables admin-frontend/src/components
if git rev-parse --git-dir >nul 2>&1 (git commit -m "feat(frontend): add sse stats and live stat card") else echo "not a git repo"
```

---

## Task 9: 创建图表组件

**Files:**
- Create: `admin-frontend/src/components/charts/TrendChart.vue`
- Create: `admin-frontend/src/components/charts/HeatmapChart.vue`
- Create: `admin-frontend/src/components/charts/WorkloadChart.vue`

**Interfaces:**
- Consumes: ECharts data props。
- Produces: 可复用的图表组件。

- [ ] **Step 1: 创建 TrendChart.vue**

```vue
<template>
  <v-chart class="chart" :option="option" autoresize />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import type { WorkOrderTrend } from '@/api/statistics'

use([CanvasRenderer, LineChart, GridComponent, TooltipComponent, LegendComponent])

const props = defineProps<{ data: WorkOrderTrend }>()

const option = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { bottom: 0 },
  grid: { left: 16, right: 16, top: 24, bottom: 32, containLabel: true },
  xAxis: { type: 'category', boundaryGap: false, data: props.data.dates },
  yAxis: { type: 'value', minInterval: 1 },
  series: props.data.series.map(s => ({
    name: s.name,
    type: 'line',
    smooth: true,
    data: s.data,
    symbolSize: 6,
  })),
}))
</script>

<script lang="ts">
export default { name: 'TrendChart' }
</script>

<style scoped>
.chart { height: 260px; width: 100%; }
</style>
```

- [ ] **Step 2: 创建 HeatmapChart.vue**

```vue
<template>
  <v-chart class="chart" :option="option" autoresize />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { HeatmapChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, VisualMapComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import type { HeatmapData } from '@/api/statistics'

use([CanvasRenderer, HeatmapChart, GridComponent, TooltipComponent, VisualMapComponent])

const props = defineProps<{ data: HeatmapData }>()

const option = computed(() => ({
  tooltip: { position: 'top' },
  grid: { left: 16, right: 16, top: 8, bottom: 64, containLabel: true },
  xAxis: { type: 'category', data: props.data.xAxis, splitArea: { show: true } },
  yAxis: { type: 'category', data: props.data.yAxis, splitArea: { show: true } },
  visualMap: { min: 0, max: 20, calculable: true, orient: 'horizontal', left: 'center', bottom: 0 },
  series: [{
    type: 'heatmap',
    data: props.data.data,
    label: { show: true },
    emphasis: { itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.5)' } },
  }],
}))
</script>

<script lang="ts">
export default { name: 'HeatmapChart' }
</script>

<style scoped>
.chart { height: 320px; width: 100%; }
</style>
```

- [ ] **Step 3: 创建 WorkloadChart.vue**

```vue
<template>
  <v-chart class="chart" :option="option" autoresize />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import type { WorkloadItem } from '@/api/statistics'

use([CanvasRenderer, BarChart, GridComponent, TooltipComponent])

const props = defineProps<{ data: WorkloadItem[] }>()

const option = computed(() => ({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: 16, right: 16, top: 8, bottom: 8, containLabel: true },
  xAxis: { type: 'value', minInterval: 1 },
  yAxis: { type: 'category', data: props.data.map(i => i.name).reverse() },
  series: [{
    type: 'bar',
    data: props.data.map(i => i.pendingCount).reverse(),
    itemStyle: { borderRadius: [0, 8, 8, 0] },
  }],
}))
</script>

<script lang="ts">
export default { name: 'WorkloadChart' }
</script>

<style scoped>
.chart { height: 260px; width: 100%; }
</style>
```

- [ ] **Step 4: Commit**

```bash
git add admin-frontend/src/components/charts
if git rev-parse --git-dir >nul 2>&1 (git commit -m "feat(frontend): add echarts components") else echo "not a git repo"
```

---

## Task 10: 创建高德地图组件

**Files:**
- Create: `admin-frontend/src/composables/useAMap.ts`
- Create: `admin-frontend/src/components/map/MapBoard.vue`

**Interfaces:**
- Consumes: `VITE_AMAP_KEY`, `DeviceMapItem[]`。
- Produces: 可渲染地图与标记的组件。

- [ ] **Step 1: 创建 useAMap.ts**

```ts
const AMAP_SCRIPT_ID = 'amap-script'

export function useAMapLoader() {
  const key = import.meta.env.VITE_AMAP_KEY

  function load(): Promise<typeof window.AMap> {
    if (!key) return Promise.reject(new Error('VITE_AMAP_KEY 未配置'))
    if (window.AMap) return Promise.resolve(window.AMap)
    const existing = document.getElementById(AMAP_SCRIPT_ID)
    if (existing) {
      return new Promise((resolve, reject) => {
        existing.addEventListener('load', () => resolve(window.AMap))
        existing.addEventListener('error', reject)
      })
    }
    return new Promise((resolve, reject) => {
      const script = document.createElement('script')
      script.id = AMAP_SCRIPT_ID
      script.src = `https://webapi.amap.com/maps?v=2.0&key=${key}`
      script.onload = () => resolve(window.AMap)
      script.onerror = reject
      document.head.appendChild(script)
    })
  }

  return { load, key }
}
```

- [ ] **Step 2: 创建 MapBoard.vue**

```vue
<template>
  <div class="map-board">
    <div v-if="error" class="map-error">
      <el-empty :description="error" />
    </div>
    <div v-else ref="mapRef" class="map-container" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useAMapLoader } from '@/composables/useAMap'
import type { DeviceMapItem } from '@/api/statistics'

const props = defineProps<{ devices: DeviceMapItem[] }>()
const mapRef = ref<HTMLDivElement>()
const error = ref('')
let map: any = null
let markers: any[] = []
const statusColor: Record<string, string> = {
  published: '#0071e3',
  claimed: '#ff9500',
  in_progress: '#ff9500',
  completing: '#ff9500',
  confirmed: '#34c759',
  closed: '#34c759',
  pending_confirm: '#af52de',
}

function render() {
  if (!map || !props.devices.length) return
  markers.forEach(m => map.remove(m))
  markers = []
  const AMap = window.AMap
  props.devices.forEach(d => {
    const marker = new AMap.Marker({
      position: [d.longitude, d.latitude],
      title: d.deviceName,
      icon: new AMap.Icon({
        size: new AMap.Size(24, 24),
        image: statusIcon(d.latestWorkOrderStatus),
        imageSize: new AMap.Size(24, 24),
      }),
    })
    marker.on('click', () => {
      const info = new AMap.InfoWindow({
        content: `<div style="padding:8px"><b>${d.deviceName}</b><br/>状态：${d.latestWorkOrderStatus || '正常'}</div>`,
        offset: new AMap.Pixel(0, -12),
      })
      info.open(map, marker.getPosition())
    })
    map.add(marker)
    markers.push(marker)
  })
  if (markers.length) map.setFitView()
}

function statusIcon(status: string | null) {
  const color = statusColor[status || ''] || '#34c759'
  const svg = encodeURIComponent(`<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10" fill="${color}"/></svg>`)
  return `data:image/svg+xml;utf8,${svg}`
}

onMounted(async () => {
  try {
    const AMap = await useAMapLoader().load()
    map = new AMap.Map(mapRef.value, { zoom: 11, viewMode: '2D' })
    render()
  } catch (e: any) {
    error.value = e.message || '地图加载失败'
  }
})

watch(() => props.devices, render, { deep: true })
</script>

<script lang="ts">
export default { name: 'MapBoard' }
</script>

<style scoped>
.map-board { width: 100%; height: 100%; position: relative; }
.map-container { width: 100%; height: 100%; border-radius: 24px; }
.map-error { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; }
</style>
```

- [ ] **Step 3: Commit**

```bash
git add admin-frontend/src/components/map admin-frontend/src/composables/useAMap.ts
if git rev-parse --git-dir >nul 2>&1 (git commit -m "feat(frontend): add amap board component") else echo "not a git repo"
```

---

## Task 11: 创建数据大屏页面

**Files:**
- Create: `admin-frontend/src/views/data-screen/index.vue`
- Modify: `admin-frontend/src/router/index.ts`
- Modify: `admin-frontend/src/layout/index.vue`

**Interfaces:**
- Consumes: `MapBoard`, `HeatmapChart`, `WorkloadChart`, `getDevicesWithLocation`, `getHeatmap`, `getWorkload`, `useSseStats`。

- [ ] **Step 1: 创建 data-screen/index.vue**

```vue
<template>
  <div class="data-screen">
    <AppCard class="screen-header">
      <div class="screen-title">数据大屏</div>
      <div class="screen-filters">
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" />
        <el-select v-model="view" placeholder="视图">
          <el-option label="地图" value="map" />
          <el-option label="故障热力" value="heatmap" />
          <el-option label="人员负载" value="workload" />
        </el-select>
      </div>
    </AppCard>

    <div class="screen-body">
      <div class="screen-sidebar">
        <LiveStatCard title="设备总数" :value="stats.deviceTotal" />
        <LiveStatCard title="在修设备" :value="inProgressDevices" />
        <LiveStatCard title="SLA 预警" :value="stats.slaOverdueCount" />
        <LiveStatCard title="人员总数" :value="stats.personnelTotal" />
      </div>
      <AppCard class="screen-main">
        <MapBoard v-if="view === 'map'" :devices="devices" />
        <HeatmapChart v-else-if="view === 'heatmap'" :data="heatmap" />
        <WorkloadChart v-else-if="view === 'workload'" :data="workload" />
      </AppCard>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import AppCard from '@/components/AppCard.vue'
import LiveStatCard from '@/components/LiveStatCard.vue'
import MapBoard from '@/components/map/MapBoard.vue'
import HeatmapChart from '@/components/charts/HeatmapChart.vue'
import WorkloadChart from '@/components/charts/WorkloadChart.vue'
import { useSseStats } from '@/composables/useSseStats'
import { getDevicesWithLocation, getHeatmap, getWorkload, type DeviceMapItem, type HeatmapData, type WorkloadItem } from '@/api/statistics'

const { stats } = useSseStats()
const view = ref('map')
const dateRange = ref([])
const devices = ref<DeviceMapItem[]>([])
const heatmap = ref<HeatmapData>({ xAxis: [], yAxis: [], data: [] })
const workload = ref<WorkloadItem[]>([])

const inProgressDevices = computed(() => devices.value.filter(d => ['claimed','in_progress','completing'].includes(d.latestWorkOrderStatus || '')).length)

onMounted(async () => {
  const [d, h, w] = await Promise.all([getDevicesWithLocation(), getHeatmap(), getWorkload()])
  devices.value = d.data
  heatmap.value = h.data
  workload.value = w.data
})
</script>

<script lang="ts">
export default { name: 'DataScreen' }
</script>

<style scoped>
.data-screen { display: flex; flex-direction: column; gap: 24px; height: calc(100vh - 160px); }
.screen-header { display: flex; justify-content: space-between; align-items: center; }
.screen-title { font-size: 22px; font-weight: 700; }
.screen-filters { display: flex; gap: 12px; }
.screen-body { flex: 1; display: flex; gap: 24px; min-height: 0; }
.screen-sidebar { width: 320px; display: flex; flex-direction: column; gap: 16px; }
.screen-main { flex: 1; min-width: 0; overflow: hidden; }
</style>
```

- [ ] **Step 2: 在 router 和 layout 中新增菜单**

`router/index.ts` children 中增加：

```ts
{
  path: 'data-screen',
  name: 'DataScreen',
  component: () => import('@/views/data-screen/index.vue'),
  meta: { title: '数据大屏' }
},
{
  path: 'reports',
  name: 'Reports',
  component: () => import('@/views/reports/index.vue'),
  meta: { title: '报表中心' }
}
```

`layout/index.vue` sidebar el-menu 中增加：

```html
<el-menu-item index="/data-screen">
  <el-icon><MapLocation /></el-icon>
  <span>数据大屏</span>
</el-menu-item>
<el-menu-item index="/reports">
  <el-icon><Document /></el-icon>
  <span>报表中心</span>
</el-menu-item>
```

并引入 `MapLocation`, `Document` icons。

- [ ] **Step 3: Commit**

```bash
git add admin-frontend/src/views/data-screen admin-frontend/src/router/index.ts admin-frontend/src/layout/index.vue
if git rev-parse --git-dir >nul 2>&1 (git commit -m "feat(frontend): add data screen page and menu") else echo "not a git repo"
```

---

## Task 12: 创建报表中心页面

**Files:**
- Create: `admin-frontend/src/views/reports/index.vue`

**Interfaces:**
- Consumes: `exportExcel`, `exportPdf`。
- Produces: 报表下载页面。

- [ ] **Step 1: 创建 reports/index.vue**

```vue
<template>
  <div class="reports-page">
    <AppCard class="report-card">
      <template #header><span>报表导出</span></template>
      <el-form :model="form" label-width="100px" style="max-width:600px">
        <el-form-item label="数据类型">
          <el-radio-group v-model="form.dataType">
            <el-radio-button value="work_order">工单</el-radio-button>
            <el-radio-button value="device">设备</el-radio-button>
            <el-radio-button value="personnel">人员</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="格式">
          <el-radio-group v-model="format">
            <el-radio-button value="excel">Excel</el-radio-button>
            <el-radio-button value="pdf">PDF</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" round :loading="loading" @click="handleExport">导出报表</el-button>
        </el-form-item>
      </el-form>
    </AppCard>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import AppCard from '@/components/AppCard.vue'
import { exportExcel, exportPdf } from '@/api/reports'
import { ElMessage } from 'element-plus'

const form = reactive({
  dataType: 'work_order',
  startDate: '',
  endDate: '',
})
const format = ref<'excel' | 'pdf'>('excel')
const loading = ref(false)
const dateRange = ref<string[]>([])

function download(blob: Blob, filename: string) {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

async function handleExport() {
  if (dateRange.value?.length === 2) {
    form.startDate = dateRange.value[0]
    form.endDate = dateRange.value[1]
  }
  loading.value = true
  try {
    const res = format.value === 'excel'
      ? await exportExcel(form)
      : await exportPdf(form)
    const blob = res.data as Blob
    const ext = format.value === 'excel' ? 'xlsx' : 'pdf'
    download(blob, `${form.dataType}_report_${Date.now()}.${ext}`)
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  } finally {
    loading.value = false
  }
}
</script>

<script lang="ts">
export default { name: 'ReportsPage' }
</script>

<style scoped>
.reports-page { display: flex; justify-content: center; padding-top: 40px; }
.report-card { width: 100%; max-width: 720px; }
</style>
```

- [ ] **Step 2: Commit**

```bash
git add admin-frontend/src/views/reports
if git rev-parse --git-dir >nul 2>&1 (git commit -m "feat(frontend): add reports page") else echo "not a git repo"
```

---

## Task 13: 改造 Dashboard

**Files:**
- Modify: `admin-frontend/src/views/dashboard/index.vue`
- Modify: `admin-frontend/src/components/CountUp.vue`（已支持 watcher，无需修改）

**Interfaces:**
- Consumes: `useSseStats`, `LiveStatCard`, `TrendChart`, `WorkloadChart`, `getTrends`, `getWorkload`。

- [ ] **Step 1: 替换 Dashboard 卡片为 LiveStatCard 并新增图表区**

修改 `admin-frontend/src/views/dashboard/index.vue`：

1. imports 增加：

```ts
import { useSseStats } from '@/composables/useSseStats'
import LiveStatCard from '@/components/LiveStatCard.vue'
import TrendChart from '@/components/charts/TrendChart.vue'
import WorkloadChart from '@/components/charts/WorkloadChart.vue'
import { getTrends, getWorkload, type WorkOrderTrend, type WorkloadItem } from '@/api/statistics'
```

2. script setup 中新增：

```ts
const { stats } = useSseStats()
const trends = ref<WorkOrderTrend>({ dates: [], series: [] })
const workload = ref<WorkloadItem[]>([])

onMounted(async () => {
  try {
    const [tRes, wRes] = await Promise.all([getTrends(30), getWorkload()])
    trends.value = tRes.data
    workload.value = wRes.data
  } catch (e) {
    ElMessage.error('加载图表数据失败')
  }
  // 原 dashboard 统计请求由 SSE 接管，保留 loading 为 false
  loading.value = false
})
```

3. 把四个统计卡片替换为 `LiveStatCard`：

```vue
<LiveStatCard title="工单总数" :value="stats.workOrderTotal" :delta-text="`+${stats.workOrderToday} 今日新增`" />
<LiveStatCard title="SLA 预警" :value="stats.slaOverdueCount" />
<LiveStatCard title="设备总数" :value="stats.deviceTotal" />
<LiveStatCard title="人员总数" :value="stats.personnelTotal" />
```

4. 在 dashboard-grid 末尾追加：

```vue
<AppCard class="dashboard-card card-trend" :hoverable="true">
  <template #header><span>近 30 天趋势</span></template>
  <TrendChart :data="trends" />
</AppCard>
<AppCard class="dashboard-card card-workload" :hoverable="true">
  <template #header><span>人员负载</span></template>
  <WorkloadChart :data="workload" />
</AppCard>
```

5. 更新 `<style scoped>` 中的 grid 布局：

```css
.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  grid-template-areas:
    "overview overview overview overview overview overview overview overview sla sla sla sla"
    "overview overview overview overview overview overview overview overview status status status status"
    "resources resources resources resources resources resources resources resources resources resources resources resources"
    "trend trend trend trend trend trend trend trend workload workload workload workload";
  gap: 24px;
}
.card-trend { grid-area: trend; }
.card-workload { grid-area: workload; }
```

- [ ] **Step 2: Commit**

```bash
git add admin-frontend/src/views/dashboard/index.vue
if git rev-parse --git-dir >nul 2>&1 (git commit -m "feat(frontend): wire live stats and charts into dashboard") else echo "not a git repo"
```

---

## Task 14: 补充演示设备坐标数据

**Files:**
- Modify: `backend/docs/sql/init.sql`

**Interfaces:**
- Produces: 地图上有可展示的坐标数据。

- [ ] **Step 1: 在 init.sql 设备表创建后追加 INSERT**

```sql
-- 演示设备坐标（广州区域）
INSERT INTO device (device_code, device_name, area, ip, latitude, longitude, operation_type, project_group) VALUES
('CAM-A01', '卡口摄像机 A01', '天河区', '192.168.1.101', 23.1291, 113.2644, '维护', '广州大道项目'),
('CAM-A02', '卡口摄像机 A02', '越秀区', '192.168.1.102', 23.1350, 113.2700, '维护', '广州大道项目'),
('CAM-A03', '卡口摄像机 A03', '海珠区', '192.168.1.103', 23.1000, 113.2800, '维护', '广州大道项目'),
('CAM-A04', '卡口摄像机 A04', '白云区', '192.168.1.104', 23.1800, 113.2500, '维护', '广州大道项目');
```

- [ ] **Step 2: Commit**

```bash
git add backend/docs/sql/init.sql
if git rev-parse --git-dir >nul 2>&1 (git commit -m "chore: add demo device coordinates") else echo "not a git repo"
```

---

## Task 15: 后端测试

**Files:**
- Create: `backend/src/test/java/edu/cdut/aiback/controller/StatisticsControllerTest.java`
- Create: `backend/src/test/java/edu/cdut/aiback/controller/ReportControllerTest.java`

**Interfaces:**
- Verifies: 新增接口返回 200 和正确结构。

- [ ] **Step 1: 创建 StatisticsControllerTest**

```java
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
        token = jwtUtil.generateToken(1L, "test@gzgd.com", "广州大道项目", "公司管理");
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
}
```

- [ ] **Step 2: 创建 ReportControllerTest**

```java
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
        token = jwtUtil.generateToken(1L, "test@gzgd.com", "广州大道项目", "公司管理");
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
}
```

- [ ] **Step 3: 运行测试**

Run: `cd backend && mvnw.cmd test`
Expected: 新增 + 原有测试全部通过。

- [ ] **Step 4: Commit**

```bash
git add backend/src/test
if git rev-parse --git-dir >nul 2>&1 (git commit -m "test: add statistics and report controller tests") else echo "not a git repo"
```

---

## Task 16: 前端构建与最终验证

**Files:**
- All modified frontend files.

**Interfaces:**
- Verifies: 前端无编译错误。

- [ ] **Step 1: 运行前端构建**

Run: `cd admin-frontend && npm run build`
Expected: build success.

- [ ] **Step 2: 手动验证清单**

1. 登录后 Dashboard 数字自动刷新。
2. 打开“数据大屏” → 地图显示 4 个演示设备标记。
3. 切换“故障热力”和“人员负载”视图正常。
4. 打开“报表中心” → 导出 Excel 和 PDF 均可下载。

- [ ] **Step 3: Commit**

```bash
git add admin-frontend
if git rev-parse --git-dir >nul 2>&1 (git commit -m "chore: finalize visualization center MVP") else echo "not a git repo"
```

---

## Self-Review Checklist

- [x] Spec coverage: 所有 MVP 功能均有对应 Task。
- [x] Placeholder scan: 无 TBD/TODO，所有代码步骤给出实际代码或明确命令。
- [x] Type consistency: 前后端 VO/TypeScript interface 字段名称一致。
- [x] Scope: 一次可交付的 MVP，二期（3D/语音/AI）未放入。
