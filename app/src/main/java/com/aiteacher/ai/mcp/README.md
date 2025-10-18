# MCP架构说明 - 简化实现

## 🏗️ **简化的MCP架构（按照协议约定）**

### 1. **MCP协议的核心约定**

- **一个MCP Client只能维持与一个Server的1:1会话**
- **Host侧可以new多个Client来连接多个Server**
- **Host负责聚合多个Client的工具列表**
- **Host根据工具名称决定调用哪个Client**
- **配置文件驱动：初始化时一次性读取配置创建Client**

### 2. **架构组件**

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   BaseAgent     │    │ MCPClientManager│    │   config/mcp.json│
│   (MCP Host进程)│◄──►│   (Client集合)  │◄──►│   (配置文件)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   LLMModel      │    │   MCP Client    │    │   MCPServer     │
│   (工具注册)    │    │   (1:1连接)     │    │   (server/目录) │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### 3. **核心概念**

#### **MCP Host (BaseAgent)**
- **定义**: 使用LLM的进程，决定何时调用工具
- **职责**: 
  - 接收用户输入
  - 调用LLM模型
  - 根据LLM输出决定是否调用工具
  - 管理对话上下文
  - **维护自己的Client集合**

#### **MCP Client (MCPClientManager中的MCPClient)**
- **定义**: 封装的`MCPClient`类，负责与MCP Server通信
- **约定**: **一个Client只能连接一个Server**
- **职责**:
  - 连接到特定的MCP Server
  - 执行工具调用
  - 管理连接状态

#### **MCP Server (server/目录下的实现)**
- **定义**: 在`server/`目录下直接实现的Server
- **职责**:
  - 实现具体工具逻辑
  - 响应工具调用请求
  - 管理工具状态

### 4. **实现逻辑**

#### **Agent实例化**
```kotlin
class SecretaryAgent(
    model: LLMModel = LLMModel("qwen-max")
) : BaseAgent(
    name = "SecretaryAgent",
    description = "教秘代理，负责制定教学计划",
    model = model,
    availableTools = listOf("knowledge_base") // 只允许使用知识库工具
)
```

#### **MCPClientManager初始化**
```kotlin
class MCPClientManager(
    private val hostName: String,
    private val configFilePath: String
) {
    private val clients = mutableMapOf<String, MCPClient>() // serverId -> MCPClient
    private val toolToServerMap = mutableMapOf<String, String>() // toolName -> serverId
    private val allAvailableTools = mutableSetOf<String>() // 所有可用工具
    
    init {
        initializeClients() // 读取配置文件，为每个服务器创建对应的Client
    }
}
```

#### **MCPClient实现（简化版）**
```kotlin
class MCPClient(
    private val serverId: String,
    private val serverDef: ServerDef
) {
    // 直接使用ServerDef创建transport
    private val transport = when {
        serverDef.cmd != null -> {
            StdioClientTransport(ProcessBuilder(serverDef.cmd))
        }
        serverDef.url != null -> {
            SseClientTransport(serverDef.url)
        }
        else -> throw IllegalArgumentException("Server definition must have either cmd or url")
    }
    
    // 把底层传输层包装成官方 DefaultMcpClient，自动完成 Initialize/心跳/路由
    private val inner = DefaultMcpClient(transport)
    
    suspend fun connect() {
        // 触发 MCP 握手：发送 InitializeRequest → 等待 InitializeResponse → 发送 InitializedNotification
        inner.connect()
    }
    
    suspend fun listTools(): List<String> {
        // 发送 tools/list 请求，返回 Server 暴露的全部工具元信息
        val tools = inner.listTools()
        return tools.map { it.name }
    }
    
    suspend fun callTool(toolName: String, parameters: Map<String, Any>): String {
        // 发送 tools/call 请求，带工具名和参数；返回 ToolResult（含 content 数组）
        val result = inner.callTool(toolName, jsonParams)
        return result.content.first().text
    }
}
```

#### **Client创建逻辑**
```kotlin
private fun initializeClients() {
    runBlocking {
        // 读取配置文件
        val config = loadConfig()
        
        // 为配置文件中的每个服务器创建Client
        config.servers.forEach { serverDef ->
            try {
                val client = MCPClient(serverDef.id, serverDef)
                client.connect()
                clients[serverDef.id] = client
                
                // 从实际连接的服务器获取工具列表
                val serverTools = client.listTools()
                serverTools.forEach { toolName ->
                    toolToServerMap[toolName] = serverDef.id
                    allAvailableTools.add(toolName)
                }
                
                println("Connected to server '${serverDef.id}' with tools: $serverTools")
            } catch (e: Exception) {
                println("Failed to connect to server '${serverDef.id}': ${e.message}")
            }
        }
    }
}
```

### 5. **工具调用流程**

```
1. Agent实例化
   ├── 传入配置文件路径
   ├── 创建MCPClientManager(hostName, configFilePath)
   ├── MCPClientManager读取配置文件
   ├── 为每个服务器创建对应的MCPClient (1:1关系)
   ├── 从实际连接的服务器获取工具列表
   └── 将工具规范注册到LLMModel

2. 用户输入处理
   ├── Agent.runOnce(userInput)
   ├── 构建prompt (包含工具信息)
   ├── LLMModel.generateText(prompt)
   └── 解析工具调用

3. 工具执行
   ├── Agent.callTool(toolName, parameters)
   ├── MCPClientManager.callTool(toolName, parameters)
   ├── 根据toolToServerMap找到对应的MCPClient
   ├── MCPClient.callTool(toolName, parameters)
   └── MCPServer执行工具逻辑

4. 结果返回
   ├── MCPServer返回结果
   ├── MCPClient返回结果
   ├── MCPClientManager返回结果
   ├── Agent处理结果
   └── 返回最终响应
```

### 6. **配置文件示例**

文件：`app/src/main/java/com/aiteacher/ai/mcp/server/mcp.json`
```json
{
  "servers": [
    {
      "id": "knowledge_server",
      "cmd": ["java", "-jar", "knowledge-server.jar"]
    },
    {
      "id": "analysis_server", 
      "cmd": ["java", "-jar", "analysis-server.jar"]
    },
    {
      "id": "weather_server",
      "url": "https://weather.mcp.run"
    }
  ]
}
```

### 7. **多Agent示例**

```kotlin
// SecretaryAgent - 使用专门的配置文件
val secretaryAgent = SecretaryAgent(
    configFilePath = "app/src/main/java/com/aiteacher/ai/mcp/server/secretary-config.json"
)

// TeachingAgent - 使用包含更多工具的配置文件
val teachingAgent = TeachingAgent(
    configFilePath = "app/src/main/java/com/aiteacher/ai/mcp/server/teaching-config.json"
)

// 每个Agent都有自己的Client集合，互不干扰
```

## 🎯 **关键优势**

1. **协议合规**: 严格按照MCP协议约定实现
2. **资源隔离**: 每个Agent维护自己的Client集合
3. **灵活配置**: Agent可以指定需要的工具
4. **服务发现**: 通过工具名称自动找到对应的服务器
5. **连接管理**: 一个Client对应一个Server，清晰明确
6. **生命周期**: Agent关闭时自动清理所有连接
7. **配置驱动**: 通过配置文件管理服务器连接
8. **传输层无关**: 同一段代码支持stdio、SSE、WebSocket多种传输方式
9. **简化架构**: 移除冗余组件，直接读取配置文件

## 🔧 **使用方式**

### 1. 创建Agent实例
```kotlin
// 使用默认配置文件
val secretaryAgent = SecretaryAgent()

// 使用自定义配置文件
val customAgent = SecretaryAgent(
    configFilePath = "path/to/custom-config.json"
)
```

### 2. 配置服务器
在配置文件中定义服务器：
```json
{
  "servers": [
    {
      "id": "knowledge_server",
      "cmd": ["java", "-jar", "knowledge-server.jar"]
    },
    {
      "id": "analysis_server",
      "cmd": ["python", "analysis_server.py"]
    }
  ]
}
```

### 3. 查看连接状态
```kotlin
val status = secretaryAgent.mcpClientManager.getConnectionStatus()
val details = secretaryAgent.mcpClientManager.getClientDetails()
println("连接状态: $status")
println("连接详情: $details")
```

## 📝 **注意事项**

1. **配置文件格式**: 必须严格按照JSON格式，支持cmd（stdio）和url（SSE/WebSocket）两种连接方式
2. **服务器ID唯一性**: 每个服务器的id必须唯一
3. **动态工具发现**: 系统会自动从实际连接的服务器获取工具列表
4. **错误处理**: 如果服务器连接失败，会记录错误日志但不影响其他服务器
5. **资源清理**: Agent关闭时会自动清理所有Client连接
6. **配置灵活性**: 每个Agent可以使用不同的配置文件，实现工具隔离
