package com.aiteacher.ai.mcp.client

import com.aiteacher.ai.mcp.registry.MCPServerRegistry
import com.aiteacher.ai.mcp.registry.MCPServerInfo
import io.modelcontextprotocol.kotlin.sdk.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * MCP Host - 每个Agent作为Host，维护自己的Client集合
 * 按照MCP协议：一个Client对应一个Server，Host聚合多个Client
 */
class MCPHost(
    private val hostName: String,
    private val requiredTools: List<String>
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val clients = mutableMapOf<String, Client>() // serverId -> Client
    private val toolToServerMap = mutableMapOf<String, String>() // toolName -> serverId
    
    init {
        initializeClients()
    }
    
    /**
     * 初始化所有需要的Client连接
     */
    private fun initializeClients() {
        runBlocking {
            // 为每个需要的工具找到对应的服务器
            requiredTools.forEach { toolName ->
                val servers = MCPServerRegistry.findServersByTool(toolName)
                if (servers.isNotEmpty()) {
                    val serverInfo = servers.first() // 使用第一个找到的服务器
                    val serverId = serverInfo.serverId
                    
                    // 如果还没有为这个服务器创建Client，则创建
                    if (!clients.containsKey(serverId)) {
                        val client = createClient(serverInfo)
                        clients[serverId] = client
                    }
                    
                    // 建立工具到服务器的映射
                    toolToServerMap[toolName] = serverId
                } else {
                    println("Warning: No server found for tool '$toolName'")
                }
            }
        }
    }
    
    /**
     * 创建Client并连接到Server
     */
    private suspend fun createClient(serverInfo: MCPServerInfo): Client {
        val client = Client(
            clientInfo = Implementation(name = hostName, version = "1.0.0")
        )
        
        when (serverInfo.transportType) {
            "stdio" -> {
                val command = serverInfo.connectionInfo["command"] as? String
                    ?: throw IllegalArgumentException("No command specified for stdio server")
                
                val transport = StdioClientTransport(command.split(" "))
                client.connect(transport)
            }
            "websocket" -> {
                val url = serverInfo.connectionInfo["url"] as? String
                    ?: throw IllegalArgumentException("No URL specified for WebSocket server")
                
                val transport = WebSocketClientTransport(url)
                client.connect(transport)
            }
            "sse" -> {
                val url = serverInfo.connectionInfo["url"] as? String
                    ?: throw IllegalArgumentException("No URL specified for SSE server")
                
                val transport = SseClientTransport(url)
                client.connect(transport)
            }
            else -> throw IllegalArgumentException("Unsupported transport type: ${serverInfo.transportType}")
        }
        
        client.initialize()
        return client
    }
    
    /**
     * 获取所有可用工具列表（聚合所有Client的工具）
     */
    suspend fun getAllTools(): List<String> {
        val allTools = mutableListOf<String>()
        
        clients.values.forEach { client ->
            try {
                val tools = client.listTools()
                allTools.addAll(tools.map { it.name })
            } catch (e: Exception) {
                println("Error listing tools from client: ${e.message}")
            }
        }
        
        return allTools
    }
    
    /**
     * 调用工具
     */
    suspend fun callTool(toolName: String, parameters: Map<String, Any>): String {
        val serverId = toolToServerMap[toolName]
            ?: throw IllegalArgumentException("Tool '$toolName' not available")
        
        val client = clients[serverId]
            ?: throw IllegalArgumentException("Client for server '$serverId' not found")
        
        val jsonParams = buildJsonObject {
            parameters.forEach { (key, value) ->
                put(key, value.toString())
            }
        }
        
        val result = client.callTool(toolName, jsonParams)
        return result.content.first().text
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
    suspend fun getToolSpecs(): List<Map<String, Any>> {
        val specs = mutableListOf<Map<String, Any>>()
        
        clients.values.forEach { client ->
            try {
                val tools = client.listTools()
                tools.forEach { tool ->
                    specs.add(mapOf(
                        "type" to "function",
                        "function" to mapOf(
                            "name" to tool.name,
                            "description" to tool.description,
                            "parameters" to tool.inputSchema
                        )
                    ))
                }
            } catch (e: Exception) {
                println("Error getting tool specs from client: ${e.message}")
            }
        }
        
        return specs
    }
    
    /**
     * 关闭所有Client连接
     */
    fun close() {
        runBlocking {
            clients.values.forEach { client ->
                try {
                    client.close()
                } catch (e: Exception) {
                    println("Error closing client: ${e.message}")
                }
            }
            clients.clear()
            toolToServerMap.clear()
        }
    }
    
    /**
     * 获取连接状态
     */
    fun getConnectionStatus(): Map<String, Boolean> {
        return clients.mapValues { (_, client) ->
            try {
                // 这里可以添加检查连接状态的逻辑
                true // 简化实现
            } catch (e: Exception) {
                false
            }
        }
    }
}

