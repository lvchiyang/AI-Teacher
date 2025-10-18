package com.aiteacher.ai.mcp.server

import kotlinx.coroutines.runBlocking

/**
 * MCP服务器启动器
 * 启动MCP工具服务器
 */
fun main() = runBlocking {
    println("启动AI Teacher MCP服务器...")
    
    val server = MCPServer()
    
    // 初始化服务器
    server.initialize()
    
    println("MCP服务器已启动，等待连接...")
    println("可用工具: knowledge_base")
    
    // 启动stdio传输
    server.startStdio()
}