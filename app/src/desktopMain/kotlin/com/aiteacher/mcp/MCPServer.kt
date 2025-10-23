package com.aiteacher.ai.mcp

import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.server.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 简单的MCP服务器，提供基本的数学运算工具
 * 使用0.7.3 API标准实现
 */
class MCPServer {
    
    private val server = Server(
        serverInfo = Implementation(
            name = "simple-calculator-server",
            version = "1.0.0"
        ),
        options = ServerOptions(
            capabilities = ServerCapabilities(
                tools = ServerCapabilities.Tools(listChanged = true)
            )
        )
    )
    
    init {
        setupTools()
    }
    
    /**
     * 设置服务器工具
     */
    private fun setupTools() {
        // 加法工具
        server.addTool(
            name = "add",
            description = "Add two numbers",
            inputSchema = Tool.Input(
                properties = buildJsonObject {
                    put("a", buildJsonObject {
                        put("type", "number")
                        put("description", "First number")
                    })
                    put("b", buildJsonObject {
                        put("type", "number")
                        put("description", "Second number")
                    })
                },
                required = listOf("a", "b")
            )
        ) { request ->
            try {
                val a = (request.arguments["a"] as? JsonPrimitive)?.content?.toDoubleOrNull() ?: 0.0
                val b = (request.arguments["b"] as? JsonPrimitive)?.content?.toDoubleOrNull() ?: 0.0
                val result = a + b
                CallToolResult(
                    content = listOf(TextContent("Result: $result"))
                )
            } catch (e: Exception) {
                CallToolResult(
                    content = listOf(TextContent("Error: ${e.message}")),
                    isError = true
                )
            }
        }
        
        // 减法工具
        server.addTool(
            name = "subtract",
            description = "Subtract two numbers",
            inputSchema = Tool.Input(
                properties = buildJsonObject {
                    put("a", buildJsonObject {
                        put("type", "number")
                        put("description", "First number")
                    })
                    put("b", buildJsonObject {
                        put("type", "number")
                        put("description", "Second number")
                    })
                },
                required = listOf("a", "b")
            )
        ) { request ->
            try {
                val a = (request.arguments["a"] as? JsonPrimitive)?.content?.toDoubleOrNull() ?: 0.0
                val b = (request.arguments["b"] as? JsonPrimitive)?.content?.toDoubleOrNull() ?: 0.0
                val result = a - b
                CallToolResult(
                    content = listOf(TextContent("Result: $result"))
                )
            } catch (e: Exception) {
                CallToolResult(
                    content = listOf(TextContent("Error: ${e.message}")),
                    isError = true
                )
            }
        }
        
        // 乘法工具
        server.addTool(
            name = "multiply",
            description = "Multiply two numbers",
            inputSchema = Tool.Input(
                properties = buildJsonObject {
                    put("a", buildJsonObject {
                        put("type", "number")
                        put("description", "First number")
                    })
                    put("b", buildJsonObject {
                        put("type", "number")
                        put("description", "Second number")
                    })
                },
                required = listOf("a", "b")
            )
        ) { request ->
            try {
                val a = (request.arguments["a"] as? JsonPrimitive)?.content?.toDoubleOrNull() ?: 0.0
                val b = (request.arguments["b"] as? JsonPrimitive)?.content?.toDoubleOrNull() ?: 0.0
                val result = a * b
                CallToolResult(
                    content = listOf(TextContent("Result: $result"))
                )
            } catch (e: Exception) {
                CallToolResult(
                    content = listOf(TextContent("Error: ${e.message}")),
                    isError = true
                )
            }
        }
        
        // 除法工具
        server.addTool(
            name = "divide",
            description = "Divide two numbers",
            inputSchema = Tool.Input(
                properties = buildJsonObject {
                    put("a", buildJsonObject {
                        put("type", "number")
                        put("description", "First number")
                    })
                    put("b", buildJsonObject {
                        put("type", "number")
                        put("description", "Second number")
                    })
                },
                required = listOf("a", "b")
            )
        ) { request ->
            try {
                val a = (request.arguments["a"] as? JsonPrimitive)?.content?.toDoubleOrNull() ?: 0.0
                val b = (request.arguments["b"] as? JsonPrimitive)?.content?.toDoubleOrNull() ?: 0.0
                
                if (b == 0.0) {
                    CallToolResult(
                        content = listOf(TextContent("Error: Division by zero")),
                        isError = true
                    )
                } else {
                    val result = a / b
                    CallToolResult(
                        content = listOf(TextContent("Result: $result"))
                    )
                }
            } catch (e: Exception) {
                CallToolResult(
                    content = listOf(TextContent("Error: ${e.message}")),
                    isError = true
                )
            }
        }
    }
    
    /**
     * 获取服务器实例
     */
    fun getServer(): Server {
        return server
    }
    
    /**
     * 启动服务器（用于测试）
     */
    fun start() {
        println("Simple MCP Server started with tools: add, subtract, multiply, divide")
    }
}