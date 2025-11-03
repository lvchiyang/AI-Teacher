package com.aiteacher.ai.tool

import com.aiteacher.domain.model.Knowledge
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * 知识库工具 - 用于查询知识点信息
 */
class KnowledgeBaseTool : BaseTool(
    toolName = "knowledge_base",
    toolDescription = "查询知识点信息，包括概念描述、应用方法等",
    parameters = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "knowledgeKeyWords" to mapOf(
                "type" to "string",
                "description" to "知识点关键字，如 '三角函数'"
            ),
        ),
        "required" to listOf("knowledge")
    )
) {
    
    /**
     * 模拟的知识库数据
     * 在实际应用中，这些数据应该从数据库或API获取
     */
    private val knowledgeBase = mapOf(
        "有理数" to Knowledge(
            knowledgeId = "M7-001",
            subject = "数学",
            grade = 7,
            chapter = "有理数",
            concept = "有理数的概念和基本性质",
            applicationMethods = listOf(
                "判断一个数是否为有理数",
                "将有理数分类为正有理数、负有理数或零",
                "在数轴上表示有理数"
            ),
            keywords = listOf("有理数", "整数", "分数", "数轴")
        ),
        "加减法" to Knowledge(
            knowledgeId = "M7-002",
            subject = "数学",
            grade = 7,
            chapter = "有理数",
            concept = "有理数的加减法运算",
            applicationMethods = listOf(
                "同号两数相加",
                "异号两数相加",
                "有理数减法转化为加法"
            ),
            keywords = listOf("有理数加法", "有理数减法", "运算规则")
        )
    )
    
    override suspend fun toolFunction(vararg args: Any): ToolResult {
        try {
            val params = args[0] as? Map<String, Any> ?: return ToolResult.QueryResult("参数错误")
            val knowledgeId = params["knowledgeId"] as? String ?: return ToolResult.QueryResult("未找到知识点")
            
            // 查询知识点，先简单实现
            val knowledge = knowledgeBase[knowledgeId]
            
            return if (knowledge != null) {
                ToolResult.QueryResult(knowledge)
            } else {
                ToolResult.QueryResult("未找到指定的知识点")
            }
        } catch (e: Exception) {
            return ToolResult.ExecuteResult.failure("查询知识点时发生错误: ${e.message}")
        }
    }
}