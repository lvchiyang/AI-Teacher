要用 Kotlin 编写一个 **MCP（Model Context Protocol）Server**，你需要实现 MCP 协议规定的 JSON-RPC 2.0 接口，通过 **标准输入/输出（stdio）** 或 **TCP/HTTP** 与 Host（如 LLM 应用）通信。

下面我将手把手教你用 Kotlin（JVM）写一个 **stdio 模式的 MCP Server**，提供一个简单工具（例如：`add` 加法器）。

---

## ✅ 目标
- 实现一个 MCP Server
- 提供一个名为 `add` 的工具
- 通过 stdio 与 Host 通信（最常用模式）
- 使用官方 SDK：`io.modelcontextprotocol:kotlin-sdk`

---

## 第一步：添加依赖（`build.gradle.kts`）

```kotlin
plugins {
    kotlin("jvm") version "1.9.24"
    application
}

dependencies {
    implementation("io.modelcontextprotocol:kotlin-sdk:0.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
}

application {
    mainClass.set("com.example.McpServerKt") // 替换为你的主类
}
```

---

## 第二步：编写 MCP Server 代码

```kotlin
// 文件: src/main/kotlin/com/example/McpServer.kt
package com.example

import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.stdio.stdioServer
import kotlinx.coroutines.runBlocking

/**
 * 一个简单的 MCP Server，提供 "add" 工具
 */
fun main() = runBlocking {
    // 创建 stdio Server（自动从 stdin 读，向 stdout 写）
    val server = stdioServer {
        // 声明支持的工具
        tool(
            name = "add",
            description = "将两个数字相加",
            inputSchema = buildJsonObject {
                put("type", "object")
                put("properties", buildJsonObject {
                    put("a", buildJsonObject {
                        put("type", "number")
                        put("description", "第一个数字")
                    })
                    put("b", buildJsonObject {
                        put("type", "number")
                        put("description", "第二个数字")
                    })
                })
                put("required", buildJsonArray { add("a"); add("b") })
            }
        ) { request ->
            // 工具执行逻辑
            val a = request.arguments["a"]?.jsonPrimitive?.doubleOrNull
            val b = request.arguments["b"]?.jsonPrimitive?.doubleOrNull

            if (a == null || b == null) {
                throw McpError(
                    code = McpErrorCode.InvalidRequest,
                    message = "参数 a 和 b 必须是数字"
                )
            }

            val result = a + b

            // 返回结果（必须是 JsonElement）
            buildJsonObject {
                put("result", result)
            }
        }

        // 可选：处理 initialize 请求
        onInitialize { request ->
            println("Host 初始化: ${request.capabilities}")
            ServerCapabilities(
                tools = ToolCapabilities(list = true)
            )
        }
    }

    // 启动服务器（阻塞直到 stdin 关闭）
    server.start()
}
```

---

## 第三步：构建并运行 Server

### 构建可执行 JAR
```bash
./gradlew installDist
```

生成的脚本在：
```
build/install/your-app-name/bin/your-app-name
```

### 或直接运行（开发阶段）
```bash
./gradlew run
```

> ⚠️ 注意：MCP Server **必须通过 stdio 通信**，所以不要直接双击运行，而是让 **Host 进程启动它**（见下文测试方法）。

---

## 第四步：测试你的 Server

### 方法 1：用 Kotlin Host 测试（推荐）

创建一个 Host 测试脚本：

```kotlin
// TestHost.kt
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.stdio.stdioTransport
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val transport = stdioTransport {
        command = "./gradlew" // 或你构建后的可执行路径
        args = listOf("run")
        // 如果你用 installDist: command = "build/install/mcp-server/bin/mcp-server"
    }

    val client = McpClient(transport)
    client.connect()

    client.initialize(InitializeRequest("2024-10-07", ClientCapabilities()))

    val tools = client.listTools(ListToolsRequest()).tools
    println("发现工具: ${tools.map { it.name }}")

    val result = client.callTool(CallToolRequest(
        name = "add",
        arguments = mapOf("a" to 3, "b" to 5)
    ))

    println("结果: $result") // 应输出 {"result": 8}

    client.shutdown()
    client.disconnect()
}
```

### 方法 2：手动测试（高级）

你也可以用 `stdio` 手动发送 JSON-RPC 请求（不推荐，仅调试用）：

```bash
# 启动 Server
./gradlew run

# 然后在另一个终端用 nc 或自定义脚本发送初始化请求（复杂，略）
```

---

## 🔑 关键点说明

| 概念 | 说明 |
|------|------|
| `stdioServer` | 创建基于标准输入/输出的 MCP Server |
| `tool { ... }` | 注册一个工具，定义名称、schema 和执行逻辑 |
| `request.arguments` | Host 传入的参数（`JsonObject`）|
| 返回值 | 必须是 `JsonElement`（如 `JsonObject`、`JsonPrimitive`）|
| `onInitialize` | 可选，用于返回 Server 能力 |

---

## 📦 项目结构建议

```
mcp-kotlin-server/
├── build.gradle.kts
├── settings.gradle.kts
└── src/main/kotlin/com/example/
    └── McpServer.kt
```

---

## ✅ 部署建议

1. **构建 fat JAR**（包含所有依赖）：
   ```kotlin
   // build.gradle.kts
   tasks.withType<Jar> {
       duplicatesStrategy = DuplicatesStrategy.EXCLUDE
       manifest {
           attributes["Main-Class"] = "com.example.McpServerKt"
       }
       from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
   }
   ```

2. **Host 启动命令**：
   ```kotlin
   command = "java"
   args = listOf("-jar", "mcp-server-all.jar")
   ```

---

## 🌐 扩展：支持 TCP 模式（可选）

如果你不想用 stdio，也可以用 TCP：

```kotlin
val server = tcpServer(port = 8080) {
    tool("add") { ... }
}
server.start()
```

Host 端则用 `tcpTransport { host = "localhost"; port = 8080 }` 连接。

---

## 📚 参考

- [MCP Kotlin SDK GitHub](https://github.com/modelcontextprotocol/kotlin-sdk)
- [MCP 协议规范](https://github.com/modelcontextprotocol/spec)
- [官方示例（Python/TS）](https://github.com/modelcontextprotocol/servers)

---

如果你希望我帮你实现：
- 多个工具（如天气、计算器、文件读取）
- 异步工具调用
- 错误处理模板
- Docker 部署脚本

请告诉我！我可以继续完善这个 Server。