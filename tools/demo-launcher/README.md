# 工单系统演示启动器

`双击运行演示.exe` / `DemoLauncher.exe` 的源代码。

## 功能

双击后自动完成：

1. 检查 Java、Node.js、npm、MySQL 环境。
2. 启动后端 Spring Boot 服务（`mvnw.cmd spring-boot:run`，端口 9090）。
3. 启动前端 Vite 开发服务（`npm run dev`，端口 5173），首次运行会自动 `npm install`。
4. 自动打开浏览器进入管理后台。
5. 按 Enter 键停止所有服务。

## 前置要求

- JDK 17+
- Node.js 22+（与 `admin-frontend/package.json` 的 `engines` 一致）
- MySQL 8.0 已启动，并执行过 `backend/docs/sql/init.sql`

## 技术栈

- .NET 8 控制台应用
- 发布为单文件、自包含、已裁剪的 Windows x64 可执行文件

## 重新编译

```bash
cd tools/demo-launcher
dotnet publish -c Release -r win-x64 --self-contained true /p:PublishSingleFile=true /p:PublishTrimmed=true /p:EnableCompressionInSingleFile=true
```

编译产物位于：

```
tools/demo-launcher/bin/Release/net8.0/win-x64/publish/DemoLauncher.exe
```

复制到项目根目录：

```bash
cp bin/Release/net8.0/win-x64/publish/DemoLauncher.exe ../../双击运行演示.exe
cp bin/Release/net8.0/win-x64/publish/DemoLauncher.exe ../../DemoLauncher.exe
```
