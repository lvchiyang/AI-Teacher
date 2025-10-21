package com.aiteacher.ai.mcp

import io.modelcontextprotocol.kotlin.sdk.client.*
import io.modelcontextprotocol.kotlin.sdk.transport.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import java.nio.file.Files
import java.nio.file.Paths
import kotlinx.serialization.json.JsonPrimitive
import java.util.concurrent.ConcurrentHashMap

/**
 * MCP Client - 单个Client实例，连接一个Server
 * 按照MCP协议文档实现
 */

private fun Map<String, Any>.toJsonObject(): JsonObject = buildJsonObject {
    forEach { (k, v) ->
        put(k, when (v) {
            is Number -> JsonPrimitive(v)
            is Boolean -> JsonPrimitive(v)
            is String -> JsonPrimitive(v)
            else -> JsonPrimitive(v.toString())
        })
    }
}

class MCPClient(
    private val serverId: String,
    private val serverDef: ServerDef,
    private val coroutineScope: CoroutineScope
) {
    private val transport = when {
        serverDef.cmd != null -> {
            StdioTransport(serverDef.cmd.first(), serverDef.cmd.drop(1))
        }
        serverDef.url != null -> {
            HttpTransport(serverDef.url)
        }
        else -> throw IllegalArgumentException("Server definition must have either cmd or url")
    }
    
    // 使用新的McpClient API
    private val inner = McpClient(transport, coroutineScope)
    
    private var isConnected = false
    
    /**
     * 连接到MCP服务器
     */
    suspend fun connect() {
        if (!isConnected) {
            // 触发 MCP 握手：发送 InitializeRequest → 等待 InitializeResponse → 发送 InitializedNotification
            inner.start()
            isConnected = true
        }
    }
    
    /**
     * 调用工具
     */
    suspend fun callTool(toolName: String, parameters: Map<String, Any>): String {
        if (!isConnected) {
            throw IllegalStateException("Client not connected to server")
        }
        
        // 发送 tools/call 请求，带工具名和参数；返回 ToolResult（含 content 数组）
        val result = inner.callTool(toolName, parameters.toJsonObject())
        return result.content.firstOrNull()?.text ?: ""
    }
    
    /**
     * 获取可用工具列表
     */
    suspend fun listTools(): List<String> {
        if (!isConnected) {
            throw IllegalStateException("Client not connected to server")
        }
        
        // 发送 tools/list 请求，返回 Server 暴露的全部工具元信息
        val tools = inner.listTools()
        return tools.map { it.name }
    }
    
    /**
     * 获取工具规范（用于LLM）
     */
    suspend fun getToolSpecs(): List<Map<String, Any>> {
        if (!isConnected) {
            throw IllegalStateException("Client not connected to server")
        }
        
        val tools = inner.listTools()
        return tools.map { tool ->
            mapOf(
                "type" to "function",
                "function" to mapOf(
                    "name" to tool.name,
                    "description" to tool.description,
                    "parameters" to tool.inputSchema
                )
            )
        }
    }
    
    /**
     * 检查连接状态
     */
    fun isConnected(): Boolean = isConnected
    
    /**
     * 关闭连接
     */
    fun close() {
        if (isConnected) {
            // 关闭传输层：先发送 Close 帧，再释放文件描述符/HTTP 连接，最后取消内部协程作用域
            transport.close()
            isConnected = false
        }
    }
}

/**
 * MCP Client管理器 - 按照MCP协议实现
 * 每个Agent作为Host进程，维护自己的Client集合
 * 核心约定：一个Client只能连接一个Server
 * 支持根据配置文件自动创建Client Map
 */
class MCPClientManager(
    private val hostName: String,
    private val configFilePath: String,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) : CoroutineScope by coroutineScope {
    private val clients = ConcurrentHashMap<String, MCPClient>() // serverId -> MCPClient
    private val toolToServerMap = mutableMapOf<String, String>() // toolName -> serverId
    private val allAvailableTools = mutableSetOf<String>() // 所有可用工具
    
    /**
     * 启动MCP客户端管理器
     */
    suspend fun start() {
        initializeClients()
    }
    
    /**
     * 初始化所有Client连接 - 根据配置文件自动创建
     */
    private suspend fun initializeClients() {
        // 读取配置文件
        val config = loadConfig()
        
        // 为配置文件中的每个服务器创建Client
        config.servers.forEach { serverDef ->
            try {
                val client = MCPClient(serverDef.id, serverDef, coroutineScope)
                client.connect()
                clients[serverDef.id] = client
                
                // 从实际连接的服务器获取工具列表
                val serverTools = client.listTools()
                serverTools.forEach { toolName ->
                    // 直接使用工具名，因为每个Agent的配置文件中没有重名工具
                    toolToServerMap[toolName] = serverDef.id
                    allAvailableTools.add(toolName)
                }
                
                println("Connected to server '${serverDef.id}' with tools: $serverTools")
            } catch (e: Exception) {
                println("Failed to connect to server '${serverDef.id}': ${e.message}")
            }
        }
        
        println("MCPClientManager initialized with ${clients.size} servers and ${allAvailableTools.size} tools")
    }
    
    /**
     * 加载配置文件
     */
    private fun loadConfig(): ConfigSnapshot {
        val configFile = when {
            configFilePath.startsWith("classpath:") -> {
                // 从classpath加载
                val resourceName = configFilePath.substring(11)
                val resource = this::class.java.classLoader.getResource(resourceName)
                    ?: throw IllegalStateException("Resource not found in classpath: $resourceName")
                Paths.get(resource.toURI())
            }
            configFilePath.startsWith("/") -> {
                // 绝对路径
                Paths.get(configFilePath)
            }
            else -> {
                // 相对路径，相对于项目根目录
                Paths.get(System.getProperty("user.dir"), configFilePath)
            }
        }
        
        return try {
            if (Files.exists(configFile)) {
                val content = Files.readString(configFile)
                Json.decodeFromString<ConfigSnapshot>(content)
            } else {
                throw IllegalStateException("Config file not found: $configFilePath (resolved to: $configFile)")
            }
        } catch (e: Exception) {
            println("Error loading config file '$configFilePath': ${e.message}")
            throw IllegalStateException("Failed to load config file: ${e.message}", e)
        }
    }
    
    /**
     * 获取所有可用工具列表（聚合所有Client的工具）
     */
    suspend fun getAllTools(): List<String> {
        return allAvailableTools.toList()
    }
    
    /**
     * 调用工具
     */
    suspend fun callTool(toolName: String, parameters: Map<String, Any>): String {
        // 1. 查路由表：这个工具在哪个服务器？
        val serverId = toolToServerMap[toolName]  // 比如 "add" -> "math-server"
            ?: throw IllegalArgumentException("Tool '$toolName' not available")
        
        // 2. 找到对应的客户端
        val client = clients[serverId]  // 获取math-server的客户端
            ?: throw IllegalArgumentException("Client for server '$serverId' not found")
        
        // 3. 通过正确的客户端调用工具
        return client.callTool(toolName, parameters)
    }
    
    /**
     * 检查工具是否可用
     */
    fun isToolAvailable(toolName: String): Boolean {
        return toolToServerMap.containsKey(toolName)
    }
    
    /**
     * 获取工具规范（用于LLM）
     */
    suspend fun getToolSpecs(): List<Map<String, Any>> = kotlinx.coroutines.coroutineScope {
        clients.values.map { client ->
            kotlinx.coroutines.async {
                try {
                    client.getToolSpecs()
                } catch (e: Exception) {
                    println("Error getting tool specs from client: ${e.message}")
                    emptyList()
                }
            }
        }.awaitAll().flatten()
    }
    
    /**
     * 关闭所有Client连接
     */
    suspend fun close() {
        coroutineScope {
            clients.values.map { client ->
                async {
                    try {
                        client.close()
                    } catch (e: Exception) {
                        println("Error closing client: ${e.message}")
                    }
                }
            }.awaitAll()
        }
        clients.clear()
        toolToServerMap.clear()
        allAvailableTools.clear()
    }
    
    /**
     * 获取连接状态
     */
    fun getConnectionStatus(): Map<String, Boolean> {
        return clients.mapValues { (_, client) ->
            client.isConnected()
        }
    }
    
    /**
     * 获取Client连接详情
     */
    fun getClientDetails(): Map<String, Map<String, Any>> {
        return clients.mapValues { (serverId, client) ->
            mapOf(
                "serverId" to serverId,
                "isConnected" to client.isConnected(),
                "tools" to toolToServerMap.entries.filter { it.value == serverId }.map { it.key }
            )
        }
    }
}

/**
 * 服务器定义 - 对应配置文件格式
 */
@Serializable
data class ServerDef(
    val id: String,
    val cmd: List<String>? = null,
    val url: String? = null
)

/**
 * 配置快照 - 对应配置文件格式
 */
@Serializable
data class ConfigSnapshot(
    val servers: List<ServerDef>
)

/**
 * MCPClientManager工厂方法
 */
object MCPClientManagerFactory {
    /**
     * 创建MCPClientManager实例
     * @param hostName Host名称
     * @param configPath 配置文件路径，支持：
     *   - 相对路径: "config/mcp.json"
     *   - 绝对路径: "/path/to/config.json"
     *   - classpath: "classpath:config/mcp.json"
     */
    suspend fun create(hostName: String, configPath: String): MCPClientManager {
        val manager = MCPClientManager(hostName, configPath)
        manager.start()
        return manager
    }
    
    /**
     * 创建SecretaryAgent的MCPClientManager
     */
    suspend fun createForSecretary(): MCPClientManager {
        return create("SecretaryAgent", "app/src/main/java/com/aiteacher/ai/mcp/server/secretary-config.json")
    }
    
    /**
     * 创建TeachingAgent的MCPClientManager
     */
    suspend fun createForTeaching(): MCPClientManager {
        return create("TeachingAgent", "app/src/main/java/com/aiteacher/ai/mcp/server/teaching-config.json")
    }
}
