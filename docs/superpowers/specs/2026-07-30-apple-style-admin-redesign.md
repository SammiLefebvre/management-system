# 管理后台 Apple 风 redesign 设计规格

> 目标：将现有 Vue 3 + Element Plus 管理后台改造为 Apple 风格，增加 Dashboard（参考 Apple Health 桌面化设计）、流畅动效、手动深色模式，并确保 Dashboard 数据真实来自数据库。

---

## 1. 设计原则

- **Apple 设计语言**：大圆角、柔和阴影、充足留白、精致的字体层级、克制的动效。
- **Apple Health 桌面化**：Dashboard 以大卡片网格布局呈现，重要指标突出，信息全面但不拥挤，适合 13~27 寸屏幕横向空间。
- **真实数据驱动**：Dashboard 所有数字必须调用后端统计接口，不得写死。
- **深色模式**：手动切换，高对比度，确保文字可读性。
- **渐进增强**：保留 Element Plus 复杂组件（表格、表单、弹窗）的功能，只改外观包装。

---

## 2. 全局设计系统（Design Tokens）

### 2.1 色彩

通过 CSS 自定义属性定义，Tailwind 与 Element Plus 共用同一套变量。

**Light Mode**

| Token | 值 | 用途 |
|---|---|---|
| `--bg-primary` | `#ffffff` | 页面主背景 |
| `--bg-secondary` | `#f5f5f7` | 卡片背景、侧边栏背景 |
| `--bg-tertiary` | `#ffffff` | 浮层、弹窗背景 |
| `--text-primary` | `#1d1d1f` | 主标题、正文 |
| `--text-secondary` | `#6e6e73` | 副标题、描述 |
| `--text-tertiary` | `#86868b` | 占位符、禁用 |
| `--accent` | `#0071e3` | 主按钮、链接、选中态 |
| `--accent-hover` | `#0077ed` | 按钮悬停 |
| `--accent-light` | `#e8f4fd` | 选中背景、轻强调 |
| `--success` | `#34c759` | 成功、正常状态 |
| `--warning` | `#ff9500` | 警告 |
| `--danger` | `#ff3b30` | 错误、超期、删除 |
| `--border` | `#d2d2d7` | 边框、分割线 |
| `--shadow-sm` | `0 2px 8px rgba(0,0,0,0.04)` | 小卡片悬浮 |
| `--shadow-md` | `0 8px 24px rgba(0,0,0,0.06)` | 大卡片 |
| `--shadow-lg` | `0 16px 40px rgba(0,0,0,0.08)` | 弹窗、下拉 |

**Dark Mode**

| Token | 值 | 用途 |
|---|---|---|
| `--bg-primary` | `#000000` | 页面主背景 |
| `--bg-secondary` | `#1d1d1f` | 卡片、侧边栏 |
| `--bg-tertiary` | `#2c2c2e` | 输入框、 hover 背景 |
| `--text-primary` | `#f5f5f7` | 主文字 |
| `--text-secondary` | `#a1a1a6` | 次要文字 |
| `--text-tertiary` | `#8e8e93` | 占位符 |
| `--accent` | `#0a84ff` | 强调色（稍亮，保证对比） |
| `--accent-light` | `#1c2c3c` | 深色下轻强调背景 |
| `--border` | `#38383a` | 边框 |
| `--shadow-*` | 使用半透明黑色，比浅色模式略强 | 保持卡片层次 |

### 2.2 字体

- 字体栈：`-apple-system, BlinkMacSystemFont, "SF Pro Text", "Segoe UI", Roboto, Helvetica, Arial, sans-serif`
- 标题：细字重、大字号
  - 页面大标题：`28px / font-weight: 600 / letter-spacing: -0.02em`
  - 卡片标题：`18px / font-weight: 600`
  - 数字指标：`36px / font-weight: 700 / letter-spacing: -0.03em`
- 正文：`14px / line-height: 1.5`
- 辅助文字：`13px / color: var(--text-secondary)`

### 2.3 圆角

- 大卡片 / Dashboard 卡片：`border-radius: 20px`
- 小卡片 / 按钮：`border-radius: 12px`
- 胶囊按钮 / 标签：`border-radius: 9999px`
- 输入框：`border-radius: 10px`

### 2.4 间距

- 页面内边距：`32px`（桌面默认），`48px`（宽屏 ≥1440px）
- 卡片间隙：`24px`
- 卡片内部内边距：`24px`
- 表单/表格行高：`48px`

---

## 3. 主题切换

- 使用 Pinia store `theme` 保存当前模式（`light` / `dark`）。
- 初始化时读取 `localStorage`，无则默认 `light`。
- 切换时：
  - 在 `<html>` 元素上添加/移除 `class="dark"`。
  - Tailwind 配置 `darkMode: 'class'`。
  - 导入 Element Plus 深色 CSS：`element-plus/theme-chalk/dark/css-vars.css`。
- 所有自定义颜色使用 CSS 变量，随 `dark` class 自动切换。
- 过渡动画：`transition: background-color 0.3s ease, color 0.3s ease, border-color 0.3s ease`。

---

## 4. 布局改造

### 4.1 侧边栏（Sidebar）

- 宽度：`240px`，背景 `var(--bg-secondary)`。
- Logo 区：高度 `80px`，左侧放系统图标 + 系统名，字体 `18px / weight 700`。
- 菜单项：
  - 高度 `48px`，圆角 `12px`，默认左右间距 `16px`。
  - 未选中：`color: var(--text-secondary)`，hover 背景 `rgba(0,0,0,0.04)`。
  - 选中态：背景 `var(--accent-light)`，文字 `var(--accent)`，左侧无竖条（Apple 不用左侧条，用背景药丸）。
  - 图标与文字间距 `12px`。
- 菜单分组：在底部可放“系统”等分组标题（小字、灰色、全大写、letter-spacing 0.05em）。

### 4.2 顶部 Header

- 高度：`64px`，固定在内容区上方。
- 背景：`var(--bg-primary)` + `backdrop-filter: blur(20px)` + `background-color: rgba(255,255,255,0.72)`（深色模式对应 rgba(0,0,0,0.72)）。
- 底部 1px 边框 `var(--border)`。
- 左侧：页面标题 + 简短面包屑或日期。
- 右侧：
  - 项目标签（小药丸）
  - 深色模式切换按钮（太阳/月亮图标）
  - 用户头像/姓名下拉菜单
  - 退出按钮

### 4.3 页面转场

- Vue Router 外层包裹 `<Transition name="page">`。
- 进入：opacity `0` → `1`，translateY `16px` → `0`，duration `300ms`，easing `cubic-bezier(0.25, 0.1, 0.25, 1.0)`。
- 离开：opacity `1` → `0`，duration `200ms`。

---

## 5. 登录页

### 5.1 布局

- 全屏居中，背景为缓慢流动的渐变网格动画（颜色使用 accent 的极低饱和度版本，避免喧宾夺主）。
- 中央大卡片：
  - 宽度 `440px`，padding `40px`
  - 背景 `rgba(255,255,255,0.82)` + `backdrop-filter: blur(24px)` + `border: 1px solid rgba(255,255,255,0.3)`
  - 圆角 `28px`
  - 阴影 `var(--shadow-lg)`
- 深色模式卡片：`rgba(30,30,30,0.85)` + 边框 `rgba(255,255,255,0.08)`。

### 5.2 内容

- 顶部系统图标 + 标题 `28px / weight 700`。
- 副标题：`欢迎使用工单管理系统`，`text-secondary`。
- 邮箱输入框：圆角 `12px`，聚焦时 `box-shadow: 0 0 0 4px var(--accent-light)`，边框变为 `var(--accent)`。
- 验证码行：输入框 + 胶囊按钮“发送验证码”。
- 登录按钮：全宽胶囊按钮，渐变蓝（`#0071e3 → #0077ed`），hover 轻微上移 + 阴影，active `scale(0.98)`。
- 底部：可选“记住我”小字（暂不做功能）。

### 5.3 动效

- 卡片入场：opacity + translateY `24px → 0`，duration `500ms`，delay `100ms`。
- 背景渐变：缓慢移动（CSS animation `60s` 循环）。
- 按钮加载：按钮内显示精致 spinner，文字淡出。

---

## 6. Dashboard 首页（参考 Apple Health 桌面化）

### 6.1 整体布局（桌面优先）

- 顶部 Hero：左侧欢迎语 + 日期，右侧“新建工单”大按钮。
- 下方采用 **Bento Grid** 布局（类似 Apple Health 的摘要页，但更适合宽屏）：
  - 左侧大列（约 2/3）：重要指标 + 趋势图。
  - 右侧小列（约 1/3）：状态分布 + 次要统计。
- 卡片之间有 `24px` 间隙。
- 所有卡片使用统一 `AppCard` 组件。

### 6.2 卡片内容设计

#### 卡片 1：工单总览（左上大卡片）

- 标题：工单总数
- 主数字：`workOrderTotal`，`36px / weight 700`，带 count-up 动画。
- 副数字：今日新增 `+{workOrderToday}`，绿色小字。
- 底部：近 7 天趋势 mini 柱状图（日期 + 新建/完成数），hover 显示 tooltip。
- 背景：白色/次色，无彩色背景（保持 Apple 干净感）。

#### 卡片 2：工单状态分布（右上或左中）

- 标题：工单状态
- 使用横向进度条或分段彩色条展示：
  - 待认领（info 蓝）
  - 进行中（warning 橙）
  - 待确认（purple）
  - 已确认（success 绿）
- 下方列出具体数字和百分比。
- 每个状态项 hover 时轻微高亮。

#### 卡片 3：SLA 预警（醒目大卡片）

- 标题：SLA 超期
- 主数字：`slaOverdueCount`
- 当数字 > 0 时：卡片左上角出现红色小圆点，数字用红色。
- 当数字 = 0 时：显示“全部正常” + 绿色对勾图标。
- 底部：最近一个超期工单编号/时间（可选，若后端好做则加上）。

#### 卡片 4：资源统计（右侧小卡片组）

- 三个紧凑 mini 卡片：
  - 设备总数
  - 人员总数
  - 班组数
- 每个卡片内：图标 + 数字 + 标签。

### 6.3 真实数据来源

后端新增接口：

```http
GET /api/statistics/dashboard
```

返回结构：

```json
{
  "code": 200,
  "data": {
    "workOrderTotal": 128,
    "workOrderToday": 3,
    "statusCounts": {
      "published": 12,
      "claimed": 5,
      "inProgress": 8,
      "pendingConfirm": 4,
      "confirmed": 99
    },
    "slaOverdueCount": 2,
    "deviceTotal": 45,
    "personnelTotal": 12,
    "teamTotal": 4,
    "last7Days": [
      { "date": "2026-07-24", "created": 2, "completed": 1 },
      { "date": "2026-07-25", "created": 5, "completed": 3 }
    ]
  }
}
```

后端实现：使用现有 MyBatis-Plus Mapper 的 `selectCount`、自定义 SQL 或 Service 层聚合。`last7Days` 按 `DATE(created_at)` 分组统计。

### 6.4 动效

- 数字使用 count-up，duration `1.2s`，easing `easeOutExpo`。
- 卡片依次入场：stagger `80ms`。
- 趋势图柱子从 0 高度增长，duration `800ms`。
- 状态分布进度条从 0 宽度增长到目标宽度，duration `1s`。

---

## 7. 内容页通用改造

### 7.1 卡片容器 `AppCard`

- 统一封装：背景 `var(--bg-secondary)`（light）或 `var(--bg-secondary)`（dark，保持卡片与页面区分）。
- 圆角 `20px`，padding `24px`。
- 阴影 `var(--shadow-md)`。
- hover：translateY `-4px`，阴影增强到 `var(--shadow-lg)`，transition `300ms`。
- 可选 header slot：标题 + 操作按钮区。

### 7.2 表格

- 保留 `el-table`，但外层套 `AppCard`。
- 表头：`font-weight: 500`，`color: var(--text-secondary)`，字体 `13px`。
- 行 hover：背景 `rgba(0,113,227,0.04)`，文字主色不变。
- 行高：`56px`。
- 状态标签：胶囊形，内部带小圆点 + 文字。
- 分页器：简化样式，当前页用蓝色药丸。

### 7.3 表单与按钮

- 所有主要按钮：胶囊按钮，蓝色渐变。
- 次要按钮：透明背景 + 边框。
- 危险按钮：红色，hover 加深。
- 输入框：圆角 `10px`，focus 蓝色光环。
- 查询区：卡片内顶部，使用 `el-form inline`，但按钮全部改为胶囊形。

### 7.4 加载状态

- 表格加载：Shimmer 骨架屏覆盖在表格区域，6~8 行灰色流光。
- Dashboard 加载：每张卡片显示 Shimmer 块，数字区域用长方形占位。
- 按钮 loading：精致 spinner（Element Plus 默认可接受，但颜色需匹配 accent）。

---

## 8. 动效清单汇总

| 动效 | 触发 | 时长 | 说明 |
|---|---|---|---|
| 页面进入 | 路由切换 | 300ms | opacity + translateY |
| 卡片悬浮 | hover | 300ms | translateY -4px + 阴影加深 |
| 按钮反馈 | hover/active | 150ms | hover 提亮，active scale 0.98 |
| 输入框聚焦 | focus | 200ms | 边框变蓝 + 光环 |
| 数字增长 | 数据加载完成 | 1.2s | count-up |
| 趋势图柱 | 数据加载完成 | 800ms | 高度从 0 增长 |
| 进度条 | 数据加载完成 | 1s | 宽度从 0 增长 |
| 卡片入场 | 页面进入 | 400ms | stagger 80ms |
| 表格行 hover | hover | 150ms | 背景高亮 |
| 骨架屏 | loading | 无限 | Shimmer 流光 1.5s 循环 |
| 深色模式切换 | 点击 | 300ms | 背景/文字颜色过渡 |

---

## 9. 技术方案

### 9.1 依赖

新增开发依赖：

```bash
npm install -D tailwindcss postcss autoprefixer
npx tailwindcss init -p
```

可选：

```bash
npm install countup.js
```

不使用重型图表库，趋势图用纯 CSS + div 实现。

### 9.2 Tailwind 配置

- `content`: 扫描 `src/**/*.{vue,ts,tsx}`。
- `darkMode: 'class'`。
- 扩展颜色：直接使用 CSS 变量，不在 Tailwind config 中硬编码颜色，保持 Element Plus 与 Tailwind 一致。
- 扩展阴影、圆角、间距与 Apple 设计系统对齐。

### 9.3 文件结构

```
admin-frontend/src/
├── assets/
│   ├── base.css          # 重置 + CSS 变量 + Element Plus 变量覆盖
│   ├── main.css          # Tailwind 导入 + 全局工具类
│   └── animations.css    # 全局动画 keyframes
├── components/
│   ├── AppCard.vue       # 统一卡片
│   ├── AppButton.vue     # 统一按钮（可选）
│   ├── AppSkeleton.vue   # Shimmer 骨架屏
│   ├── CountUp.vue       # 数字动画组件
│   ├── MiniTrend.vue     # 7 天趋势 mini 柱状图
│   ├── StatusTag.vue     # 胶囊状态标签
│   └── ThemeToggle.vue   # 深色模式切换
├── composables/
│   └── useTheme.ts       # 主题切换逻辑
├── layout/
│   └── index.vue         # 改造后的布局
├── router/
│   └── index.ts          # 增加 /dashboard 路由
├── store/
│   ├── user.ts           # 现有
│   └── theme.ts          # 新增
├── views/
│   ├── login/index.vue   # 改造后的登录页
│   ├── dashboard/index.vue # 新增 Dashboard
│   ├── workorder/*.vue   # 套用新样式
│   ├── device/index.vue
│   ├── personnel/index.vue
│   ├── codetable/index.vue
│   ├── sla/index.vue
│   └── team/index.vue
```

### 9.4 后端改动

新增：

- `backend/src/main/java/edu/cdut/aiback/controller/StatisticsController.java`
- `backend/src/main/java/edu/cdut/aiback/service/StatisticsService.java`
- `backend/src/main/java/edu/cdut/aiback/service/impl/StatisticsServiceImpl.java`

复用现有 Mapper：

- `WorkOrderMapper`
- `DeviceMapper`
- `PersonnelMapper`
- `TeamMapper`

接口路径：`/api/statistics/dashboard`（已在 `AuthInterceptor` 拦截 `/api/**`，但 Dashboard 需要登录后访问，符合现有逻辑）。

---

## 10. 实施顺序

1. 安装 Tailwind + 配置设计 Token + 主题 store。
2. 全局样式覆盖 Element Plus + 动画 CSS。
3. 改造 `layout/index.vue`（侧边栏、Header、ThemeToggle）。
4. 改造 `login/index.vue`。
5. 新增后端 `StatisticsController` / `Service`。
6. 新增 `views/dashboard/index.vue` + 前端 `api/statistics.ts`。
7. 创建通用组件：`AppCard`、`AppSkeleton`、`CountUp`、`MiniTrend`、`StatusTag`。
8. 改造 `workorder/list.vue`、`device/index.vue` 等页面，统一使用新组件。
9. 路由调整：根路径 `/` 改为 Dashboard，工单列表改为 `/work-order`。
10. 动效与加载状态收尾、深色模式全面测试。

---

## 11. 验收标准

- [ ] 双击 `双击运行演示.exe` 后，系统启动，浏览器打开，整体视觉明显 Apple 化。
- [ ] 登录页有毛玻璃卡片 + 背景渐变动画。
- [ ] Dashboard 显示 5 组真实数据，数字有 count-up 动画。
- [ ] 侧边栏、Header、卡片、按钮、输入框符合设计 Token。
- [ ] 路由切换有淡入上滑动画。
- [ ] 手动深色模式切换正常，所有文字对比度合格。
- [ ] 表格/表单页面视觉统一，hover 动效正常。
- [ ] 无功能回归（登录、工单 CRUD、设备导入导出等）。

---

## 12. 注意事项

- 不改业务逻辑，只改 UI/UX。
- Element Plus 组件内部功能保留，通过 CSS 变量和全局样式覆盖外观。
- 移动端适配不做为主要目标，但布局应能在 1280px 以上屏幕正常显示。
- Dashboard 后端接口只做聚合统计，不改动现有表结构。
