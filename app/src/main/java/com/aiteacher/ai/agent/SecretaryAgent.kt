package com.aiteacher.ai.agent

import com.aiteacher.ai.service.LLMModel
import com.aiteacher.ai.service.LLMOutput
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * 教秘Agent - 负责制定教学计划
 */
class SecretaryAgent(
    model: LLMModel = LLMModel("qwen-max")
) : BaseAgent(
    name = "SecretaryAgent",
    description = "教秘代理，负责整体教学计划和进度管理",
    model = model
) {
    
    override fun buildSystemPrompt(): String {
        return """
            你是教秘Agent，负责制定教学计划和管理学习进度。
            
            你的主要职责：
            1. 根据学生的学习进度制定教学计划
            2. 分析需要复习的知识点
            3. 规划新学习的知识点
            4. 合理安排学习时间
            
            制定教学计划的流程：
            1. 首先使用knowledge_base工具检索相关年级的教学大纲
            2. 分析学生的LearningProgress，确定需要复习的知识点
            3. 根据当前章节，确定新学习的知识点
            4. 制定合理的教学计划，包括复习和新学内容
            
            请根据学生的LearningProgress和当前章节，制定合理的教学计划。
        """.trimIndent()
    }
    
    /**
     * 制定教学计划
     * 这是教秘Agent的核心能力，通过调用LLM直接完成
     */
    suspend fun createTeachingPlan(
        studentId: String,
        grade: Int,
        currentChapter: String,
        learningProgress: com.aiteacher.domain.model.LearningProgress
    ): Result<TeachingPlanResult> {
        val prompt = """
            请为以下学生制定教学计划：
            
            学生ID: $studentId
            年级: $grade
            当前章节: $currentChapter
            
            学习进度:
            - 已讲解待复习: ${learningProgress.taughtToReview.joinToString(", ")}
            - 未掌握: ${learningProgress.notMastered.joinToString(", ")}
            - 初步掌握: ${learningProgress.basicMastery.joinToString(", ")}
            - 熟练掌握: ${learningProgress.fullMastery.joinToString(", ")}
            
            请按照以下步骤制定教学计划：
            1. 首先使用knowledge_base工具检索${grade}年级的数学教学大纲
            2. 分析学生的学习进度，确定需要复习的知识点
            3. 根据当前章节"$currentChapter"，确定新学习的知识点
            4. 制定合理的教学计划，包括复习和新学内容
            5. 估算学习时间
            
            重要：请严格按照以下JSON格式返回教学计划，不要添加任何其他文字：
            {
                "reviewKnowledgePoints": ["知识点1", "知识点2"],
                "newKnowledgePoints": ["新知识点1", "新知识点2"],
                "estimatedDuration": 30,
                "teachingSequence": ["步骤1", "步骤2", "步骤3"]
            }
            
            请开始制定教学计划。
        """.trimIndent()
        
        return try {
            val result = runOnce(prompt)
            if (result.isSuccess) {
                val plan = parseTeachingPlan(result.getOrThrow())
                Result.success(plan)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Failed to create teaching plan"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun parseTeachingPlan(response: String): TeachingPlanResult {
        return try {
            // 尝试从响应中提取JSON部分
            val jsonStart = response.indexOf('{')
            val jsonEnd = response.lastIndexOf('}') + 1
            
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                val jsonStr = response.substring(jsonStart, jsonEnd)
                
                // 使用kotlinx-serialization解析JSON
                val json = Json { ignoreUnknownKeys = true }
                val planJson = json.decodeFromString<TeachingPlanJson>(jsonStr)
                
                TeachingPlanResult(
                    reviewKnowledgePoints = planJson.reviewKnowledgePoints,
                    newKnowledgePoints = planJson.newKnowledgePoints,
                    estimatedDuration = planJson.estimatedDuration,
                    planDescription = planJson.teachingSequence.joinToString(" -> ")
                )
            } else {
                // 如果无法找到JSON，使用默认值
                getDefaultTeachingPlan()
            }
        } catch (e: Exception) {
            // 解析失败时返回默认值
            println("Error parsing teaching plan JSON: ${e.message}")
            getDefaultTeachingPlan()
        }
    }
    
    /**
     * 获取默认教学计划
     */
    private fun getDefaultTeachingPlan(): TeachingPlanResult {
        return TeachingPlanResult(
            reviewKnowledgePoints = listOf("7_1_1_1", "7_1_1_2"),
            newKnowledgePoints = listOf("7_1_1_3"),
            estimatedDuration = 30,
            planDescription = "今日教学计划：复习有理数基础概念，学习有理数运算"
        )
    }
}

/**
 * 教学计划JSON数据类
 */
@Serializable
data class TeachingPlanJson(
    val reviewKnowledgePoints: List<String>,
    val newKnowledgePoints: List<String>,
    val estimatedDuration: Int,
    val teachingSequence: List<String>
)

/**
 * 教学计划结果
 */
data class TeachingPlanResult(
    val reviewKnowledgePoints: List<String>,
    val newKnowledgePoints: List<String>,
    val estimatedDuration: Int,
    val planDescription: String
)