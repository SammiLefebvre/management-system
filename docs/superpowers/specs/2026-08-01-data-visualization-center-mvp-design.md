# 数据可视化中心 MVP 设计文档

## 1. 背景与目标

当前工单管理系统已具备 Dashboard、工单生命周期、设备台账、人员、班组、码表、SLA 等基础能力。
本设计在一期内增加一个**实用可视化中心**，目标：

1. 用地图直观展示设备与工单的地理分布。
2. 用 ECharts 展示工单趋势、故障热力、人员负载。
3. 让 Dashboard 关键数字实时刷新，并带平滑动画。
4. 支持工单/设备/人员数据一键导出 Excel/PDF。

## 2. 范围

### 2.1 包含功能

- 新增菜单/页面 **数据大屏**（`/data-screen`）。
- 新增菜单/页面 **报表中心**（`/reports`）。
- 改造现有 **仪表盘**（`/dashboard`）：增强实时动画卡片 + 增加趋势图。
- 新增后端统计接口：`/api/statistics/trends`、`/api/statistics/heatmap`、`/api/statistics/workload`、`/api/devices/with-location`。
- 新增后端报表导出接口：`/api/reports/export-excel`、`/api/reports/export-pdf`。
- 新增 SSE 实时通道：`/api/sse/dashboard`。
- 新增/增强前端组件：`MapBoard`、`TrendChart`、`HeatmapChart`、`WorkloadChart`、`ReportPanel`、`LiveStatCard`。

### 2.2 非目标

- 3D 地球、语音建单、AI 助手（放到二期“炫技包”）。
- 移动端地图适配。
- 按钮级 RBAC 权限控制。
- 实时 GPS 轨迹追踪。

## 3. 用户旅程

1. 管理员登录后进入 **Dashboard**，看到实时跳动的关键数字和近 30 天趋势图。
2. 进入 **数据大屏**，在地图上查看所有设备；红色标记表示该设备有待处理工单，绿色表示正常。
3. 在数据大屏右侧切换“故障热力”或“人员负载”视图，辅助调度决策。
4. 进入 **报表中心**，选择时间范围、数据类型、格式，点击导出，浏览器下载文件。

## 4. UI 设计

保持现有 Apple-style 设计语言：大圆角卡片、柔和阴影、充足留白、毛玻璃头部。

### 4.1 仪表盘（/dashboard）

在现有 Bento 网格下方新增一行：

- **近 30 天工单趋势**（占 8 列）：折线图，展示新增、完成、超期三条线。
- **人员负载**（占 4 列）：横向柱状图，展示当前未完工单数 Top 8 人员。

顶部数字卡片改为 `LiveStatCard`：当 SSE 推送数据变化时，数字平滑跳动，同时卡片边框轻微闪烁提示更新。

### 4.2 数据大屏（/data-screen）

页面布局：

```
┌─────────────────────────────────────────────────────────┐
│  页面头部：标题 + 项目组筛选 + 工单状态筛选 + 时间范围    │
├──────────────┬──────────────────────────────────────────┤
│  统计卡片    │                                          │
│  - 设备总数  │          高德地图                         │
│  - 在修设备  │          标记 + 聚合                      │
│  - 在线人员  │                                          │
│  - SLA 预警  │                                          │
├──────────────┤                                          │
│  视图切换    │                                          │
│  - 地图      │                                          │
│  - 故障热力  │                                          │
│  - 人员负载  │                                          │
└──────────────┴──────────────────────────────────────────┘
```

- 左侧边栏宽度 320px，放置实时统计卡片和视图切换按钮。
- 地图区域占满剩余空间。
- 点击地图标记弹出信息窗：设备名称、最新工单编号、状态、操作按钮“查看详情”。

### 4.3 报表中心（/reports）

居中卡片布局：

- 数据类型单选：工单 / 设备 / 人员。
- 时间范围选择器。
- 项目组下拉（可选）。
- 格式单选：Excel / PDF。
- 导出按钮 + 下载链接展示区。

## 5. 数据模型与 API

### 5.1 现有依赖

- 设备表 `device` 已有 `latitude`、`longitude`。
- 工单表 `work_order` 已有 `device_id`、`status`、`emergency_level`、`project_group`。
- 人员表 `personnel` 已有 `project_group`、`role`。

### 5.2 新增接口

#### GET /api/statistics/trends

查询参数：

| 参数 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| days | int | 30 | 统计天数 |
| projectGroup | string | null | 按项目组过滤（可选） |

响应示例：

```json
{
  "data": {
    "dates": ["07-03", "07-04", "..."],
    "series": [
      { "name": "新增工单", "data": [5, 8, 2, ...] },
      { "name": "完成工单", "data": [4, 6, 3, ...] },
      { "name": "超期工单", "data": [0, 1, 0, ...] }
    ]
  }
}
```

#### GET /api/statistics/heatmap

返回故障类型 × 区域的二维热力矩阵。

响应示例：

```json
{
  "data": {
    "xAxis": ["离线", "画面异常", "电源故障", "网络抖动"],
    "yAxis": ["天河区", "越秀区", "海珠区", "白云区"],
    "data": [
      [0, 0, 12],
      [1, 2, 8],
      ...
    ]
  }
}
```

`data` 中每一项为 `[xIndex, yIndex, count]`。

#### GET /api/statistics/workload

返回人员负载，用于柱状图。

响应示例：

```json
{
  "data": [
    {
      "personnelId": 1,
      "name": "张三",
      "role": "外场",
      "pendingCount": 5,
      "completedThisWeek": 12,
      "avgResponseMinutes": 18
    }
  ]
}
```

#### GET /api/devices/with-location

返回带经纬度的设备列表及设备上最新一条工单状态（若无则为 null）。

响应示例：

```json
{
  "data": [
    {
      "id": 1,
      "deviceName": "卡口摄像机 A01",
      "deviceCode": "CAM-A01",
      "latitude": 23.1291,
      "longitude": 113.2644,
      "area": "天河区",
      "projectGroup": "广州大道项目",
      "latestWorkOrderStatus": "published"
    }
  ]
}
```

无经纬度的设备不返回，或返回 `hasLocation: false` 由前端统一提示。

#### POST /api/reports/export-excel

请求体：

```json
{
  "dataType": "work_order",
  "startDate": "2026-07-01",
  "endDate": "2026-08-01",
  "projectGroup": "广州大道项目",
  "status": "published"
}
```

响应：直接返回 `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` 二进制流，Header 带 `Content-Disposition: attachment; filename=工单报表_20260801.xlsx`。

#### POST /api/reports/export-pdf

与 Excel 接口参数一致，返回 `application/pdf` 流。

PDF 使用 HTML 模板渲染，可通过 `openhtmltopdf` 生成，内容包含：

- 报表标题、时间范围、生成时间。
- 汇总数字。
- 数据表格。
- 可选趋势图/柱状图图片（通过 ECharts 服务端渲染或前端截图嵌入，一期可不做）。

#### GET /api/sse/dashboard

SSE 连接，后端每 30 秒推送一次 `DashboardStatisticsVO` 完整数据：

```
event: stats
data: {"workOrderTotal": 120, "slaOverdueCount": 3, ...}
```

连接断开时前端自动降级为 30 秒轮询 `/api/statistics/dashboard`。

## 6. 前端组件设计

### 6.1 新增组件

| 组件 | 职责 |
|---|---|
| `MapBoard.vue` | 高德地图容器、标记渲染、聚合、信息窗 |
| `TrendChart.vue` | ECharts 折线图 |
| `HeatmapChart.vue` | ECharts 热力图 |
| `WorkloadChart.vue` | ECharts 横向柱状图 |
| `ReportPanel.vue` | 报表筛选表单 + 导出按钮 |
| `LiveStatCard.vue` | 带数据变化动画和闪烁提示的统计卡片 |

### 6.2 增强现有组件

- `CountUp.vue`：支持 `key` 变化时重新执行动画。
- `Dashboard/index.vue`：接入 `useSseStats` composable。

### 6.3 实时数据 Composable

新增 `admin-frontend/src/composables/useSseStats.ts`：

- 建立 EventSource。
- 收到数据后更新 Pinia store 或本地 ref。
- 断线 5 秒后重连。
- 不支持 SSE 时自动降级轮询。

## 7. 地图方案

使用 **高德地图 JS API 2.0**。

- 需要申请 Web端（JS API）Key。
- 通过 CDN 加载 `AMapLoader`。
- 标记按工单状态着色：
  - 正常/无工单：绿色 `#34c759`
  - 待认领 `published`：蓝色 `#0071e3`
  - 进行中 `claimed/in_progress/completing`：橙色 `#ff9500`
  - 超期：红色 `#ff3b30`
- 标记聚合使用 `AMap.MarkerCluster`。
- 点击标记弹出 `AMap.InfoWindow`，显示设备信息和跳转按钮。

**注意**：当前 `init.sql` 未预置设备数据；若演示需要地图效果，本次实现会补充若干带坐标的演示设备。

## 8. 导出方案

| 格式 | 后端库 | 说明 |
|---|---|---|
| Excel | EasyExcel | 大数据量流式写入，内存友好 |
| PDF | OpenHTMLToPDF | 用 Thymeleaf/Freemarker 渲染 HTML 模板后转 PDF |

导出接口均为同步生成（当前数据量不大），前端显示“正在生成…”加载状态。

## 9. 动画与交互

- 数字变化：`CountUp` 从旧值动画到新值，持续 800ms。
- 卡片更新提示：`LiveStatCard` 在数值增加时边框显示绿色脉冲，减少时红色脉冲，1s 后消失。
- 图表入场：ECharts 开启 `animationDuration: 1000` 和缓动。
- 地图标记加载：带 `drop` 动画效果。

## 10. 错误处理

- 高德 Key 缺失或加载失败：地图区域显示占位提示“地图服务暂不可用”，并给出切换到列表模式的入口（二期）。
- SSE 连接失败：自动降级为 30 秒轮询，UI 不阻断。
- 无经纬度数据：地图中央显示“暂无设备坐标数据”，同时左侧统计卡片仍展示。
- 导出失败：前端显示 `ElMessage.error`，保留表单状态。

## 11. 测试计划

- 后端：为 `StatisticsController` 新增接口写单元测试，覆盖 trends/heatmap/workload/devices-with-location。
- 后端：为 `ReportController` 两个导出接口写测试，验证 Content-Type 和文件名。
- 前端：`npm run build` 通过。
- 前端：手动验证 SSE 连接、地图标记、图表渲染、导出下载。

## 12. 后续可扩展（二期）

- 3D 地球/城市大屏。
- 语音建单。
- AI 助手聊天窗口。
- 实时 GPS 轨迹。
- 报表定时邮件发送。
