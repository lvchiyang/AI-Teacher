# MCP架构说明

## 🏗️ **正确的MCP架构（按照协议约定）**

### 1. **MCP协议的核心约定**

- **一个MCP Client只能维持与一个Server的1:1会话**
- **Host侧可以new多个Client来连接多个Server**
- **Host负责聚合多个Client的工具列表**
- **Host根据工具名称决定调用哪个Client**

### 2. **架构组件**

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   BaseAgent     │    │    MCPHost      │    │ MCPServerRegistry│
│   (MCP Host)    │◄──►│   (Client集合)  │◄──►│   (单例)        │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   LLMModel      │    │   MCP Client    │    │   MCPServer     │
│   (工具注册)    │    │   (1:1连接)     │    │   (工具实现)    │
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

#### **MCP Client (MCPHost中的Client)**
- **定义**: SDK提供的`Client`类，负责与MCP Server通信
- **约定**: **一个Client只能连接一个Server**
- **职责**:
  - 连接到特定的MCP Server
  - 执行工具调用
  - 管理连接状态

#### **MCP Server (MCPServer)**
- **定义**: SDK提供的`Server`类，暴露工具服务
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

#### **MCPHost初始化**
```kotlin
class MCPHost(
    private val hostName: String,
    private val requiredTools: List<String>
) {
    private val clients = mutableMapOf<String, Client>() // serverId -> Client
    private val toolToServerMap = mutableMapOf<String, String>() // toolName -> serverId
    
    init {
        initializeClients() // 为每个需要的工具创建对应的Client
    }
}
```

#### **Client创建逻辑**
```kotlin
private fun initializeClients() {
    requiredTools.forEach { toolName ->
        val servers = MCPServerRegistry.findServersByTool(toolName)
        if (servers.isNotEmpty()) {
            val serverInfo = servers.first()
            val serverId = serverInfo.serverId
            
            // 如果还没有为这个服务器创建Client，则创建
            if (!clients.containsKey(serverId)) {
                val client = createClient(serverInfo) // 一个Client对应一个Server
                clients[serverId] = client
            }
            
            // 建立工具到服务器的映射
            toolToServerMap[toolName] = serverId
        }
    }
}
```

### 5. **工具调用流程**

```
1. Agent实例化
   ├── 定义availableTools: ["knowledge_base"]
   ├── 创建MCPHost(hostName, availableTools)
   ├── MCPHost为每个工具找到对应的Server
   ├── 为每个Server创建对应的Client (1:1关系)
   └── 将工具规范注册到LLMModel

2. 用户输入处理
   ├── Agent.runOnce(userInput)
   ├── 构建prompt (包含工具信息)
   ├── LLMModel.generateText(prompt)
   └── 解析工具调用

3. 工具执行
   ├── Agent.callTool(toolName, parameters)
   ├── MCPHost.callTool(toolName, parameters)
   ├── 根据toolToServerMap找到对应的Client
   ├── Client.callTool(toolName, parameters)
   └── MCPServer执行工具逻辑

4. 结果返回
   ├── MCPServer返回结果
   ├── Client返回结果
   ├── MCPHost返回结果
   ├── Agent处理结果
   └── 返回最终响应
```

### 6. **服务器配置示例**

```kotlin
// 在MCPServerRegistry中注册服务器
MCPServerRegistry.registerServer("knowledge_server", MCPServerInfo(
    serverId = "knowledge_server",
    name = "知识库服务器",
    availableTools = listOf("knowledge_base"),
    transportType = "stdio",
    connectionInfo = mapOf("command" to "java -jar knowledge-server.jar")
))

MCPServerRegistry.registerServer("analysis_server", MCPServerInfo(
    serverId = "analysis_server",
    name = "学习分析服务器", 
    availableTools = listOf("analyze_progress", "generate_questions"),
    transportType = "stdio",
    connectionInfo = mapOf("command" to "java -jar analysis-server.jar")
))
```

### 7. **多Agent示例**

```kotlin
// SecretaryAgent - 只能使用知识库工具
val secretaryAgent = SecretaryAgent() // 内部创建MCPHost，连接knowledge_server

// TeachingAgent - 可以使用多个工具
val teachingAgent = TeachingAgent() // 内部创建MCPHost，连接knowledge_server和analysis_server

// 每个Agent都有自己的Client集合，互不干扰
```

## 🎯 **关键优势**

1. **协议合规**: 严格按照MCP协议约定实现
2. **资源隔离**: 每个Agent维护自己的Client集合
3. **灵活配置**: Agent可以指定需要的工具
4. **服务发现**: 通过工具名称自动找到对应的服务器
5. **连接管理**: 一个Client对应一个Server，清晰明确
6. **生命周期**: Agent关闭时自动清理所有连接
