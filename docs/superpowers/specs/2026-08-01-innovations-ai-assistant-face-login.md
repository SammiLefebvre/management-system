# 创新功能设计：AI 大模型助手 + 人脸识别登录

## Goal
为管理后台增加两个可演示的创新亮点：
1. 基于 Hugging Face 开源大语言模型的 AI 助手（全局对话 + 工单智能派单建议）。
2. 基于百度人脸识别 API 的人脸登录。

完成后将项目整理并推送到 GitHub。

## Tech Stack
- 后端：Spring Boot 3 + RestTemplate + Hugging Face Inference API
- 默认模型：`Qwen/Qwen2.5-7B-Instruct`
- 备用模型：`meta-llama/Llama-3.2-3B-Instruct`
- 人脸：百度 `AipFace`（已存在）
- 前端：Vue 3 + Element Plus + `<video>`/`<canvas>` 摄像头抓拍

## Security & Privacy
- **Hugging Face API Token 不得写入代码仓库**。通过环境变量 `HUGGINGFACE_API_KEY` 注入。
- 后端 `application.yml` 使用 `${HUGGINGFACE_API_KEY:}` 占位。
- 登录页和 README 提示用户自行申请 Token。

## Feature 1: AI 大模型助手

### 1.1 通用对话
- 后端接口：`POST /api/ai/chat`
- 请求：`{ "message": "今天有几条超期工单？" }`
- 后端把当前系统数据摘要拼成 system prompt，连同用户消息一起发给 Hugging Face。
- 返回：`{ "data": "..." }`

### 1.2 AI 智能派单建议
- 后端接口：`POST /api/ai/dispatch/advice?workOrderId=...`
- 取工单详情、设备坐标、同项目组的空闲/可用外场工程师列表及其坐标、当前负载。
- 构造 prompt 让模型推荐最合适的一位工程师，并说明理由。
- 返回：`{ "data": { "personnelId": 1, "name": "张三", "reason": "..." } }`

### 1.3 Prompt 设计原则
- 使用 Qwen chat template：`<|im_start|>system\n...<|im_end|>\n<|im_start|>user\n...<|im_end|>\n<|im_start|>assistant\n`
- system prompt 明确告诉模型：只基于提供的数据回答，不要编造。
- 控制上下文长度，避免超出免费 tier token 限制。

## Feature 2: 人脸识别登录

### 2.1 人脸登录
- 新增 `POST /api/auth/face-login`
- 接收 `imageBase64`，调百度 `AipFace` 的 `search`。
- 匹配分数 ≥ 阈值时，取 `user_id`（对应 personnel.account）。
- 查询 personnel，签发 JWT，返回 `LoginResponse`。

### 2.2 人脸录入
- 复用已有 `POST /api/face/register/base64`
- 前端在人员管理页为每位人员增加「录入人脸」按钮。
- 调用时 `groupId = gzgd_users`，`userId = personnel.account`。

## Feature 3: 项目整理 & GitHub

- 更新 README：功能介绍、启动方式、环境变量配置、演示账号、创新点说明。
- 补全 `.env.example`、`.gitignore`。
- 初始化本地 git 仓库，提交全部代码，并关联 GitHub 远程仓库推送。

## Open Questions (Resolved)
- 使用 Hugging Face 而非 DashScope，因为用户要求免费开源模型。
- 不自己训练模型，直接调用 Inference API。
