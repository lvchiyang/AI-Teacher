package com.aiteacher.ai.mcp.server

import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.server.*
import kotlinx.serialization.json.JsonObject
import kotlinx.coroutines.runBlocking

/**
 * MCP Server - 使用官方SDK实现
 * 只实现一个知识库检索工具
 */
class MCPServer {
    private val server = Server(
        serverInfo = Implementation(name = "ai-teacher-server", version = "1.0.0"),
        options = ServerOptions(
            capabilities = ServerCapabilities(
                tools = ServerCapabilities.Tools(listChanged = true)
            )
        )
    )
    
    /**
     * 初始化服务器
     */
    fun initialize() {
        // 注册知识库检索工具
        server.addTool(
            name = "knowledge_base",
            description = "检索教学大纲和知识点信息"
        ) { args: JsonObject? ->
            val type = args?.get("type")?.toString()?.removeSurrounding("\"") ?: "syllabus"
            val grade = args?.get("grade")?.toString()?.toIntOrNull() ?: 7
            val subject = args?.get("subject")?.toString()?.removeSurrounding("\"") ?: "数学"
            val pointId = args?.get("point_id")?.toString()?.removeSurrounding("\"") ?: ""
            
            val result = when (type) {
                "syllabus" -> getSyllabus(grade, subject)
                "knowledge_point" -> getKnowledgePoint(pointId)
                else -> "未知的检索类型: $type"
            }
            
            CallToolResult(listOf(TextContent(result)))
        }
    }
    
    /**
     * 启动stdio传输
     */
    fun startStdio() = runBlocking {
        server.connect(StdioServerTransport())
        server.awaitClose()
    }
    
    /**
     * 获取教学大纲
     */
    private fun getSyllabus(grade: Int, subject: String): String {
        return when {
            grade == 7 && subject == "数学" -> """
            {
                "grade": 7,
                "subject": "数学",
                "chapters": [
                    {
                        "id": "7_1",
                        "name": "第一章 有理数",
                        "knowledge_points": [
                            {"id": "7_1_1", "name": "有理数的概念", "difficulty": "easy"},
                            {"id": "7_1_2", "name": "有理数的运算", "difficulty": "medium"},
                            {"id": "7_1_3", "name": "有理数的应用", "difficulty": "hard"}
                        ]
                    },
                    {
                        "id": "7_2",
                        "name": "第二章 整式的加减",
                        "knowledge_points": [
                            {"id": "7_2_1", "name": "整式的概念", "difficulty": "easy"},
                            {"id": "7_2_2", "name": "整式的加减运算", "difficulty": "medium"}
                        ]
                    }
                ]
            }
            """.trimIndent()
            else -> "暂不支持该年级和学科的教学大纲"
        }
    }
    
    /**
     * 获取知识点详情
     */
    private fun getKnowledgePoint(pointId: String): String {
        return when (pointId) {
            "7_1_1" -> """
            {
                "id": "7_1_1",
                "name": "有理数的概念",
                "description": "理解有理数的定义，掌握有理数的分类",
                "key_points": [
                    "有理数包括整数和分数",
                    "有理数可以用分数形式表示",
                    "有理数在数轴上的表示"
                ],
                "examples": [
                    "1/2是有理数",
                    "-3是有理数",
                    "0是有理数"
                ],
                "difficulty": "easy"
            }
            """.trimIndent()
            "7_1_2" -> """
            {
                "id": "7_1_2",
                "name": "有理数的运算",
                "description": "掌握有理数的四则运算规则",
                "key_points": [
                    "同号两数相加，取相同的符号",
                    "异号两数相加，取绝对值大的数的符号",
                    "有理数乘法：同号得正，异号得负"
                ],
                "examples": [
                    "(-3) + (-5) = -8",
                    "(-3) + 5 = 2",
                    "(-3) × 4 = -12"
                ],
                "difficulty": "medium"
            }
            """.trimIndent()
            else -> "知识点ID: $pointId 不存在"
        }
    }
}