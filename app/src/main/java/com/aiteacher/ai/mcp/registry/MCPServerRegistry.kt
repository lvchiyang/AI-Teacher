package com.aiteacher.ai.mcp.registry

import kotlinx.coroutines.runBlocking

/**
 * MCP服务器注册表
 * 管理所有可用的MCP服务器
 */
object MCPServerRegistry {
    private val servers = mutableMapOf<String, MCPServerInfo>()
    
    /**
     * 注册MCP服务器
     */
    fun registerServer(serverId: String, serverInfo: MCPServerInfo) {
        servers[serverId] = serverInfo
    }
    
    /**
     * 获取服务器信息
     */
    fun getServer(serverId: String): MCPServerInfo? {
        return servers[serverId]
    }
    
    /**
     * 获取所有服务器
     */
    fun getAllServers(): Map<String, MCPServerInfo> {
        return servers.toMap()
    }
    
    /**
     * 根据工具名称查找提供该工具的服务器
     */
    fun findServersByTool(toolName: String): List<MCPServerInfo> {
        return servers.values.filter { serverInfo ->
            serverInfo.availableTools.contains(toolName)
        }
    }
    
    /**
     * 初始化默认服务器
     */
    fun initializeDefaultServers() {
        // 注册知识库服务器
        registerServer("knowledge_server", MCPServerInfo(
            serverId = "knowledge_server",
            name = "知识库服务器",
            description = "提供教学大纲和知识点检索服务",
            availableTools = listOf("knowledge_base"),
            transportType = "stdio",
            connectionInfo = mapOf("command" to "java -jar knowledge-server.jar")
        ))
        
        // 注册学习分析服务器
        registerServer("analysis_server", MCPServerInfo(
            serverId = "analysis_server", 
            name = "学习分析服务器",
            description = "提供学习进度分析和题目生成服务",
            availableTools = listOf("analyze_progress", "generate_questions"),
            transportType = "stdio",
            connectionInfo = mapOf("command" to "java -jar analysis-server.jar")
        ))
    }
}

/**
 * MCP服务器信息
 */
data class MCPServerInfo(
    val serverId: String,
    val name: String,
    val description: String,
    val availableTools: List<String>,
    val transportType: String, // "stdio", "sse", "websocket"
    val connectionInfo: Map<String, Any>
)
