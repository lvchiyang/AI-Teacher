# MCP Kotlin SDK - 服务器文档

## 概述

MCP Kotlin SDK 服务器模块提供了构建 Model Context Protocol (MCP) 服务器的完整功能。它支持多种传输方式，包括标准输入输出 (Stdio)、Server-Sent Events (SSE) 和 WebSocket。

## 核心组件

### 必要的导入语句

```kotlin
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import io.modelcontextprotocol.kotlin.sdk.server.SseServerTransport
import io.modelcontextprotocol.kotlin.sdk.server.WebSocketMcpServerTransport
import io.modelcontextprotocol.kotlin.sdk.shared.Implementation
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.Prompt
import io.modelcontextprotocol.kotlin.sdk.PromptArgument
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.GetPromptResult
import io.modelcontextprotocol.kotlin.sdk.ReadResourceResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.TextResourceContents
import io.modelcontextprotocol.kotlin.sdk.PromptMessage
import io.modelcontextprotocol.kotlin.sdk.Role
import io.modelcontextprotocol.kotlin.sdk.server.RegisteredTool
import io.modelcontextprotocol.kotlin.sdk.server.RegisteredPrompt
import io.modelcontextprotocol.kotlin.sdk.server.RegisteredResource
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import kotlinx.io.Source
import kotlinx.io.Sink
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.Job
import io.ktor.server.engine.embeddedServer
import io.ktor.server.cio.CIO
import io.ktor.server.plugins.websocket.WebSockets
import io.ktor.server.routing.routing
import io.ktor.server.routing.webSocket
import io.ktor.server.sse.SSE
import io.ktor.server.routing.Routing
import io.ktor.server.application.Application
import io.ktor.server.routing.route
import io.ktor.server.routing.sse
import io.ktor.server.routing.post
```

### 1. Server 类

`Server` 类是 MCP 服务器的核心实现，提供了完整的服务器功能。

```kotlin
public open class Server(
    public val serverInfo: Implementation,
    public val options: ServerOptions,
    public val instructionsProvider: (() -> String)? = null,
)
```

#### 主要功能
- **工具管理**: 注册、执行和管理工具
- **提示管理**: 提供可重用的提示模板
- **资源管理**: 管理可访问的资源
- **会话管理**: 处理多个并发连接

### 2. ServerOptions 配置

```kotlin
public class ServerOptions(
    public val capabilities: ServerCapabilities, 
    public val enforceStrictCapabilities: Boolean = true
)
```

#### 服务器能力配置

```kotlin
ServerCapabilities(
    tools = ServerCapabilities.Tools(listChanged = true),
    prompts = ServerCapabilities.Prompts(listChanged = true),
    resources = ServerCapabilities.Resources(
        subscribe = true, 
        listChanged = true
    ),
    experimental = null,
    logging = null,
    sampling = null
)
```

## 服务器实现

### 基本服务器创建

```kotlin
val server = Server(
    serverInfo = Implementation(
        name = "my-mcp-server",
        version = "1.0.0"
    ),
    options = ServerOptions(
        capabilities = ServerCapabilities(
            tools = ServerCapabilities.Tools(listChanged = true),
            prompts = ServerCapabilities.Prompts(listChanged = true),
            resources = ServerCapabilities.Resources(
                subscribe = true,
                listChanged = true
            )
        )
    )
)
```

### 工具注册

#### 单个工具注册

```kotlin
server.addTool(
    name = "weather",
    description = "Get weather information for a location",
    inputSchema = Tool.Input(
        properties = buildJsonObject {
            putJsonObject("location") {
                put("type", "string")
                put("description", "City name or coordinates")
            }
        },
        required = listOf("location")
    ),
    title = null,
    outputSchema = null,
    toolAnnotations = null
) { request ->
    CallToolResult(
        content = listOf(TextContent("Weather data for ${request.arguments["location"]}"))
    )
}
```

#### 批量工具注册

```kotlin
val toolsToAdd = listOf(
    RegisteredTool(
        tool = Tool(
            name = "calculator",
            description = "Perform mathematical calculations"
        ),
        handler = { request -> /* 处理逻辑 */ }
    )
)
server.addTools(toolsToAdd)
```

### 提示注册

#### 单个提示注册

```kotlin
server.addPrompt(
    name = "code_review",
    description = "Generate code review prompts",
    arguments = listOf(
        PromptArgument(
            name = "language",
            description = "Programming language",
            required = true
        )
    )
) { request ->
    GetPromptResult(
        description = "Code review prompt for ${request.arguments?.get("language")}",
        messages = listOf(
            PromptMessage(
                role = Role.user,
                content = TextContent("Please review this ${request.arguments?.get("language")} code...")
            )
        )
    )
}
```

### 资源注册

```kotlin
server.addResource(
    uri = "file:///documents/readme.md",
    name = "README",
    description = "Project documentation",
    mimeType = "text/markdown"
) { request ->
    ReadResourceResult(
        contents = listOf(
            TextResourceContents(
                text = "# Project Documentation\n\n...",
                uri = request.uri,
                mimeType = "text/markdown"
            )
        )
    )
}
```

## 传输方式

### 1. Stdio 传输

适用于命令行工具和进程间通信。

```kotlin
val transport = StdioServerTransport(
    inputStream = System.`in` as Source,
    outputStream = System.out as Sink
)

runBlocking {
    val session = server.connect(transport)
    val done = Job()
    session.onClose {
        done.complete()
    }
    done.join()
}
```

### 2. SSE 传输 (Server-Sent Events)

适用于 Web 应用程序。

#### 使用 Ktor 插件

```kotlin
fun Application.module() {
    install(SSE)
    routing {
        mcp("/mcp") {
            Server(
                serverInfo = Implementation(
                    name = "sse-server",
                    version = "1.0.0"
                ),
                options = ServerOptions(
                    capabilities = ServerCapabilities(
                        tools = ServerCapabilities.Tools(listChanged = true)
                    )
                )
            ) {
                "This server provides tools via SSE"
            }
        }
    }
}
```

#### 手动配置 SSE

```kotlin
embeddedServer(CIO, host = "127.0.0.1", port = 8080) {
    install(SSE)
    routing {
        sse("/sse") {
            val transport = SseServerTransport("/message", this)
            val serverSession = server.connect(transport)
            
            serverSession.onClose {
                println("Server closed")
            }
        }
        
        post("/message") {
            val sessionId = call.request.queryParameters["sessionId"]
            // 处理消息
        }
    }
}.startSuspend(wait = true)
```

### 3. WebSocket 传输

```kotlin
embeddedServer(CIO, host = "127.0.0.1", port = 8080) {
    install(WebSockets)
    routing {
        webSocket("/mcp") {
            val transport = WebSocketMcpServerTransport(this)
            val serverSession = server.connect(transport)
            
            serverSession.onClose {
                println("WebSocket connection closed")
            }
        }
    }
}.startSuspend(wait = true)
```

## 生命周期管理

### 连接回调

```kotlin
server.onConnect {
    println("New client connected")
}

server.onClose {
    println("Client disconnected")
}
```

### 会话管理

```kotlin
val session = server.connect(transport)

session.onInitialized {
    println("Session initialized")
}

session.onClose {
    println("Session closed")
}
```

## 错误处理

### 工具调用错误

```kotlin
server.addTool("risky_tool", "A tool that might fail") { request ->
    try {
        // 执行可能失败的操作
        CallToolResult(content = listOf(TextContent("Success")))
    } catch (e: Exception) {
        CallToolResult(
            content = listOf(TextContent("Error: ${e.message}")),
            isError = true
        )
    }
}
```

### 资源访问错误

```kotlin
server.addResource("file:///secure/data.txt", "Secure Data", "Sensitive information") { request ->
    try {
        // 检查访问权限
        if (!hasPermission(request.uri)) {
            throw SecurityException("Access denied")
        }
        ReadResourceResult(contents = listOf(/* 资源内容 */))
    } catch (e: SecurityException) {
        throw IllegalArgumentException("Access denied to resource: ${request.uri}")
    }
}
```

## 完整示例

### 天气服务器示例

```kotlin
fun createWeatherServer(): Server {
    val server = Server(
        serverInfo = Implementation(
            name = "weather-server",
            version = "1.0.0"
        ),
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

    // 添加天气工具
    server.addTool(
        name = "get_weather",
        description = "Get current weather for a location",
        inputSchema = Tool.Input(
            properties = buildJsonObject {
                putJsonObject("location") {
                    put("type", "string")
                    put("description", "City name or coordinates")
                }
            },
            required = listOf("location")
        )
    ) { request ->
        val location = request.arguments["location"]?.jsonPrimitive?.content
        val weather = getWeatherData(location ?: "Unknown")
        
        CallToolResult(
            content = listOf(TextContent("Weather in $location: $weather"))
        )
    }

    // 添加天气资源
    server.addResource(
        uri = "weather://forecast",
        name = "Weather Forecast",
        description = "7-day weather forecast",
        mimeType = "application/json"
    ) { request ->
        ReadResourceResult(
            contents = listOf(
                TextResourceContents(
                    text = getForecastData(),
                    uri = request.uri,
                    mimeType = "application/json"
                )
            )
        )
    }

    return server
}

// 启动服务器
fun main() = runBlocking {
    val server = createWeatherServer()
    val transport = StdioServerTransport(
        System.`in` as Source,
        System.out as Sink
    )
    
    val session = server.connect(transport)
    val done = Job()
    session.onClose { done.complete() }
    done.join()
}
```

## 最佳实践

### 1. 错误处理
- 始终在工具和资源处理器中包含适当的错误处理
- 使用 `isError = true` 标记错误结果
- 提供有意义的错误消息

### 2. 性能优化
- 使用异步操作处理长时间运行的任务
- 实现适当的缓存机制
- 避免阻塞操作

### 3. 安全性
- 验证输入参数
- 实施适当的访问控制
- 记录安全相关事件

### 4. 监控和日志
- 使用结构化日志记录
- 监控服务器性能指标
- 实现健康检查端点

## 部署选项

### 1. 独立应用程序
```kotlin
// 作为独立进程运行
fun main() = runBlocking {
    val server = createServer()
    val transport = StdioServerTransport(
        System.`in` as Source,
        System.out as Sink
    )
    server.connect(transport)
}
```

### 2. 嵌入式服务器
```kotlin
// 嵌入到现有应用程序中
class MyApplication {
    private val mcpServer = createServer()
    
    fun startMcpServer() {
        // 启动 MCP 服务器
    }
}
```

### 3. 微服务
```kotlin
// 作为微服务部署
fun Application.module() {
    install(SSE)
    routing {
        mcp("/api/mcp") {
            createServer()
        }
    }
}
```

这个文档涵盖了 MCP Kotlin SDK 服务器的主要功能和用法。通过这个 SDK，您可以轻松构建功能强大的 MCP 服务器，支持多种传输方式和丰富的功能。
