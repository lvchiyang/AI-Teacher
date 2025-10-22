package com.aiteacher.ai.mcp

import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.client.Client
import io.modelcontextprotocol.kotlin.sdk.client.StdioClientTransport
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.serialization.json.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.nio.file.Files
import java.nio.file.Paths
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * MCP客户端管理器
 * 基于0.7.3版本的API实现，参考官方示例
 */
class MCPClientManager(
    private val hostName: String,
    private val configFilePath: String,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) : CoroutineScope by coroutineScope {
    
    private val clients = mutableMapOf<String, MCPClient>()
    private val toolToServerMap = mutableMapOf<String, String>()
    private val allAvailableTools = mutableSetOf<String>()
    
    /**
     * 启动所有客户端
     */
    suspend fun start() {
        try {
            loadConfig()
            initializeClients()
        } catch (e: Exception) {
            println("Error starting MCP clients: ${e.message}")
            throw e
        }
    }
    
    /**
     * 加载配置文件
     */
    private fun loadConfig() {
        val configFile = Paths.get(configFilePath)
        if (!Files.exists(configFile)) {
            throw IllegalArgumentException("Config file not found: $configFilePath")
        }
        
        val content = Files.readAllLines(configFile).joinToString("\n")
        val json = Json.parseToJsonElement(content).jsonObject
        
        val servers = json["servers"]?.jsonObject ?: throw IllegalArgumentException("No servers found in config")
        
        servers.forEach { (serverId, serverConfig) ->
            val serverDef = parseServerDefinition(serverId, serverConfig.jsonObject)
            // 这里可以添加服务器定义到某个集合中
        }
    }
    
    /**
     * 解析服务器定义
     */
    private fun parseServerDefinition(serverId: String, config: JsonObject): ServerDef {
        val cmd = config["cmd"]?.jsonObject?.let { cmdObj ->
            cmdObj["command"]?.jsonPrimitive?.content?.let { command ->
                val args = cmdObj["args"]?.jsonObject?.let { argsObj ->
                    argsObj.mapValues { it.value.jsonPrimitive.content }
                } ?: emptyMap()
                listOf(command) + args.values
            }
        }
        
        val url = config["url"]?.jsonPrimitive?.content
        
        return ServerDef(serverId, cmd, url)
    }
    
    /**
     * 初始化客户端
     */
    private suspend fun initializeClients() {
        // 简化实现，只支持stdio传输
        val serverDef = ServerDef("test-server", listOf("echo", "test"), null)
        val client = MCPClient(serverDef.id, serverDef, coroutineScope)
        clients[serverDef.id] = client
    }
    
    /**
     * 调用工具
     */
    suspend fun callTool(toolName: String, kwargs: Map<String, Any> = emptyMap()): Any {
        val serverId = toolToServerMap[toolName] ?: throw IllegalArgumentException("Tool $toolName not found")
        val client = clients[serverId] ?: throw IllegalArgumentException("Server $serverId not found")
        return client.callTool(toolName, kwargs)
    }
    
    /**
     * 获取所有可用工具
     */
    fun getAllTools(): List<String> {
        return allAvailableTools.toList()
    }
    
    /**
     * 获取工具规范
     */
    suspend fun getToolSpecs(): List<Map<String, Any>> {
        return clients.values.flatMap { runBlocking { it.getToolSpecs() } }
    }
    
    /**
     * 检查工具是否可用
     */
    fun isToolAvailable(toolName: String): Boolean {
        return toolName in allAvailableTools
    }
    
    /**
     * 关闭所有客户端
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
}

/**
 * MCP客户端
 * 基于官方示例实现
 */
class MCPClient(
    private val serverId: String,
    private val serverDef: ServerDef,
    private val coroutineScope: CoroutineScope
) {
    // 使用Client API，参考官方示例
    private val mcp: Client = Client(clientInfo = Implementation(name = "ai-teacher-client", version = "1.0.0"))
    
    private var isConnected = false
    
    /**
     * 连接到服务器
     */
    suspend fun connect() {
        if (!isConnected) {
            val transport = when {
                serverDef.cmd != null -> {
                    val process = ProcessBuilder(serverDef.cmd).start()
                    StdioClientTransport(
                        input = process.inputStream.asSource().buffered(),
                        output = process.outputStream.asSink().buffered()
                    )
                }
                serverDef.url != null -> {
                    // 暂时不支持SSE，只支持stdio
                    throw UnsupportedOperationException("SSE transport not implemented yet")
                }
                else -> throw IllegalArgumentException("Server definition must have either cmd or url")
            }
            
            mcp.connect(transport)
            isConnected = true
        }
    }
    
    /**
     * 调用工具
     */
    suspend fun callTool(toolName: String, kwargs: Map<String, Any> = emptyMap()): Any {
        if (!isConnected) {
            connect()
        }
        
        val result = mcp.callTool(toolName, kwargs)
        return result?.content?.firstOrNull()?.let { content ->
            when (content) {
                is TextContent -> content.text
                else -> content.toString()
            }
        } ?: ""
    }
    
    /**
     * 获取工具列表
     */
    suspend fun listTools(): List<String> {
        if (!isConnected) {
            connect()
        }
        
        val toolsResult = mcp.listTools()
        return toolsResult?.tools?.map { it.name } ?: emptyList()
    }
    
    /**
     * 获取工具规范
     */
    suspend fun getToolSpecs(): List<Map<String, Any>> {
        if (!isConnected) {
            connect()
        }
        
        val toolsResult = mcp.listTools()
        return toolsResult?.tools?.map { tool ->
            mapOf<String, Any>(
                "type" to "function",
                "function" to mapOf<String, Any>(
                    "name" to tool.name,
                    "description" to (tool.description ?: ""),
                    "parameters" to (tool.inputSchema ?: emptyMap<String, Any>())
                )
            )
        } ?: emptyList()
    }
    
    /**
     * 关闭连接
     */
    suspend fun close() {
        if (isConnected) {
            mcp.close()
            isConnected = false
        }
    }
}

/**
 * 服务器定义
 */
data class ServerDef(
    val id: String,
    val cmd: List<String>?,
    val url: String?
)