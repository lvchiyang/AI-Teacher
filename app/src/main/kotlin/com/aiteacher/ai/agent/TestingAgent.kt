package com.aiteacher.ai.agent

import android.util.Log
import com.aiteacher.ai.service.LLMModel
import com.aiteacher.ai.tool.BaseTool
import com.aiteacher.domain.model.Knowledge
import com.aiteacher.domain.model.Question
import com.aiteacher.domain.model.GradingResult
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * 测试Agent - 负责生成测试题和批改学生作答
 */
class TestingAgent(
    model: LLMModel = LLMModel("qwen-max"),
    tools: List<BaseTool> = emptyList()
) : BaseAgent(
    name = "TestingAgent",
    description = "测试代理，负责生成测试题和批改学生作答",
    model = model,
    tools = tools,
    memoryManagerName = "TestingAgent"
) {
    
    /**
     * 从配置文件创建 TestingAgent
     * 使用BaseAgent的动态工具加载功能
     */
    constructor(toolsConfigPath: String) : this(
        model = LLMModel("qwen-max"),
        tools = emptyList()  // 初始为空，后续通过loadToolsFromConfig动态加载
    ) {
        // 动态加载工具
        loadToolsFromConfig(toolsConfigPath)
    }
    
    override fun buildSystemPrompt(): String {
        return """
            你是测试Agent，负责根据知识点生成测试题和批改学生作答。
            
            你的主要职责：
            1. 根据知识点信息，生成10道相关的测试题
            2. 批改学生的作答，给出最终成绩（满分100分）
            
            出题要求：
            1. 题目类型应多样化（填空题、简答题等）
            2. 题目难度应适中，覆盖知识点的主要内容
            3. 每道题应有明确的答案和评分标准
            
            批改要求：
            1. 严格按照标准答案进行批改
            2. 给出每道题的得分和总分（满分100分）
            3. 提供简要的反馈意见
            
            按照以下JSON格式生成题目：
            [
            {
                "type": "题目类型，如单项选择、填空、简答",
                "question": "题目",
                "answer": "答案",
                "explanation": "题目解析",
                "difficulty": "难度等级，最高10级",
                "score": "题目分数"
            },
            ]
            
            请确保生成的题目总分为100，并在批改时给出详细的反馈。
        """.trimIndent()
    }
    
    /**
     * 生成测试题
     * @param knowledge 知识点信息
     * @return 测试题列表
     */
    suspend fun generateTestQuestions(knowledge: Knowledge): Result<List<Question>> {
        val prompt = """
            请根据以下知识点生成10道测试题：
            
            学科: ${knowledge.subject}
            年级: ${knowledge.grade}
            章节: ${knowledge.chapter}
            概念: ${knowledge.concept}
            
            请使用 question_retrieval 工具从数据库中检索相关题目，并结合自身知识补充题目，总共生成10道测试题。
            题目类型应多样化，包括选择题、填空题、简答题等。
            确保每道题都有明确的答案和适当的难度等级。
        """.trimIndent()
        
        return try {
            val result = runReAct(prompt)
            if (result.isSuccess) {
                val response = result.getOrThrow()
                // 从工具调用结果中提取题目
                val questions = extractQuestionsFromResponse(response)
                Result.success(questions)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Failed to generate test questions"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 批改学生作答
     * @param questions 测试题列表
     * @return 批改结果（成绩和反馈）
     */
    suspend fun gradeStudentAnswers(
        questions: List<Question>
    ): Result<List<GradingResult>> {
        val prompt = """
            请批改学生的测试题作答：
            
            ${
                questions.map{mapOf(
                    "question_id" to it.questionId,
                    "question" to it.questionText,
                    "answer" to it.answer,
                    "student_answer" to it.studentAnswer,
                )}
            }
            
            请批改学生作答，并给出最终成绩（满分100分）和反馈意见。
            请确保详细分析每道题的作答情况，并给出建设性的反馈。
            输出如下JSON格式的字符串:
            [
            {
                "question_id": "题号1",
                "score": "得分",
                "feedback": "批改意见"
            },
            {
                "question_id": "题号2",
                "score": "得分",
                "feedback": "批改意见"
            }
            ]""".trimIndent()
        
        return try {
            val result = runReAct(prompt)
            if (result.isSuccess) {
                val response = result.getOrThrow()
                // 从工具调用结果中提取评分结果
                val gradingResults = extractGradingResultFromResponse(response)
                Result.success(gradingResults)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Failed to grade student answers"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun extractQuestionsFromResponse(response: String): List<Question> {
        return try {
            // 尝试从响应中提取JSON部分
            val jsonStart = response.indexOf('[')
            val jsonEnd = response.lastIndexOf(']') + 1
            
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                val jsonStr = response.substring(jsonStart, jsonEnd)
                
                // 使用kotlinx-serialization解析JSON
                val json = Json { ignoreUnknownKeys = true }
                val jsonElement = json.parseToJsonElement(jsonStr)
                
                if (jsonElement is JsonArray) {
                    jsonElement.mapIndexed { index, element ->
                        parseQuestionFromJson(element, index.toString())
                    }
                } else {
                    emptyList()
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("TestingAgent", "解析题目时出错: ${e.message}", e)
            emptyList()
        }
    }

    private fun parseQuestionFromJson(jsonElement: JsonElement, questionId: String): Question {
        return try {
            val obj = jsonElement.jsonObject

            Question(
                questionId = questionId,
                subject = "",
                grade = 0,
                questionText = obj["question"]?.jsonPrimitive?.content ?: "",
                answer = obj["answer"]?.jsonPrimitive?.content ?: "",
                questionType = obj["type"]?.jsonPrimitive?.content ?: "",
                difficulty = obj["difficulty"]?.jsonPrimitive?.content?.toIntOrNull(),
                relatedKnowledgeIds = emptyList()
            )
        } catch (e: Exception) {
            Log.e("TestingAgent", "解析单个题目时出错: ${e.message}", e)
            // 返回默认题目
            Question(
                questionId = questionId,
                subject = "",
                grade = 0,
                questionText = "",
                answer = "",
                questionType = "",
                difficulty = null,
                relatedKnowledgeIds = emptyList()
            )
        }
    }
    
    private fun extractGradingResultFromResponse(response: String): List<GradingResult> {
        return try {
            // 尝试从响应中提取JSON部分
            val jsonStart = response.indexOf('[')
            val jsonEnd = response.lastIndexOf(']') + 1
            
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                val jsonStr = response.substring(jsonStart, jsonEnd)
                
                // 使用kotlinx-serialization解析JSON
                val json = Json { ignoreUnknownKeys = true }
                val jsonElement = json.parseToJsonElement(jsonStr)
                
                if (jsonElement is JsonArray) {
                    jsonElement.map { element ->
                        parseGradingResultFromJson(element)
                    }
                } else {
                    emptyList()
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("TestingAgent", "解析评分结果时出错: ${e.message}", e)
            emptyList()
        }
    }
    
    private fun parseGradingResultFromJson(jsonElement: JsonElement): GradingResult {
        return try {
            val obj = jsonElement.jsonObject
            
            GradingResult(
                questionId = obj["question_id"]?.jsonPrimitive?.content ?: "",
                score = obj["score"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0,
                feedback = obj["feedback"]?.jsonPrimitive?.content ?: ""
            )
        } catch (e: Exception) {
            Log.e("TestingAgent", "解析单个评分结果时出错: ${e.message}", e)
            // 返回默认评分结果
            GradingResult(
                questionId = "",
                score = 0,
                feedback = ""
            )
        }
    }
}
