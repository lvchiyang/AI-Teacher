# MCP Kotlin SDK - 完整指南

## 概述

MCP Kotlin SDK 是一个 Kotlin 多平台实现，用于构建 Model Context Protocol (MCP) 客户端和服务器。它提供了完整的 MCP 协议支持，包括工具调用、资源管理、提示模板等功能。

## 架构概览

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   MCP Client    │◄──►│  Transport      │◄──►│   MCP Server    │
│                 │    │  (Stdio/SSE/WS)  │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### 核心模块

1. **kotlin-sdk-core**: 核心协议类型和传输抽象
2. **kotlin-sdk-client**: 客户端实现
3. **kotlin-sdk-server**: 服务器实现
4. **kotlin-sdk**: 完整 SDK 包

## 安装和配置

### Gradle 配置

#### 1. 简单 JVM 项目 (推荐新手)

```kotlin
// build.gradle.kts
plugins {
    kotlin("jvm") version "2.2.10"
    kotlin("plugin.serialization") version "2.2.10"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    // MCP SDK - 选择您需要的模块
    implementation("io.modelcontextprotocol:kotlin-sdk:0.7.4-SNAPSHOT")
    // 或者分别添加
    // implementation("io.modelcontextprotocol:kotlin-sdk-client:0.7.4-SNAPSHOT")
    // implementation("io.modelcontextprotocol:kotlin-sdk-server:0.7.4-SNAPSHOT")
    
    // Ktor 依赖 (根据您的传输方式选择)
    implementation("io.ktor:ktor-client-cio:3.3.0")        // 客户端
    implementation("io.ktor:ktor-server-netty:3.3.0")       // 服务器
    implementation("io.ktor:ktor-server-sse:3.3.0")        // SSE 支持
    implementation("io.ktor:ktor-server-websockets:3.3.0") // WebSocket 支持
    
    // 日志
    implementation("io.github.oshai:kotlin-logging:7.0.13")
    implementation("org.slf4j:slf4j-simple:2.0.17")
    
    // 测试
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("com.yourcompany.yourapp.MainKt")
}
```

#### 2. 多平台项目 (支持 JVM + Web)

```kotlin
// build.gradle.kts
plugins {
    kotlin("multiplatform") version "2.2.10"
    kotlin("plugin.serialization") version "2.2.10"
    application
}

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        withJava()
        jvmToolchain(17)
    }
    
    js(IR) {
        browser()
        nodejs()
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.modelcontextprotocol:kotlin-sdk:0.7.4-SNAPSHOT")
                implementation("io.ktor:ktor-client-core:3.3.0")
                implementation("io.github.oshai:kotlin-logging:7.0.13")
            }
        }
        
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-server-netty:3.3.0")
                implementation("io.ktor:ktor-server-sse:3.3.0")
                implementation("io.ktor:ktor-server-websockets:3.3.0")
                implementation("org.slf4j:slf4j-simple:2.0.17")
            }
        }
        
        val jsMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-js:3.3.0")
            }
        }
    }
}
```

#### 3. 使用版本目录管理 (推荐大型项目)

```toml
# gradle/libs.versions.toml
[versions]
kotlin = "2.2.10"
mcp-kotlin = "0.7.4-SNAPSHOT"
ktor = "3.3.0"
logging = "7.0.13"

[libraries]
mcp-kotlin-sdk = { group = "io.modelcontextprotocol", name = "kotlin-sdk", version.ref = "mcp-kotlin" }
mcp-kotlin-client = { group = "io.modelcontextprotocol", name = "kotlin-sdk-client", version.ref = "mcp-kotlin" }
mcp-kotlin-server = { group = "io.modelcontextprotocol", name = "kotlin-sdk-server", version.ref = "mcp-kotlin" }
ktor-client-cio = { group = "io.ktor", name = "ktor-client-cio", version.ref = "ktor" }
ktor-server-netty = { group = "io.ktor", name = "ktor-server-netty", version.ref = "ktor" }
kotlin-logging = { group = "io.github.oshai", name = "kotlin-logging", version.ref = "logging" }

[plugins]
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

```kotlin
// build.gradle.kts
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

dependencies {
    implementation(libs.mcp.kotlin.sdk)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.server.netty)
    implementation(libs.kotlin.logging)
}
```

#### 4. 完整的企业级配置

```kotlin
// build.gradle.kts
plugins {
    kotlin("jvm") version "2.2.10"
    kotlin("plugin.serialization") version "2.2.10"
    id("org.jlleitschuh.gradle.ktlint") version "13.1.0"
    id("org.jetbrains.kotlinx.kover") version "0.9.3"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    // MCP SDK
    implementation("io.modelcontextprotocol:kotlin-sdk:0.7.4-SNAPSHOT")
    
    // Ktor
    implementation(platform("io.ktor:ktor-bom:3.3.0"))
    implementation("io.ktor:ktor-client-cio")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-server-sse")
    implementation("io.ktor:ktor-server-websockets")
    implementation("io.ktor:ktor-client-logging")
    
    // 日志
    implementation("io.github.oshai:kotlin-logging:7.0.13")
    implementation("org.slf4j:slf4j-simple:2.0.17")
    
    // 测试
    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-assertions-core:6.0.4")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

application {
    mainClass.set("com.yourcompany.yourapp.MainKt")
}

// 代码质量
ktlint {
    filter {
        exclude("**/generated*/**")
    }
}

// 测试覆盖率
kover {
    reports {
        total {
            verify {
                rule {
                    minBound(80)
                }
            }
        }
    }
}
```

### 版本兼容性

- **Kotlin**: 2.2.10 (最新稳定版)
- **JVM**: 1.8+ (推荐 17 或 21)
- **平台支持**: JVM, Wasm/JS, Native
- **协议版本**: 2025-03-26 (最新), 2024-11-05 (兼容)
- **MCP SDK**: 0.7.4-SNAPSHOT (当前开发版)

### 常用依赖组合

#### 仅客户端项目：
```kotlin
implementation("io.modelcontextprotocol:kotlin-sdk-client:0.7.4-SNAPSHOT")
implementation("io.ktor:ktor-client-cio:3.3.0")
```

#### 仅服务器项目：
```kotlin
implementation("io.modelcontextprotocol:kotlin-sdk-server:0.7.4-SNAPSHOT")
implementation("io.ktor:ktor-server-netty:3.3.0")
implementation("io.ktor:ktor-server-sse:3.3.0")
```

#### 完整项目：
```kotlin
implementation("io.modelcontextprotocol:kotlin-sdk:0.7.4-SNAPSHOT")
implementation("io.ktor:ktor-client-cio:3.3.0")
implementation("io.ktor:ktor-server-netty:3.3.0")
```

### Gradle 配置选择指南

根据您的项目需求选择合适的配置：

| 项目类型 | 推荐配置 | 适用场景 |
|---------|---------|---------|
| **初学者项目** | 简单 JVM 项目 | 学习 MCP、快速原型 |
| **Web 应用** | 多平台项目 | 需要浏览器支持 |
| **大型项目** | 版本目录管理 | 团队协作、版本控制 |
| **企业项目** | 企业级配置 | 生产环境、代码质量 |

#### 配置选择决策树

```
您的项目是？
├── 学习/原型项目 → 简单 JVM 项目
├── 需要 Web 支持 → 多平台项目
├── 团队协作项目 → 版本目录管理
└── 生产环境项目 → 企业级配置
```

## 快速开始

### 1. 创建简单服务器

```kotlin
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.server.*

fun createSimpleServer(): Server {
    val server = Server(
        serverInfo = Implementation(
            name = "simple-server",
            version = "1.0.0"
        ),
        options = ServerOptions(
            capabilities = ServerCapabilities(
                tools = ServerCapabilities.Tools(listChanged = true)
            )
        )
    )

    // 添加一个简单的工具
    server.addTool(
        name = "echo",
        description = "Echo back the input",
        inputSchema = Tool.Input(
            properties = buildJsonObject {
                putJsonObject("message") {
                    put("type", "string")
                    put("description", "Message to echo")
                }
            },
            required = listOf("message")
        )
    ) { request ->
        val message = request.arguments["message"]?.jsonPrimitive?.content ?: "No message"
        CallToolResult(
            content = listOf(TextContent("Echo: $message"))
        )
    }

    return server
}
```

### 2. 创建简单客户端

```kotlin
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.client.*

fun createSimpleClient(): Client {
    return Client(
        clientInfo = Implementation(
            name = "simple-client",
            version = "1.0.0"
        )
    )
}
```

### 3. 连接和通信

```kotlin
import kotlinx.coroutines.runBlocking
import kotlinx.io.Source
import kotlinx.io.Sink

fun main() = runBlocking {
    // 创建服务器和客户端
    val server = createSimpleServer()
    val client = createSimpleClient()
    
    // 创建传输
    val transport = StdioServerTransport(
        System.`in` as Source,
        System.out as Sink
    )
    
    // 连接服务器
    val session = server.connect(transport)
    
    // 客户端连接到服务器
    client.connect(transport)
    
    // 调用工具
    val result = client.callTool(
        name = "echo",
        arguments = mapOf("message" to "Hello, MCP!")
    )
    
    println("Result: ${result?.content?.firstOrNull()}")
}
```

## 传输方式详解

### 1. Stdio 传输

**适用场景**: 命令行工具、进程间通信

```kotlin
// 服务器端
val transport = StdioServerTransport(
    inputStream = System.`in` as Source,
    outputStream = System.out as Sink
)

// 客户端
val transport = StdioClientTransport(
    input = processInputStream as Source,
    output = processOutputStream as Sink
)
```

### 2. WebSocket 传输

**适用场景**: 实时双向通信、Web 应用

```kotlin
// 服务器端
embeddedServer(CIO, host = "127.0.0.1", port = 8080) {
    install(WebSockets)
    routing {
        webSocket("/mcp") {
            val transport = WebSocketServerTransport(this)
            val session = server.connect(transport)
            session.onClose { println("WebSocket closed") }
        }
    }
}.startSuspend(wait = true)

// 客户端
val httpClient = HttpClient {
    install(WebSockets)
}

val transport = WebSocketClientTransport(
    client = httpClient,
    urlString = "ws://localhost:8080/mcp",
    requestBuilder = { /* 可选：自定义请求构建器 */ }
)
```

### 3. SSE 传输

**适用场景**: 服务器推送、Web 应用

```kotlin
// 服务器端
embeddedServer(CIO, host = "127.0.0.1", port = 8080) {
    install(SSE)
    routing {
        mcp("/mcp") {
            server
        }
    }
}.startSuspend(wait = true)

// 客户端
val transport = SseClientTransport(
    client = httpClient,
    urlString = "http://localhost:8080/sse",
    reconnectionTime = null, // 可选：重连时间
    requestBuilder = { /* 可选：自定义请求构建器 */ }
)
```

## 高级功能

### 1. 自定义传输

```kotlin
class CustomTransport : AbstractTransport() {
    override suspend fun start() {
        // 实现自定义传输逻辑
    }
    
    override suspend fun send(message: JSONRPCMessage) {
        // 实现消息发送逻辑
    }
    
    override suspend fun close() {
        // 实现清理逻辑
    }
}
```

### 2. 自定义请求处理器

```kotlin
// 服务器端
server.setRequestHandler<CustomRequest>(Method.Custom("custom_method")) { request, _ ->
    CustomResult(/* 处理结果 */)
}

// 客户端
client.setRequestHandler<CustomRequest>(Method.Custom("custom_method")) { request, _ ->
    CustomResult(/* 处理结果 */)
}
```

### 3. 进度通知

```kotlin
// 服务器端发送进度通知
session.sendNotification(
    ProgressNotification(
        ProgressNotification.Params(
            progress = 0.5,
            progressToken = progressToken,
            total = 100.0,
            message = "Processing 50% complete"
        )
    )
)
```

### 4. 资源订阅

```kotlin
// 客户端订阅资源更新
client.subscribeResource(
    SubscribeRequest(uri = "file:///documents/readme.md")
)

// 服务器端发送资源更新通知
session.sendNotification(
    ResourceUpdatedNotification(
        ResourceUpdatedNotification.Params(uri = "file:///documents/readme.md")
    )
)
```

## 实际应用示例

### 1. 文件管理服务器

```kotlin
class FileManagerServer {
    private val server = Server(
        serverInfo = Implementation(name = "file-manager", version = "1.0.0"),
        options = ServerOptions(
            capabilities = ServerCapabilities(
                tools = ServerCapabilities.Tools(listChanged = true),
                resources = ServerCapabilities.Resources(
                    subscribe = true,
                    listChanged = true
                )
            )
        )
    )

    init {
        setupTools()
        setupResources()
    }

    private fun setupTools() {
        // 列出文件工具
        server.addTool(
            name = "list_files",
            description = "List files in a directory",
            inputSchema = Tool.Input(
                properties = buildJsonObject {
                    putJsonObject("path") {
                        put("type", "string")
                        put("description", "Directory path")
                    }
                },
                required = listOf("path")
            )
        ) { request ->
            val path = request.arguments["path"]?.jsonPrimitive?.content ?: "."
            val files = File(path).listFiles()?.map { it.name } ?: emptyList()
            
            CallToolResult(
                content = listOf(TextContent(files.joinToString("\n")))
            )
        }

        // 读取文件工具
        server.addTool(
            name = "read_file",
            description = "Read contents of a file",
            inputSchema = Tool.Input(
                properties = buildJsonObject {
                    putJsonObject("path") {
                        put("type", "string")
                        put("description", "File path")
                    }
                },
                required = listOf("path")
            )
        ) { request ->
            val path = request.arguments["path"]?.jsonPrimitive?.content
            if (path == null) {
                return@addTool CallToolResult(
                    content = listOf(TextContent("Error: No path provided")),
                    isError = true
                )
            }
            
            try {
                val content = File(path).readText()
                CallToolResult(
                    content = listOf(TextContent(content))
                )
            } catch (e: Exception) {
                CallToolResult(
                    content = listOf(TextContent("Error: ${e.message}")),
                    isError = true
                )
            }
        }
    }

    private fun setupResources() {
        // 添加文件资源
        server.addResource(
            uri = "file:///documents",
            name = "Documents",
            description = "User documents directory",
            mimeType = "text/plain"
        ) { request ->
            val files = File("/documents").listFiles()?.map { file ->
                TextResourceContents(
                    text = file.name,
                    uri = "file://${file.absolutePath}",
                    mimeType = "text/plain"
                )
            } ?: emptyList()
            
            ReadResourceResult(contents = files)
        }
    }

    fun getServer() = server
}
```

### 2. AI 助手客户端

```kotlin
class AIAssistantClient : AutoCloseable {
    private val client = Client(
        clientInfo = Implementation(name = "ai-assistant", version = "1.0.0"),
        options = ClientOptions(
            capabilities = ClientCapabilities(
                roots = ClientCapabilities.Roots(listChanged = true)
            )
        )
    )
    
    private val anthropic = AnthropicOkHttpClient.fromEnv()
    private lateinit var tools: List<ToolUnion>

    suspend fun connectToServer(serverPath: String) {
        // 启动服务器进程
        val process = ProcessBuilder("node", serverPath).start()
        
        val transport = StdioClientTransport(
            input = process.inputStream as Source,
            output = process.outputStream as Sink
        )
        
        client.connect(transport)
        
        // 获取可用工具
        val toolsResult = client.listTools()
        tools = toolsResult.tools.map { tool ->
            ToolUnion.ofTool(
                Tool.builder()
                    .name(tool.name)
                    .description(tool.description ?: "")
                    .inputSchema(convertInputSchema(tool.inputSchema))
                    .build()
            )
        }
    }

    suspend fun processQuery(query: String): String {
        val messages = mutableListOf(
            MessageParam.builder()
                .role(MessageParam.Role.USER)
                .content(query)
                .build()
        )

        val response = anthropic.messages().create(
            MessageCreateParams.builder()
                .model(Model.CLAUDE_4_SONNET_20250514)
                .messages(messages)
                .tools(tools)
                .maxTokens(1024)
                .build()
        )

        val finalText = mutableListOf<String>()
        
        response.content().forEach { content ->
            when {
                content.isText() -> {
                    finalText.add(content.text().getOrNull()?.text() ?: "")
                }
                content.isToolUse() -> {
                    val toolUse = content.toolUse().get()
                    val toolName = toolUse.name()
                    val toolArgs = toolUse._input().convert(object : TypeReference<Map<String, JsonValue>>() {})
                    
                    // 调用 MCP 工具
                    val result = client.callTool(
                        name = toolName,
                        arguments = toolArgs ?: emptyMap()
                    )
                    
                    finalText.add("[Tool $toolName called]")
                    
                    // 添加工具结果
                    messages.add(
                        MessageParam.builder()
                            .role(MessageParam.Role.USER)
                            .content("Tool result: ${result?.content?.joinToString()}")
                            .build()
                    )
                    
                    // 获取最终响应
                    val finalResponse = anthropic.messages().create(
                        MessageCreateParams.builder()
                            .messages(messages)
                            .build()
                    )
                    
                    finalText.add(finalResponse.content().first().text().getOrNull()?.text() ?: "")
                }
            }
        }
        
        return finalText.joinToString("\n")
    }

    override fun close() {
        runBlocking {
            client.close()
            anthropic.close()
        }
    }
}
```

## 部署和运维

### 1. Docker 部署

```dockerfile
FROM openjdk:17-jre-slim

COPY build/libs/my-mcp-server.jar /app/server.jar

EXPOSE 8080

CMD ["java", "-jar", "/app/server.jar"]
```

### 2. Kubernetes 部署

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mcp-server
spec:
  replicas: 3
  selector:
    matchLabels:
      app: mcp-server
  template:
    metadata:
      labels:
        app: mcp-server
    spec:
      containers:
      - name: mcp-server
        image: my-mcp-server:latest
        ports:
        - containerPort: 8080
        env:
        - name: MCP_SERVER_CONFIG
          value: "/config/server.conf"
```

### 3. 监控和日志

```kotlin
// 结构化日志
private val logger = KotlinLogging.logger {}

// 性能监控
val startTime = System.currentTimeMillis()
val result = client.callTool("performance_tool", emptyMap())
val duration = System.currentTimeMillis() - startTime
logger.info { "Tool call took ${duration}ms" }

// 健康检查
suspend fun healthCheck(): Boolean {
    return try {
        client.ping()
        true
    } catch (e: Exception) {
        logger.error(e) { "Health check failed" }
        false
    }
}
```

## 最佳实践

### 1. Gradle 配置最佳实践

#### 依赖管理
```kotlin
// 使用 BOM 管理版本
implementation(platform("io.ktor:ktor-bom:3.3.0"))
implementation("io.ktor:ktor-client-cio") // 不需要版本号
implementation("io.ktor:ktor-server-netty")

// 使用版本目录管理大型项目
// gradle/libs.versions.toml
[versions]
kotlin = "2.2.10"
mcp-kotlin = "0.7.4-SNAPSHOT"

[libraries]
mcp-kotlin-sdk = { group = "io.modelcontextprotocol", name = "kotlin-sdk", version.ref = "mcp-kotlin" }
```

#### 构建优化
```kotlin
kotlin {
    jvmToolchain(17) // 使用固定 JVM 版本
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict") // 启用严格模式
    }
}

// 启用增量编译
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf("-Xjsr305=strict")
    }
}
```

#### 代码质量
```kotlin
// 添加代码检查
plugins {
    id("org.jlleitschuh.gradle.ktlint") version "13.1.0"
    id("org.jetbrains.kotlinx.kover") version "0.9.3"
}

ktlint {
    filter {
        exclude("**/generated*/**")
    }
}

kover {
    reports {
        total {
            verify {
                rule {
                    minBound(80) // 80% 测试覆盖率
                }
            }
        }
    }
}
```

### 2. 错误处理
- 使用适当的异常类型
- 提供有意义的错误消息
- 实现重试机制

### 3. 性能优化
- 使用连接池
- 实现缓存机制
- 避免阻塞操作

### 4. 安全性
- 验证输入参数
- 实施访问控制
- 使用安全传输

### 5. 测试
- 单元测试
- 集成测试
- 端到端测试

## 故障排除

### 常见问题

1. **Gradle 构建问题**
   - **依赖冲突**: 使用 `./gradlew dependencies` 检查依赖树
   - **版本不兼容**: 确保 Kotlin 和 Ktor 版本匹配
   - **构建失败**: 检查 JVM 工具链版本 (推荐 17+)
   ```bash
   # 检查依赖冲突
   ./gradlew dependencies --configuration runtimeClasspath
   
   # 清理并重新构建
   ./gradlew clean build
   ```

2. **连接失败**
   - 检查传输配置
   - 验证服务器状态
   - 查看日志输出

3. **工具调用失败**
   - 验证工具参数
   - 检查服务器能力
   - 查看错误消息

4. **资源访问失败**
   - 检查资源 URI
   - 验证访问权限
   - 查看服务器日志

### 调试技巧

#### Gradle 调试
```bash
# 启用详细构建日志
./gradlew build --info

# 检查依赖解析
./gradlew dependencies --configuration runtimeClasspath

# 运行特定测试
./gradlew test --tests "com.yourpackage.YourTest"

# 生成依赖报告
./gradlew dependencyInsight --dependency kotlin-stdlib
```

#### 应用调试
```kotlin
// 启用详细日志
client.setLoggingLevel(LoggingLevel.debug)

// 监控连接状态
client.onConnect { println("Connected") }
client.onClose { println("Disconnected") }
client.onError { error -> println("Error: $error") }

// 启用 Ktor 日志
HttpClient {
    install(Logging) {
        level = LogLevel.ALL
    }
}
```

这个完整指南涵盖了 MCP Kotlin SDK 的所有主要功能和用法。通过这个 SDK，您可以构建功能强大的 MCP 应用程序，支持多种传输方式和丰富的功能。
