package com.aiteacher.ai.mcp.client

import io.modelcontextprotocol.kotlin.sdk.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.coroutines.runBlocking

/**
 * MCP Client - 使用官方SDK实现
 * 用于Agent调用MCP Server的工具
 */
class MCPClient {
    private val client = Client(
        clientInfo = Implementation(name = "ai-teacher-client", version = "1.0.0")
    )
    
    private var isConnected = false
    
    /**
     * 连接到MCP服务器
     */
    suspend fun connect() {
        if (!isConnected) {
            client.connect(StdioClientTransport(System.`in`, System.out))
            client.initialize()
            isConnected = true
        }
    }
    
    /**
     * 调用知识库检索工具
     */
    suspend fun callKnowledgeBase(
        type: String = "syllabus",
        grade: Int = 7,
        subject: String = "数学",
        pointId: String = ""
    ): String {
        if (!isConnected) {
            throw IllegalStateException("Client not connected to server")
        }
        
        val parameters = buildJsonObject {
            put("type", type)
            put("grade", grade)
            put("subject", subject)
            if (pointId.isNotEmpty()) {
                put("point_id", pointId)
            }
        }
        
        val result = client.callTool("knowledge_base", parameters)
        return result.content.first().text
    }
    
    /**
     * 获取可用工具列表
     */
    suspend fun listTools(): List<String> {
        if (!isConnected) {
            throw IllegalStateException("Client not connected to server")
        }
        
        val tools = client.listTools()
        return tools.map { it.name }
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
            client.close()
            isConnected = false
        }
    }
}

/**
 * MCP Client管理器
 * 单例模式管理MCP客户端
 */
object MCPClientManager {
    private var instance: MCPClient? = null
    
    /**
     * 获取MCP客户端实例
     */
    fun getInstance(): MCPClient {
        return instance ?: MCPClient().also { instance = it }
    }
    
    /**
     * 初始化客户端连接
     */
    suspend fun initialize() {
        val client = getInstance()
        if (!client.isConnected()) {
            client.connect()
        }
    }
    
    /**
     * 关闭客户端连接
     */
    fun shutdown() {
        instance?.close()
        instance = null
    }
}