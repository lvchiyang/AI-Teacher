# MCP Kotlin SDK - 客户端文档

## 概述

MCP Kotlin SDK 客户端模块提供了连接到 MCP 服务器并与之交互的完整功能。它支持多种传输方式，包括标准输入输出 (Stdio)、Server-Sent Events (SSE)、WebSocket 和 HTTP 流式传输。

## 核心组件

### 必要的导入语句

```kotlin
import io.modelcontextprotocol.kotlin.sdk.client.Client
import io.modelcontextprotocol.kotlin.sdk.client.StdioClientTransport
import io.modelcontextprotocol.kotlin.sdk.client.SseClientTransport
import io.modelcontextprotocol.kotlin.sdk.client.WebSocketClientTransport
import io.modelcontextprotocol.kotlin.sdk.client.StreamableHttpClientTransport
import io.modelcontextprotocol.kotlin.sdk.shared.Implementation
import io.modelcontextprotocol.kotlin.sdk.shared.ClientOptions
import io.modelcontextprotocol.kotlin.sdk.shared.ClientCapabilities
import io.modelcontextprotocol.kotlin.sdk.shared.RequestOptions
import io.modelcontextprotocol.kotlin.sdk.CallToolRequest
import io.modelcontextprotocol.kotlin.sdk.ReadResourceRequest
import io.modelcontextprotocol.kotlin.sdk.SubscribeRequest
import io.modelcontextprotocol.kotlin.sdk.UnsubscribeRequest
import io.modelcontextprotocol.kotlin.sdk.GetPromptRequest
import io.modelcontextprotocol.kotlin.sdk.CompleteRequest
import io.modelcontextprotocol.kotlin.sdk.Root
import io.modelcontextprotocol.kotlin.sdk.LoggingLevel
import io.modelcontextprotocol.kotlin.sdk.ListPromptsRequest
import io.modelcontextprotocol.kotlin.sdk.ListResourcesRequest
import io.modelcontextprotocol.kotlin.sdk.ListToolsRequest
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.ImageContent
import io.modelcontextprotocol.kotlin.sdk.AudioContent
import io.modelcontextprotocol.kotlin.sdk.TextResourceContents
import io.modelcontextprotocol.kotlin.sdk.BlobResourceContents
import io.modelcontextprotocol.kotlin.sdk.PromptReference
import io.modelcontextprotocol.kotlin.sdk.Method
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.coroutines.runBlocking
```

### 1. Client 类

`Client` 类是 MCP 客户端的核心实现，提供了完整的客户端功能。

```kotlin
public open class Client(
    private val clientInfo: Implementation, 
    options: ClientOptions = ClientOptions()
) : Protocol(options)
```

#### 主要功能
- **自动初始化**: 自动执行与服务器的握手协议
- **工具调用**: 调用服务器提供的工具
- **资源访问**: 读取和管理服务器资源
- **提示获取**: 获取服务器提供的提示模板
- **根管理**: 管理客户端根目录

### 2. ClientOptions 配置

```kotlin
public class ClientOptions(
    public val capabilities: ClientCapabilities = ClientCapabilities(),
    enforceStrictCapabilities: Boolean = true,
) : ProtocolOptions(enforceStrictCapabilities = enforceStrictCapabilities)
```

#### 客户端能力配置

```kotlin
ClientCapabilities(
    roots = ClientCapabilities.Roots(listChanged = true),
    sampling = JsonObject(emptyMap()),
    elicitation = JsonObject(emptyMap())
)
```

## 客户端实现

### 基本客户端创建

```kotlin
val client = Client(
    clientInfo = Implementation(
        name = "my-mcp-client",
        version = "1.0.0"
    ),
    options = ClientOptions(
        capabilities = ClientCapabilities(
            roots = ClientCapabilities.Roots(listChanged = true)
        )
    )
)
```

### 连接管理

```kotlin
// 连接到服务器
client.connect(transport)

// 检查连接状态
val serverCapabilities = client.serverCapabilities
val serverVersion = client.serverVersion
val serverInstructions = client.serverInstructions
```

## 传输方式

### 1. Stdio 传输

适用于命令行工具和进程间通信。

```kotlin
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered

val transport = StdioClientTransport(
    input = processInputStream.asSource().buffered(),
    output = processOutputStream.asSink().buffered()
)

client.connect(transport)
```

### 2. WebSocket 传输

适用于实时双向通信。

```kotlin
val httpClient = HttpClient {
    install(WebSockets)
}

val transport = WebSocketClientTransport(
    client = httpClient,
    urlString = "ws://localhost:8080/mcp"
)

client.connect(transport)
```

### 3. SSE 传输

适用于单向服务器推送。

```kotlin
val transport = SseClientTransport(
    client = httpClient,
    urlString = "http://localhost:8080/sse"
)

client.connect(transport)
```

### 4. HTTP 流式传输

```kotlin
val transport = StreamableHttpClientTransport(
    client = httpClient,
    url = "http://localhost:8080/stream"
)

client.connect(transport)
```

## 功能使用

### 工具调用

#### 基本工具调用

```kotlin
// 调用工具并传递参数
val result = client.callTool(
    name = "weather",
    arguments = mapOf(
        "location" to "New York",
        "units" to "metric"
    ),
    compatibility = false,
    options = null
)

// 处理结果
result?.content?.forEach { content ->
    when (content) {
        is TextContent -> println("Result: ${content.text}")
        is ImageContent -> println("Image: ${content.data}")
        is AudioContent -> println("Audio: ${content.data}")
    }
}
```

#### 兼容模式工具调用

```kotlin
val result = client.callTool(
    request = CallToolRequest(
        name = "legacy_tool",
        arguments = JsonObject(mapOf(
            "param1" to JsonPrimitive("value1")
        ))
    ),
    compatibility = true // 使用兼容模式
)
```

### 资源管理

#### 列出资源

```kotlin
val resources = client.listResources(
    request = ListResourcesRequest(),
    options = null
)
resources.resources.forEach { resource ->
    println("Resource: ${resource.name} (${resource.uri})")
}
```

#### 读取资源

```kotlin
val resourceContent = client.readResource(
    ReadResourceRequest(uri = "file:///documents/readme.md"),
    options = null
)

resourceContent.contents.forEach { content ->
    when (content) {
        is TextResourceContents -> println("Text: ${content.text}")
        is BlobResourceContents -> println("Binary data: ${content.blob}")
    }
}
```

#### 资源订阅

```kotlin
// 订阅资源更新
client.subscribeResource(
    SubscribeRequest(uri = "file:///documents/readme.md"),
    options = null
)

// 取消订阅
client.unsubscribeResource(
    UnsubscribeRequest(uri = "file:///documents/readme.md"),
    options = null
)
```

### 提示管理

#### 列出提示

```kotlin
val prompts = client.listPrompts(
    request = ListPromptsRequest(),
    options = null
)
prompts.prompts.forEach { prompt ->
    println("Prompt: ${prompt.name} - ${prompt.description}")
}
```

#### 获取提示

```kotlin
val prompt = client.getPrompt(
    GetPromptRequest(
        name = "code_review",
        arguments = mapOf("language" to "Kotlin")
    ),
    options = null
)

prompt.messages.forEach { message ->
    println("${message.role}: ${message.content}")
}
```

### 根管理

#### 添加根目录

```kotlin
// 添加单个根
client.addRoot(
    uri = "file:///home/user/projects",
    name = "My Projects"
)

// 批量添加根
val roots = listOf(
    Root("file:///home/user/docs", "Documents"),
    Root("file:///home/user/code", "Code")
)
client.addRoots(roots)
```

#### 管理根目录

```kotlin
// 移除根目录
val removed = client.removeRoot("file:///home/user/projects")

// 批量移除
val removedCount = client.removeRoots(listOf(
    "file:///home/user/docs",
    "file:///home/user/code"
))

// 通知服务器根目录变化
client.sendRootsListChanged()
```

### 高级功能

#### 完成请求

```kotlin
val completion = client.complete(
    CompleteRequest(
        ref = PromptReference("code_review"),
        argument = CompleteRequest.Argument(
            name = "language",
            value = "Kotlin"
        )
    ),
    options = null
)

completion.completion.values.forEach { value ->
    println("Completion: $value")
}
```

#### 日志级别设置

```kotlin
client.setLoggingLevel(LoggingLevel.info, options = null)
```

#### 心跳检测

```kotlin
val pingResult = client.ping(options = null)
println("Server is alive: ${pingResult != null}")
```

## 事件处理

### 设置事件处理器

```kotlin
// 设置请求处理器
client.setRequestHandler<CustomRequest>(Method.Custom("custom_method")) { request, _ ->
    // 处理自定义请求
    CustomResult()
}

// 设置通知处理器
client.setNotificationHandler<CustomNotification>(Method.Custom("custom_notification")) { notification ->
    // 处理自定义通知
    println("Received notification: ${notification.method}")
}

// 设置启发处理器（用于处理服务器请求的启发信息）
client.setElicitationHandler { elicitation ->
    println("Received elicitation: ${elicitation.message}")
    // 返回启发响应
    ElicitationResponse(/* ... */)
}
```

### 会话生命周期

```kotlin
// 注意：这些方法在 Protocol 基类中，需要通过传输层设置
// 或者通过自定义传输实现来处理连接事件
```

## 完整示例

### 天气客户端示例

```kotlin
class WeatherClient : AutoCloseable {
    private val client = Client(
        clientInfo = Implementation(
            name = "weather-client",
            version = "1.0.0"
        )
    )

    suspend fun connectToWeatherServer(serverPath: String) {
        // 启动服务器进程
        val process = ProcessBuilder("node", serverPath).start()
        
        // 创建传输
        val transport = StdioClientTransport(
            input = process.inputStream.asSource().buffered(),
            output = process.outputStream.asSink().buffered()
        )
        
        // 连接到服务器
        client.connect(transport)
        
        // 获取可用工具
        val tools = client.listTools(
            request = ListToolsRequest(),
            options = null
        )
        println("Available tools: ${tools.tools.map { it.name }}")
    }

    suspend fun getWeather(location: String): String {
        val result = client.callTool(
            name = "get_weather",
            arguments = mapOf("location" to location),
            compatibility = false,
            options = null
        )
        
        return result?.content?.joinToString("\n") { content ->
            when (content) {
                is TextContent -> content.text ?: ""
                else -> content.toString()
            }
        } ?: "No weather data available"
    }

    suspend fun getForecast(): String {
        val resource = client.readResource(
            ReadResourceRequest(uri = "weather://forecast")
        )
        
        return resource.contents.joinToString("\n") { content ->
            when (content) {
                is TextResourceContents -> content.text
                else -> content.toString()
            }
        }
    }

    override fun close() {
        runBlocking {
            client.close()
        }
    }
}

// 使用示例
fun main() = runBlocking {
    val weatherClient = WeatherClient()
    
    try {
        weatherClient.connectToWeatherServer("weather-server.js")
        
        println("Weather in New York:")
        println(weatherClient.getWeather("New York"))
        
        println("\n7-day forecast:")
        println(weatherClient.getForecast())
        
    } finally {
        weatherClient.close()
    }
}
```

### 与 Anthropic API 集成示例

```kotlin
class MCPAnthropicClient : AutoCloseable {
    private val anthropic = AnthropicOkHttpClient.fromEnv()
    private val mcpClient = Client(
        clientInfo = Implementation(
            name = "mcp-anthropic-client",
            version = "1.0.0"
        )
    )
    
    private lateinit var tools: List<ToolUnion>

    suspend fun connectToServer(serverScriptPath: String) {
        // 启动 MCP 服务器
        val process = ProcessBuilder("node", serverScriptPath).start()
        
        val transport = StdioClientTransport(
            input = process.inputStream.asSource().buffered(),
            output = process.outputStream.asSink().buffered()
        )
        
        mcpClient.connect(transport)
        
        // 获取工具列表
        val toolsResult = mcpClient.listTools(
            request = ListToolsRequest(),
            options = null
        )
        tools = toolsResult.tools.map { tool ->
            ToolUnion.ofTool(
                Tool.builder()
                    .name(tool.name)
                    .description(tool.description ?: "")
                    .inputSchema(/* 转换输入模式 */)
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
                    val result = mcpClient.callTool(
                        name = toolName,
                        arguments = toolArgs ?: emptyMap(),
                        compatibility = false,
                        options = null
                    )
                    
                    finalText.add("[Tool $toolName called]")
                    
                    // 添加工具结果到对话
                    messages.add(
                        MessageParam.builder()
                            .role(MessageParam.Role.USER)
                            .content("Tool result: ${result?.content?.joinToString()}")
                            .build()
                    )
                    
                    // 获取更新后的响应
                    val updatedResponse = anthropic.messages().create(
                        MessageCreateParams.builder()
                            .messages(messages)
                            .build()
                    )
                    
                    finalText.add(updatedResponse.content().first().text().getOrNull()?.text() ?: "")
                }
            }
        }
        
        return finalText.joinToString("\n")
    }

    override fun close() {
        runBlocking {
            mcpClient.close()
            anthropic.close()
        }
    }
}
```

## 最佳实践

### 1. 连接管理
- 始终在 try-finally 块中管理连接
- 实现适当的重连机制
- 监控连接状态

### 2. 错误处理
```kotlin
try {
    val result = client.callTool(
        name = "risky_tool", 
        arguments = emptyMap(),
        compatibility = false,
        options = null
    )
    // 处理结果
} catch (e: IllegalStateException) {
    println("Server does not support this tool: $e")
} catch (e: Exception) {
    println("Tool call failed: $e")
}
```

### 3. 资源清理
```kotlin
class ManagedMCPClient : AutoCloseable {
    private val client = Client(/* ... */)
    
    override fun close() {
        runBlocking {
            client.close()
        }
    }
}
```

### 4. 性能优化
- 使用连接池管理多个连接
- 实现适当的缓存机制
- 避免频繁的工具调用

### 5. 安全性
- 验证服务器响应
- 实施适当的超时机制
- 记录安全相关事件

## 调试和监控

### 日志配置

```kotlin
// 设置详细日志
client.setLoggingLevel(LoggingLevel.debug, options = null)
```

### 连接监控

```kotlin
// 注意：连接事件处理需要通过传输层实现
// 或者通过自定义传输类来处理连接状态变化
```

### 性能指标

```kotlin
// 监控工具调用性能
val startTime = System.currentTimeMillis()
val result = client.callTool(
    name = "performance_tool", 
    arguments = emptyMap(),
    compatibility = false,
    options = null
)
val duration = System.currentTimeMillis() - startTime
println("Tool call took ${duration}ms")
```

这个文档涵盖了 MCP Kotlin SDK 客户端的主要功能和用法。通过这个 SDK，您可以轻松构建功能强大的 MCP 客户端，支持多种传输方式和丰富的功能。
