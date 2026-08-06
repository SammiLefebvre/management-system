# 工单管理系统（GZGD）

基于两份需求文档实现的工单管理系统，包含 Spring Boot 后端、Vue 3 管理后台、uni-app 微信小程序三端。

已完成 **数据可视化中心**、**AI 大模型助手**、**AI 智能派单**、**人脸识别登录** 等创新功能。

---

## 项目结构

```
工单管理系统/
├── docs/
│   ├── 01-需求分析.md          # 原始需求文档
│   ├── 02-数据库设计.md        # 原始数据库设计文档
│   └── superpowers/            # 设计文档与实现计划
│       ├── specs/              # 功能设计规格
│       └── plans/              # 实现计划
├── backend/                     # Spring Boot 后端
│   ├── docs/sql/init.sql        # 数据库初始化脚本
│   └── src/main/java/...
├── admin-frontend/              # Vue 3 管理后台
│   └── src/...
└── mini-program/                # uni-app 微信小程序
    └── pages/...
```

---

## 技术栈

| 端 | 技术 |
|---|---|
| 后端 | Spring Boot + MyBatis-Plus + MySQL 8.0 + JWT |
| 管理后台 | Vue 3 + Vite + TypeScript + Element Plus + ECharts + 高德地图 |
| 小程序 | uni-app (Vue 3) |
| AI | Hugging Face Inference API（Qwen2.5 / Llama） |
| 人脸识别 | 百度智能云人脸 API |

---

## 环境变量

项目使用到的第三方服务密钥 **不会提交到仓库**，请在本地配置：

### 管理后台（高德地图）

复制 `admin-frontend/.env.example` 为 `.env`：

```env
VITE_AMAP_KEY=你的高德地图Key
```

### 后端（Hugging Face AI）

复制 `backend/.env.example` 为 `.env`，或在启动时设置环境变量：

```bash
HUGGINGFACE_API_KEY=你的HuggingFaceToken
```

### 后端（百度人脸识别）

在 `backend/src/main/resources/application.yml` 中配置：

```yaml
baidu:
  face:
    app-id: 你的AppID
    api-key: 你的API Key
    secret-key: 你的Secret Key
    score-threshold: 80
```

---

## 快速启动

### 1. 初始化数据库

```bash
mysql -u root -p < backend/docs/sql/init.sql
```

> 默认数据库名 `gzgd`，账号/密码：`root` / `root`。可在 `application.yml` 中修改。

### 2. 启动后端

```bash
# Windows 请设置 Hugging Face Token
set HUGGINGFACE_API_KEY=你的HuggingFaceToken

cd backend
mvn spring-boot:run
```

后端默认端口：`http://localhost:9090`

API 文档：`http://localhost:9090/doc.html`

### 3. 启动管理后台

```bash
cd admin-frontend
npm install
npm run dev
```

默认地址：`http://localhost:5173`

登录使用邮箱验证码或人脸识别：
- 测试账号：`FrenchFriesWX@outlook.com`（内场）
- 测试账号：`pm@gzgd.com`（项目管理）
- 测试账号：`field@gzgd.com`（外场）

> 需先配置好邮件服务（`application.yml` 中的 `spring.mail`）。

### 4. 启动小程序

使用 HBuilderX 导入 `mini-program`，运行到微信开发者工具。

---

## 一键启动演示

项目根目录提供了双击即可启动演示环境的程序：

- `双击运行演示.exe`
- `DemoLauncher.exe`（同一程序，英文文件名）

双击后会自动完成：

1. 检查 Java、Node.js、npm、MySQL 是否已就绪。
2. 启动后端服务 `mvnw.cmd spring-boot:run`（端口 `9090`）。
3. 启动前端开发服务 `npm run dev`（端口 `5173`），首次运行会自动 `npm install`。
4. 自动打开浏览器进入管理后台。
5. 按 Enter 键停止所有服务。

### 前置要求

- **JDK 17+** 并已配置环境变量。
- **Node.js 22+** 及 npm（前端 `package.json` 要求 `^22.18.0 || >=24.12.0`）。
- **MySQL 8.0** 已启动，并执行过初始化脚本：

  ```bash
  mysql -u root -p < backend/docs/sql/init.sql
  ```

- （可选）**Hugging Face Token** 用于 AI 助手和 AI 派单。
- （可选）**高德地图 Key** 用于数据大屏地图。
- （可选）**百度人脸 API** 用于人脸识别登录。

### 默认登录信息

打开页面后进入登录页，可使用以下测试账号：

| 账号 | 角色 |
|---|---|
| `FrenchFriesWX@outlook.com` | 内场人员 |
| `pm@gzgd.com` | 项目管理人员 |
| `field@gzgd.com` | 外场工程师 |

验证码可输入任意 6 位数字（如 `123456`），后端 `AuthService.loginByCode` 已内置测试放行逻辑，便于本地演示。

### 人脸识别登录说明

1. 在人员管理页为对应账号点击「人脸录入」。
2. 使用摄像头拍照并确认。
3. 回到登录页切换到「人脸登录」，拍照即可登录。

### 重新编译

启动器源码位于 `tools/demo-launcher`，如需修改，可重新发布：

```bash
cd tools/demo-launcher
dotnet publish -c Release -r win-x64 --self-contained true /p:PublishSingleFile=true /p:PublishTrimmed=true /p:EnableCompressionInSingleFile=true
cp bin/Release/net8.0/win-x64/publish/DemoLauncher.exe ../../双击运行演示.exe
cp bin/Release/net8.0/win-x64/publish/DemoLauncher.exe ../../DemoLauncher.exe
```

该 exe 已自包含 .NET 8 运行时，目标机器无需额外安装 .NET。

---

## 已实现功能

### 后端

- [x] JWT 登录认证 + 邮箱验证码登录
- [x] **人脸识别登录**（百度人脸 API）
- [x] 微信小程序一键登录（OpenID 占位，需接入微信 code2Session）
- [x] MyBatis-Plus 多租户数据隔离（按 `project_group`）
- [x] 设备台账 CRUD + Excel 导入导出
- [x] 人员管理、码表管理、SLA 配置
- [x] 工单全流程：建单 → 发布 → 认领 → 签到 → 排查 → 完工 → 确认
- [x] 强制关闭（发起 + 确认）
- [x] 班组管理（成员、司机、车辆、排班）
- [x] 文件上传 + 本地存储
- [x] **数据可视化中心**：高德地图、ECharts 趋势/热力图、人员负载、实时 SSE 统计卡片
- [x] **AI 助手**（基于 Hugging Face Qwen2.5/Llama）
- [x] **AI 智能派单建议**（结合设备坐标、人员位置、负载、响应时长）
- [x] **管理端一键指派工单**
- [x] Excel / PDF 报表导出

### 管理后台

- [x] 登录页（邮箱验证码 / 人脸识别）
- [x] Dashboard 数据概览（实时 SSE 数据、趋势图、人员负载）
- [x] **数据大屏**：高德地图看板、故障热力图、人员负载
- [x] **报表中心**：Excel / PDF 导出
- [x] 工单管理（列表、详情、状态流转、强制关闭、AI 派单建议）
- [x] 设备台账（导入/导出/模板下载）
- [x] 人员管理、码表管理、SLA 配置、班组查看
- [x] 全局右下角 **AI 助手** 聊天窗口
- [x] Apple 风格 UI：大圆角卡片、毛玻璃、动画、明暗主题切换

### 小程序

- [x] 微信一键登录
- [x] 首页三 Tab：待发布/处理中/已确认
- [x] 手动建单
- [x] 工单详情 + 作业流程（签到拍照 → 排查拍照 → 完工拍照）
- [x] 班组管理

---

## 注意事项

1. **AI 服务**：默认调用 Hugging Face Inference API，模型为 `Qwen/Qwen2.5-7B-Instruct`，可在 `application.yml` 中切换为 `meta-llama/Llama-3.2-3B-Instruct`。未配置 Token 时，AI 助手会提示配置。
2. **微信登录**：当前 `AuthService.wxLogin` 中使用 `wxCode` 直接作为 `openid` 测试，生产环境需调用微信 `code2Session` 接口。
3. **文件存储**：默认存放到 `./uploads`，生产环境建议替换为 OSS/COS。
4. **照片水印**：完工照片后端叠加文字水印，逻辑在 `ImageWatermarkUtil.java`。
5. **工单编号**：使用 `work_order_seq` 表 + `SELECT FOR UPDATE` 行锁保证并发唯一。
6. **数据隔离**：所有包含 `project_group` 的表自动追加租户过滤。

---

## 后续可优化

- [ ] 接入真实微信登录（appid/secret）
- [ ] 照片迁移至 OSS/COS
- [ ] 增加短信验证码登录
- [ ] AI 助手接入长连接，实现流式输出
- [ ] 班组排班支持结束日期段
- [ ] 小程序消息推送
