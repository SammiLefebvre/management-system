using System;
using System.Diagnostics;
using System.IO;
using System.Net.Sockets;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading;

namespace DemoLauncher;

class Program
{
    static int Main(string[] args)
    {
        Console.OutputEncoding = Encoding.UTF8;
        Console.InputEncoding = Encoding.UTF8;
        Console.Title = "工单系统演示启动器";

        string projectRoot = GetProjectRoot();

        Console.WriteLine("========================================");
        Console.WriteLine("  工单管理系统 - 一键演示启动器");
        Console.WriteLine("========================================");
        Console.WriteLine();

        // 前置环境检查
        if (!CheckJava(out string javaVersion))
        {
            ErrorAndExit("未检测到 Java。请安装 JDK 17+ 并配置 JAVA_HOME / PATH。");
            return 1;
        }
        Console.WriteLine($"✅ Java: {javaVersion}");

        if (!CheckNode(out string nodeVersion))
        {
            ErrorAndExit("未检测到 Node.js。请安装 Node.js 22+ 并配置 PATH。");
            return 1;
        }
        Console.WriteLine($"✅ Node.js: {nodeVersion}");

        if (!CheckNpm(out string npmVersion))
        {
            ErrorAndExit("未检测到 npm。请安装 Node.js 时附带 npm。");
            return 1;
        }
        Console.WriteLine($"✅ npm: {npmVersion}");

        if (!CheckMySql())
        {
            ErrorAndExit("未检测到 MySQL（localhost:3306）。请先启动 MySQL，并执行：\n" +
                         "  mysql -u root -p < backend/docs/sql/init.sql");
            return 1;
        }
        Console.WriteLine("✅ MySQL: localhost:3306 可连接");

        Console.WriteLine();

        string backendDir = Path.Combine(projectRoot, "backend");
        string frontendDir = Path.Combine(projectRoot, "admin-frontend");

        if (!File.Exists(Path.Combine(backendDir, "mvnw.cmd")))
        {
            ErrorAndExit($"未找到 backend/mvnw.cmd。\n请将本程序放在项目根目录再运行。当前目录：{projectRoot}");
            return 1;
        }

        if (!File.Exists(Path.Combine(frontendDir, "package.json")))
        {
            ErrorAndExit("未找到 admin-frontend/package.json。");
            return 1;
        }

        if (IsPortOpen("localhost", 9090))
        {
            ErrorAndExit("端口 9090 已被占用。请先关闭已有的后端服务，再运行本程序。");
            return 1;
        }
        if (IsPortOpen("localhost", 5173))
        {
            ErrorAndExit("端口 5173 已被占用。请先关闭已有的前端服务，再运行本程序。");
            return 1;
        }

        // 启动后端
        Console.WriteLine("▶ 正在启动后端服务（端口 9090）...");
        var backend = StartService("工单后端服务", backendDir, "mvnw.cmd spring-boot:run");

        if (!WaitForPort(9090, 600))
        {
            ErrorAndExit("后端服务未能在 10 分钟内启动，请查看弹出的后端服务窗口。");
            StopService(backend);
            return 1;
        }
        Console.WriteLine("✅ 后端服务已启动：http://localhost:9090");
        Console.WriteLine();

        // 启动前端
        bool hasNodeModules = Directory.Exists(Path.Combine(frontendDir, "node_modules"));
        string frontendCommand = hasNodeModules
            ? "npm run dev"
            : "npm install && npm run dev";

        Console.WriteLine($"▶ 正在启动前端服务（端口 5173）{(hasNodeModules ? "" : "，首次运行需安装依赖，请稍候...")}");
        var frontend = StartService("工单前端服务", frontendDir, frontendCommand);

        if (!WaitForPort(5173, 600))
        {
            ErrorAndExit("前端服务未能在 10 分钟内启动，请查看弹出的前端服务窗口。");
            StopService(frontend);
            StopService(backend);
            return 1;
        }
        Console.WriteLine("✅ 前端服务已启动：http://localhost:5173");
        Console.WriteLine();

        // 打开浏览器
        Console.WriteLine("▶ 正在打开浏览器...");
        OpenBrowser("http://localhost:5173");

        Console.WriteLine();
        Console.WriteLine("========================================");
        Console.WriteLine("  演示系统已就绪！");
        Console.WriteLine("  管理后台：http://localhost:5173");
        Console.WriteLine("  后端 API：http://localhost:9090");
        Console.WriteLine("  API 文档：http://localhost:9090/doc.html");
        Console.WriteLine();
        Console.WriteLine("  登录提示：");
        Console.WriteLine("    账号：FrenchFriesWX@outlook.com");
        Console.WriteLine("    账号：pm@gzgd.com");
        Console.WriteLine("    账号：field@gzgd.com");
        Console.WriteLine("    验证码：任意 6 位数字（如 123456）");
        Console.WriteLine();
        Console.WriteLine("  按 Enter 键停止所有服务并退出...");
        Console.WriteLine("========================================");

        Console.ReadLine();

        Console.WriteLine();
        Console.WriteLine("▶ 正在停止前端服务...");
        StopService(frontend);

        Console.WriteLine("▶ 正在停止后端服务...");
        StopService(backend);

        Console.WriteLine();
        Console.WriteLine("已停止所有服务。");
        Thread.Sleep(500);
        return 0;
    }

    static string GetProjectRoot()
    {
        string? exePath = Process.GetCurrentProcess().MainModule?.FileName;
        if (string.IsNullOrEmpty(exePath))
        {
            throw new InvalidOperationException("无法定位启动程序路径。");
        }
        return Path.GetDirectoryName(exePath)!;
    }

    static bool CheckJava(out string version)
    {
        version = string.Empty;
        try
        {
            var psi = new ProcessStartInfo
            {
                FileName = "cmd.exe",
                Arguments = "/c java -version 2>&1",
                UseShellExecute = false,
                RedirectStandardOutput = true,
                RedirectStandardError = true,
                CreateNoWindow = true,
            };
            using var process = Process.Start(psi);
            if (process == null) return false;
            process.WaitForExit();
            string output = process.StandardOutput.ReadToEnd() + process.StandardError.ReadToEnd();
            if (process.ExitCode != 0) return false;

            var match = Regex.Match(output, @"version ""([^""]+)""");
            version = match.Success ? match.Groups[1].Value : "unknown";
            return true;
        }
        catch
        {
            return false;
        }
    }

    static bool CheckNode(out string version)
    {
        version = string.Empty;
        try
        {
            var psi = new ProcessStartInfo
            {
                FileName = "cmd.exe",
                Arguments = "/c node -v",
                UseShellExecute = false,
                RedirectStandardOutput = true,
                CreateNoWindow = true,
            };
            using var process = Process.Start(psi);
            if (process == null) return false;
            process.WaitForExit();
            version = process.StandardOutput.ReadToEnd().Trim();
            return process.ExitCode == 0 && !string.IsNullOrEmpty(version);
        }
        catch
        {
            return false;
        }
    }

    static bool CheckNpm(out string version)
    {
        version = string.Empty;
        try
        {
            var psi = new ProcessStartInfo
            {
                FileName = "cmd.exe",
                Arguments = "/c npm -v",
                UseShellExecute = false,
                RedirectStandardOutput = true,
                CreateNoWindow = true,
            };
            using var process = Process.Start(psi);
            if (process == null) return false;
            process.WaitForExit();
            version = process.StandardOutput.ReadToEnd().Trim();
            return process.ExitCode == 0 && !string.IsNullOrEmpty(version);
        }
        catch
        {
            return false;
        }
    }

    static bool CheckMySql()
    {
        try
        {
            using var client = new TcpClient();
            var result = client.ConnectAsync("localhost", 3306).Wait(2000);
            return result && client.Connected;
        }
        catch
        {
            return false;
        }
    }

    static Process StartService(string title, string workingDirectory, string command)
    {
        var psi = new ProcessStartInfo
        {
            FileName = "cmd.exe",
            Arguments = $"/k \"title {title} && {command}\"",
            WorkingDirectory = workingDirectory,
            UseShellExecute = true,
            CreateNoWindow = false,
            WindowStyle = ProcessWindowStyle.Normal,
        };

        var process = Process.Start(psi);
        if (process == null)
        {
            throw new InvalidOperationException($"无法启动服务：{title}");
        }
        return process;
    }

    static bool WaitForPort(int port, int timeoutSeconds)
    {
        for (int i = 0; i < timeoutSeconds; i++)
        {
            if (IsPortOpen("localhost", port))
            {
                return true;
            }
            Thread.Sleep(1000);
        }
        return false;
    }

    static bool IsPortOpen(string host, int port)
    {
        try
        {
            using var client = new TcpClient();
            var connected = client.ConnectAsync(host, port).Wait(800);
            return connected && client.Connected;
        }
        catch
        {
            return false;
        }
    }

    static void OpenBrowser(string url)
    {
        try
        {
            Process.Start(new ProcessStartInfo
            {
                FileName = url,
                UseShellExecute = true,
            });
        }
        catch (Exception ex)
        {
            Console.WriteLine($"⚠️ 无法自动打开浏览器：{ex.Message}");
            Console.WriteLine($"   请手动访问：{url}");
        }
    }

    static void StopService(Process? process)
    {
        if (process == null || process.HasExited)
        {
            return;
        }
        try
        {
            Process.Start(new ProcessStartInfo
            {
                FileName = "cmd.exe",
                Arguments = $"/c taskkill /F /T /PID {process.Id}",
                UseShellExecute = true,
                CreateNoWindow = true,
            });
        }
        catch (Exception ex)
        {
            Console.WriteLine($"⚠️ 停止服务时出错（PID {process.Id}）：{ex.Message}");
        }
    }

    static void ErrorAndExit(string message)
    {
        Console.WriteLine();
        Console.WriteLine("❌ " + message);
        Console.WriteLine();
        Console.WriteLine("按 Enter 键退出...");
        Console.ReadLine();
    }
}
