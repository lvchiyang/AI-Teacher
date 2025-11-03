package com.aiteacher.ai.agent

import com.aiteacher.ai.service.LLMModel
import com.aiteacher.ai.tool.BaseTool
import com.aiteacher.domain.model.Knowledge

/**
 * 教师Agent - 负责知识点讲解和学习效果评估
 */
class TeacherAgent(
    model: LLMModel = LLMModel("qwen-max"),
    tools: List<BaseTool> = emptyList()
) : BaseAgent(
    name = "TeacherAgent",
    description = "教师代理，负责知识点讲解和学习效果评估",
    model = model,
    tools = tools,
    memoryManagerName = "TeacherAgent"
) {

    /**
     * 从配置文件创建 TeacherAgent
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
            你是教师Agent，负责知识点的详细讲解和学生学习效果的评估。
            
            你的主要职责：
            1. 接收知识点信息，进行深入浅出的讲解
            2. 与学生交流，判断其对知识点的掌握情况
            3. 根据掌握情况决定是否需要补充讲解
            
            教学流程：
            1. 首先对知识点进行详细讲解，确保学生理解
            2. 通过提问等方式与学生交流，评估掌握情况
            3. 如果掌握良好，使用finish工具结束教学；如果不好，则进行补充讲解
            
            你可以使用以下工具：
            1. knowledge_base - 查询知识点详细信息
            2. finish - 结束当前知识点教学过程
            
            教学对话管理：
            1. 开始教学时，提供知识点的详细讲解
            2. 讲解后主动提问检查学生理解
            3. 根据学生回答判断掌握情况
            4. 掌握良好时使用finish工具结束，掌握不佳时进行补充讲解
            
            请使用友好、耐心的语调进行教学，确保学生能够理解。
            通过多轮对话与学生交流，自行判断学生的掌握情况。
        """.trimIndent()
    }

    /**
     * 开始教学对话
     * @param knowledge 要讲解的知识点
     * @return 初始讲解内容
     */
    suspend fun startTeachingSession(knowledge: Knowledge): Result<String> {
        val prompt = """
            请为学生讲解以下知识点：
            
            学科: ${knowledge.subject}
            年级: ${knowledge.grade}
            章节: ${knowledge.chapter}
            概念: ${knowledge.concept}
            
            请使用通俗易懂的语言进行讲解，并提供相关的应用方法和示例。
            讲解完成后，请主动提出问题检查学生的理解程度。
        """.trimIndent()

        return try {
            val result = runReAct(prompt)
            if (result.isSuccess) {
                val response = result.getOrThrow()
                Result.success(response)
            } else {
                Result.failure(
                    result.exceptionOrNull() ?: Exception("Failed to start teaching session")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


/**
 * 教学对话状态
 */
data class TeachingSessionState(
    val knowledge: Knowledge,
    val explanationGiven: Boolean = false,
    val isFinished: Boolean = false,
    val needFurtherExplanation: Boolean = false
)

/**
 * 评估结果
 */
data class AssessmentResult(
    val understandingLevel: String,  // good or poor
    val feedback: String,
    val needFurtherExplanation: Boolean
)