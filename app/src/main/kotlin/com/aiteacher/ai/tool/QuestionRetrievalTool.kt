package com.aiteacher.ai.tool

import com.aiteacher.domain.model.Question

/**
 * 题目检索工具 - 用于从数据库中检索相关题目
 */
class QuestionRetrievalTool : BaseTool(
    toolName = "question_retrieval",
    toolDescription = "从数据库中检索与知识点相关的题目",
    parameters = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "knowledgeId" to mapOf(
                "type" to "string",
                "description" to "知识点ID，如 'M7-001'"
            ),
            "subject" to mapOf(
                "type" to "string",
                "description" to "学科，如 '数学'"
            ),
            "grade" to mapOf(
                "type" to "integer",
                "description" to "年级，如 7"
            ),
            "count" to mapOf(
                "type" to "integer",
                "description" to "需要检索的题目数量，默认为10",
                "minimum" to 1,
                "maximum" to 50
            )
        ),
        "required" to listOf("knowledgeId")
    )
) {
    
    /**
     * 模拟的题目数据库
     * 在实际应用中，这些数据应该从真实数据库中获取
     */
    private val questionDatabase = mapOf(
        "M7-001" to listOf(
            Question(
                questionId = "Q1001",
                subject = "数学",
                grade = 7,
                questionText = "下列哪个数是有理数？A) √2 B) π C) 1/3 D) 0.1010010001...",
                answer = "C",
                questionType = "选择题",
                difficulty = 1,
                relatedKnowledgeIds = listOf("M7-001")
            ),
            Question(
                questionId = "Q1002",
                subject = "数学",
                grade = 7,
                questionText = "请写出三个不同的有理数。",
                answer = "例如：1, -2, 3/4",
                questionType = "简答题",
                difficulty = 1,
                relatedKnowledgeIds = listOf("M7-001")
            )
        ),
        "M7-002" to listOf(
            Question(
                questionId = "Q2001",
                subject = "数学",
                grade = 7,
                questionText = "计算：(-3) + 5 = ?",
                answer = "2",
                questionType = "计算题",
                difficulty = 1,
                relatedKnowledgeIds = listOf("M7-002")
            )
        )
    )
    
    override suspend fun toolFunction(vararg args: Any): ToolResult {
        try {
            val params = args[0] as? Map<String, Any> ?: return ToolResult.QueryResult("参数错误")
            val knowledgeId = params["knowledgeId"] as? String ?: return ToolResult.QueryResult("缺少知识点ID")
            val count = (params["count"] as? Int ?: 10).coerceIn(1, 50)
            
            // 从数据库中检索题目
            val questions = questionDatabase[knowledgeId] ?: emptyList()
            
            // 如果题目数量不足，可以考虑从相关知识点中检索
            val relatedQuestions = if (questions.size < count) {
                // 简化实现，实际应用中可以检索相关知识点的题目
                questions
            } else {
                questions.take(count)
            }
            
            return if (relatedQuestions.isNotEmpty()) {
                ToolResult.QueryResult(mapOf(
                    "questions" to relatedQuestions,
                    "total" to relatedQuestions.size
                ))
            } else {
                ToolResult.QueryResult("未找到相关题目")
            }
        } catch (e: Exception) {
            return ToolResult.ExecuteResult.failure("检索题目时发生错误: ${e.message}")
        }
    }
}