# Work Order Management System (GZGD)

A full-stack work order management system for field maintenance operations, featuring a **Spring Boot** backend, a **Vue 3** admin dashboard, and a **uni-app** WeChat mini-program. The system includes **AI assistant / AI dispatch recommendations**, **face recognition login**, **data visualization center**, and real-time statistics.

---

## English Documentation

### Project Overview

This system is designed to manage work orders for field maintenance teams. It covers the full workflow from ticket creation to completion, supports multi-role access control (internal staff, field engineers, project managers, company managers), and provides data isolation by project group.

### Project Structure

```
management-system/
├── docs/                          # Requirements and design docs
│   ├── 01-需求分析.md
│   ├── 02-数据库设计.md
│   └── superpowers/
│       ├── specs/                 # Feature design specs
│       └── plans/                 # Implementation plans
├── backend/                       # Spring Boot backend
│   ├── docs/sql/init.sql          # Database initialization script
│   └── src/main/java/...
├── admin-frontend/                # Vue 3 admin dashboard
│   └── src/...
└── mini-program/                  # uni-app WeChat mini-program
    └── pages/...
```

### Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3 + MyBatis-Plus + MySQL 8.0 + JWT |
| Admin Dashboard | Vue 3 + Vite + TypeScript + Element Plus + ECharts + AMap |
| Mini Program | uni-app (Vue 3) |
| AI | Hugging Face Inference API (Qwen2.5 / Llama) |
| Face Recognition | Baidu AI Cloud Face API |

### Environment Variables

All real secrets stay on your local machine and are **never committed** to the repository.

#### Admin Dashboard — AMap Key

Copy `admin-frontend/.env.example` to `admin-frontend/.env` and fill in your key:

```env
VITE_AMAP_KEY=your-amap-key
```

#### Backend — Baidu Face Recognition

Edit `backend/src/main/resources/application.yml`:

```yaml
baidu:
  face:
    app-id: your-app-id
    api-key: your-api-key
    secret-key: your-secret-key
    score-threshold: 80
```

#### Hugging Face API Token

The backend does **not** require a Hugging Face API key in any config file or environment variable. When a user first clicks the AI Assistant or the AI Dispatch Recommendation button, the admin dashboard asks for a Hugging Face API token. The token is stored in the browser's local storage and sent to the backend only for the AI request. The backend does not store it.

### Quick Start

#### 1. Initialize the Database

```bash
mysql -u root -p < backend/docs/sql/init.sql
```

Default database name: `gzgd`, user/password: `root` / `root`. You can change these in `application.yml`.

#### 2. Start the Backend

```bash
cd backend
mvnw.cmd spring-boot:run
```

Backend URL: `http://localhost:9090`

API docs: `http://localhost:9090/doc.html`

#### 3. Start the Admin Dashboard

```bash
cd admin-frontend
npm install
npm run dev
```

Dashboard URL: `http://localhost:5173`

#### 4. Start the Mini Program

Import the `mini-program` folder into HBuilderX and run it in the WeChat Developer Tools.

### One-Click Demo Launcher

The project root contains a double-click executable:

- `双击运行演示.exe`
- `DemoLauncher.exe` (same file, English name)

When double-clicked, it automatically:

1. Checks for Java, Node.js, npm, and MySQL.
2. Starts the backend with `mvnw.cmd spring-boot:run` on port `9090`.
3. Starts the frontend dev server with `npm run dev` on port `5173` (and runs `npm install` if needed).
4. Opens the browser at `http://localhost:5173`.
5. Stops all services when you press Enter.

### Prerequisites

- JDK 17+
- Node.js 22+ and npm
- MySQL 8.0 started and initialized with `init.sql`
- Optional: AMap key for the map dashboard
- Optional: Hugging Face token for AI features (entered at runtime)
- Optional: Baidu Face API credentials for face login

### Demo Accounts

On the login page you can use the verification code `123456` for any of the following accounts:

| Account | Role |
|---|---|
| `FrenchFriesWX@outlook.com` | Internal staff |
| `pm@gzgd.com` | Project manager |
| `field@gzgd.com` | Field engineer |

### Face Recognition Login

1. Go to **Personnel Management** and click **Register Face** for the account you want to use.
2. Take a photo with the camera and confirm.
3. On the login page, switch to the **Face Login** tab and take a photo to log in.

### Implemented Features

#### Backend

- JWT authentication + email verification code login
- Face recognition login (Baidu Face API)
- WeChat mini-program login (OpenID placeholder, needs WeChat `code2Session` integration)
- MyBatis-Plus multi-tenant data isolation by `project_group`
- Device ledger CRUD + Excel import/export
- Personnel management, code table management, SLA configuration
- Full work order lifecycle: create → publish → claim → check-in → process → complete → confirm
- Force close (request + confirm)
- Team management (members, drivers, vehicles, scheduling)
- File upload + local storage
- Data visualization center: AMap dashboard, ECharts trends/heatmap, workload charts, real-time SSE stat cards
- AI assistant (Hugging Face Qwen2.5 / Llama)
- AI dispatch recommendation (combines device coordinates, personnel location, workload, response time)
- Manager one-click work order assignment
- Excel / PDF report export

#### Admin Dashboard

- Login page (email code / face recognition)
- Dashboard with real-time SSE data, trend charts, and workload charts
- Data visualization center: AMap dashboard, fault heatmap, workload
- Report center: Excel / PDF export
- Work order management (list, detail, status flow, force close, AI dispatch recommendation)
- Device ledger (import/export/template download)
- Personnel management, code table management, SLA configuration, team view
- Global floating **AI Assistant** chat widget
- Apple-style UI: large rounded cards, glassmorphism, animations, light/dark theme toggle

#### Mini Program

- WeChat one-click login
- Three tabs: pending publish / in progress / confirmed
- Manual work order creation
- Work order detail + operation flow (check-in photos → process photos → completion photos)
- Team management

### Notes

1. **AI Service**: The backend calls the Hugging Face Inference API. The default model is `Qwen/Qwen2.5-7B-Instruct`, and you can switch to `meta-llama/Llama-3.2-3B-Instruct` in `application.yml`. If no token is provided at runtime, the AI features will prompt the user to enter one.
2. **WeChat Login**: `AuthService.wxLogin` currently uses `wxCode` as a placeholder OpenID. Production usage requires calling WeChat `code2Session`.
3. **File Storage**: Files are stored under `./uploads` by default. For production, migrate to OSS/COS.
4. **Photo Watermark**: Completion photos are watermarked on the backend. See `ImageWatermarkUtil.java`.
5. **Work Order Code**: Uses the `work_order_seq` table with `SELECT FOR UPDATE` row locking to guarantee uniqueness under concurrency.
6. **Data Isolation**: All tables containing `project_group` are automatically filtered by tenant.

### Roadmap

- Integrate real WeChat login (`appid`/`secret`)
- Migrate photos to OSS/COS
- Add SMS verification code login
- Stream AI assistant responses
- Support team schedule date ranges
- Mini-program push notifications

---

## 中文文档

### 项目简介

本系统是一套面向外场维护团队的工单管理系统，包含工单全生命周期管理、多角色权限控制（内场/外场/项目管理/公司管理），并按项目组进行数据隔离。

### 项目结构

```
management-system/
├── docs/                          # 需求与设计文档
│   ├── 01-需求分析.md
│   ├── 02-数据库设计.md
│   └── superpowers/
│       ├── specs/                 # 功能设计规格
│       └── plans/                 # 实现计划
├── backend/                       # Spring Boot 后端
│   ├── docs/sql/init.sql          # 数据库初始化脚本
│   └── src/main/java/...
├── admin-frontend/                # Vue 3 管理后台
│   └── src/...
└── mini-program/                  # uni-app 微信小程序
    └── pages/...
```

### 技术栈

| 端 | 技术 |
|---|---|
| 后端 | Spring Boot 3 + MyBatis-Plus + MySQL 8.0 + JWT |
| 管理后台 | Vue 3 + Vite + TypeScript + Element Plus + ECharts + 高德地图 |
| 小程序 | uni-app (Vue 3) |
| AI | Hugging Face Inference API（Qwen2.5 / Llama） |
| 人脸识别 | 百度智能云人脸 API |

### 环境变量

所有真实密钥都保存在本地，**不会提交到仓库**。

#### 管理后台 — 高德地图 Key

复制 `admin-frontend/.env.example` 为 `admin-frontend/.env`，填入：

```env
VITE_AMAP_KEY=你的高德地图Key
```

#### 后端 — 百度人脸识别

编辑 `backend/src/main/resources/application.yml`：

```yaml
baidu:
  face:
    app-id: 你的AppID
    api-key: 你的APIKey
    secret-key: 你的SecretKey
    score-threshold: 80
```

#### Hugging Face API Token

后端**不需要**在任何配置文件或环境变量中配置 Hugging Face API Key。当用户首次点击 AI 助手或 AI 派单建议时，管理后台会提示输入 Hugging Face API Token。Token 保存在浏览器本地，仅在请求 AI 接口时发送给后端，后端不会存储。

### 快速启动

#### 1. 初始化数据库

```bash
mysql -u root -p < backend/docs/sql/init.sql
```

默认数据库名 `gzgd`，账号/密码：`root` / `root`。可在 `application.yml` 中修改。

#### 2. 启动后端

```bash
cd backend
mvnw.cmd spring-boot:run
```

后端地址：`http://localhost:9090`

API 文档：`http://localhost:9090/doc.html`

#### 3. 启动管理后台

```bash
cd admin-frontend
npm install
npm run dev
```

管理后台地址：`http://localhost:5173`

#### 4. 启动小程序

使用 HBuilderX 导入 `mini-program` 文件夹，运行到微信开发者工具。

### 一键启动演示

项目根目录提供了双击即可运行的程序：

- `双击运行演示.exe`
- `DemoLauncher.exe`（同一文件，英文文件名）

双击后自动完成：

1. 检查 Java、Node.js、npm、MySQL 是否就绪。
2. 启动后端 `mvnw.cmd spring-boot:run`（端口 `9090`）。
3. 启动前端开发服务 `npm run dev`（端口 `5173`），首次运行会自动 `npm install`。
4. 打开浏览器进入 `http://localhost:5173`。
5. 按 Enter 键停止所有服务。

### 环境要求

- JDK 17+
- Node.js 22+ 及 npm
- MySQL 8.0 已启动并执行过 `init.sql`
- 可选：高德地图 Key（用于数据大屏地图）
- 可选：Hugging Face Token（使用 AI 功能时在前端输入）
- 可选：百度人脸 API 凭证（用于人脸识别登录）

### 演示账号

登录页验证码可输入任意 6 位数字，例如 `123456`：

| 账号 | 角色 |
|---|---|
| `FrenchFriesWX@outlook.com` | 内场人员 |
| `pm@gzgd.com` | 项目管理人员 |
| `field@gzgd.com` | 外场工程师 |

### 人脸识别登录

1. 进入 **人员管理**，为需要使用的账号点击 **人脸录入**。
2. 使用摄像头拍照并确认。
3. 在登录页切换到 **人脸登录** Tab，拍照即可登录。

### 已实现功能

#### 后端

- JWT 认证 + 邮箱验证码登录
- 人脸识别登录（百度人脸 API）
- 微信小程序一键登录（OpenID 占位，需接入微信 `code2Session`）
- MyBatis-Plus 按 `project_group` 多租户数据隔离
- 设备台账 CRUD + Excel 导入导出
- 人员管理、码表管理、SLA 配置
- 工单全生命周期：建单 → 发布 → 认领 → 签到 → 排查 → 完工 → 确认
- 强制关闭（发起 + 确认）
- 班组管理（成员、司机、车辆、排班）
- 文件上传 + 本地存储
- 数据可视化中心：高德地图看板、ECharts 趋势/热力图、人员负载、SSE 实时统计卡片
- AI 助手（Hugging Face Qwen2.5 / Llama）
- AI 智能派单建议（结合设备坐标、人员位置、负载、响应时长）
- 管理端一键指派工单
- Excel / PDF 报表导出

#### 管理后台

- 登录页（邮箱验证码 / 人脸识别）
- Dashboard 数据概览（实时 SSE 数据、趋势图、人员负载）
- 数据大屏：高德地图看板、故障热力图、人员负载
- 报表中心：Excel / PDF 导出
- 工单管理（列表、详情、状态流转、强制关闭、AI 派单建议）
- 设备台账（导入/导出/模板下载）
- 人员管理、码表管理、SLA 配置、班组查看
- 全局右下角 **AI 助手** 聊天窗口
- Apple 风格 UI：大圆角卡片、毛玻璃、动画、明暗主题切换

#### 小程序

- 微信一键登录
- 首页三 Tab：待发布 / 处理中 / 已确认
- 手动建单
- 工单详情 + 作业流程（签到拍照 → 排查拍照 → 完工拍照）
- 班组管理

### 注意事项

1. **AI 服务**：后端调用 Hugging Face Inference API，默认模型 `Qwen/Qwen2.5-7B-Instruct`，可在 `application.yml` 切换为 `meta-llama/Llama-3.2-3B-Instruct`。运行时未提供 Token 会提示用户输入。
2. **微信登录**：当前 `AuthService.wxLogin` 使用 `wxCode` 作为 OpenID 占位，生产环境需调用微信 `code2Session`。
3. **文件存储**：默认存放到 `./uploads`，生产环境建议迁移到 OSS/COS。
4. **照片水印**：完工照片后端叠加文字水印，逻辑见 `ImageWatermarkUtil.java`。
5. **工单编号**：使用 `work_order_seq` 表 + `SELECT FOR UPDATE` 行锁保证并发唯一。
6. **数据隔离**：所有包含 `project_group` 的表自动追加租户过滤。

### 后续优化

- 接入真实微信登录（appid/secret）
- 照片迁移至 OSS/COS
- 增加短信验证码登录
- AI 助手接入流式输出
- 班组排班支持结束日期段
- 小程序消息推送
