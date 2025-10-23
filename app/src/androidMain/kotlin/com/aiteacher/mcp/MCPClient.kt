package com.aiteacher.ai.mcp

import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.client.*
import io.modelcontextprotocol.kotlin.sdk.shared.*
import kotlinx.coroutines.runBlocking

/**
 * 简单的MCP客户端，用于调用服务器工具
 */
class MCPClient {
    
    private val client = Client(
        clientInfo = Implementation(
            name = "simple-calculator-client",
            version = "1.0.0"
        ),
        options = ClientOptions(
            capabilities = ClientCapabilities(
                roots = ClientCapabilities.Roots(listChanged = true)
            )
        )
    )
    
    /**
     * 连接到服务器
     */
    suspend fun connectToServer(transport: Transport) {
        client.connect(transport)
    }
    
    /**
     * 调用加法工具
     */
    suspend fun addNumbers(a: Double, b: Double): String? {
        return try {
            val result = client.callTool(
                name = "add",
                arguments = mapOf(
                    "a" to a,
                    "b" to b
                ),
                compatibility = false,
                options = null
            )
            result?.content?.firstOrNull()?.let { content ->
                when (content) {
                    is TextContent -> content.text
                    else -> content.toString()
                }
            } ?: "No result"
        } catch (e: Exception) {
            "Error calling add tool: ${e.message}"
        }
    }
    
    /**
     * 调用减法工具
     */
    suspend fun subtractNumbers(a: Double, b: Double): String? {
        return try {
            val result = client.callTool(
                name = "subtract",
                arguments = mapOf(
                    "a" to a,
                    "b" to b
                ),
                compatibility = false,
                options = null
            )
            result?.content?.firstOrNull()?.let { content ->
                when (content) {
                    is TextContent -> content.text
                    else -> content.toString()
                }
            } ?: "No result"
        } catch (e: Exception) {
            "Error calling subtract tool: ${e.message}"
        }
    }
    
    /**
     * 列出可用工具
     */
    suspend fun listTools(): List<String> {
        return try {
            val result = client.listTools(
                request = ListToolsRequest(),
                options = null
            )
            result?.tools?.map { it.name } ?: emptyList()
        } catch (e: Exception) {
            listOf("Error listing tools: ${e.message}")
        }
    }
    
    /**
     * 关闭客户端连接
     */
    suspend fun close() {
        client.close()
    }
}
