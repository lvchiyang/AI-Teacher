package com.aiteacher.ai.agent

import com.aiteacher.ai.service.LLMModel
import com.aiteacher.ai.service.LLMOutput

/**
 * 教秘Agent - 负责制定教学计划
 */
class SecretaryAgent(
    model: LLMModel = LLMModel("qwen-max"),
    configFilePath: String = "app/src/main/java/com/aiteacher/ai/mcp/server/mcp.json"
) : BaseAgent(
    name = "SecretaryAgent",
    description = "教秘代理，负责整体教学计划和进度管理",
    model = model,
    configFilePath = configFilePath
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
    
    // SecretaryAgent不需要重写这些方法，使用BaseAgent的默认实现
    
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
                
                // 简单的JSON解析（实际项目中应使用JSON库）
                val reviewPoints = extractJsonArray(jsonStr, "reviewKnowledgePoints") 
                    ?: listOf("7_1_1_1", "7_1_1_2")
                val newPoints = extractJsonArray(jsonStr, "newKnowledgePoints") 
                    ?: listOf("7_1_1_3")
                val duration = extractJsonInt(jsonStr, "estimatedDuration") ?: 30
                val sequence = extractJsonArray(jsonStr, "teachingSequence") 
                    ?: listOf("复习基础概念", "学习新知识点")
                
                TeachingPlanResult(
                    reviewKnowledgePoints = reviewPoints,
                    newKnowledgePoints = newPoints,
                    estimatedDuration = duration,
                    planDescription = sequence.joinToString(" -> ")
                )
            } else {
                // 如果无法解析JSON，使用默认值
                TeachingPlanResult(
                    reviewKnowledgePoints = listOf("7_1_1_1", "7_1_1_2"),
                    newKnowledgePoints = listOf("7_1_1_3"),
                    estimatedDuration = 30,
                    planDescription = "今日教学计划：复习有理数基础概念，学习有理数运算"
                )
            }
        } catch (e: Exception) {
            // 解析失败时返回默认值
            TeachingPlanResult(
                reviewKnowledgePoints = listOf("7_1_1_1", "7_1_1_2"),
                newKnowledgePoints = listOf("7_1_1_3"),
                estimatedDuration = 30,
                planDescription = "今日教学计划：复习有理数基础概念，学习有理数运算"
            )
        }
    }
    
    /**
     * 从JSON字符串中提取数组
     */
    private fun extractJsonArray(jsonStr: String, key: String): List<String>? {
        val pattern = "\"$key\"\\s*:\\s*\\[(.*?)\\]".toRegex()
        val match = pattern.find(jsonStr)
        return match?.groupValues?.get(1)?.let { arrayContent ->
            arrayContent.split(",").map { it.trim().removeSurrounding("\"") }
        }
    }
    
    /**
     * 从JSON字符串中提取整数
     */
    private fun extractJsonInt(jsonStr: String, key: String): Int? {
        val pattern = "\"$key\"\\s*:\\s*(\\d+)".toRegex()
        val match = pattern.find(jsonStr)
        return match?.groupValues?.get(1)?.toIntOrNull()
    }
}

/**
 * 教学计划结果
 */
data class TeachingPlanResult(
    val reviewKnowledgePoints: List<String>,
    val newKnowledgePoints: List<String>,
    val estimatedDuration: Int,
    val planDescription: String
)

/**
 * 知识库检索工具
 * 用于检索教学大纲和知识点信息
 */
class KnowledgeBaseTool : BaseTool(
    toolName = "knowledge_base",
    toolDescription = "检索教学大纲和知识点信息",
    parameters = mapOf(
        "type" to mapOf(
            "type" to "string",
            "description" to "检索类型：syllabus(教学大纲)、knowledge_point(知识点)、chapter(章节)",
            "enum" to listOf("syllabus", "knowledge_point", "chapter")
        ),
        "grade" to mapOf(
            "type" to "integer",
            "description" to "年级：7(初一)、8(初二)、9(初三)",
            "minimum" to 7,
            "maximum" to 9
        ),
        "subject" to mapOf(
            "type" to "string",
            "description" to "学科，默认为数学",
            "default" to "数学"
        ),
        "keyword" to mapOf(
            "type" to "string",
            "description" to "搜索关键词，可选"
        )
    )
) {
    init {
        setFunction { parameters ->
            val type = parameters["type"] as? String ?: "syllabus"
            val grade = parameters["grade"] as? Int ?: 7
            val subject = parameters["subject"] as? String ?: "数学"
            val keyword = parameters["keyword"] as? String
            
            when (type) {
                "syllabus" -> getSyllabus(grade, subject)
                "knowledge_point" -> getKnowledgePoints(grade, subject, keyword)
                "chapter" -> getChapters(grade, subject)
                else -> "未知的检索类型: $type"
            }
        }
    }
    
    /**
     * 获取教学大纲
     */
    private fun getSyllabus(grade: Int, subject: String): String {
        return when (grade) {
            7 -> """
                ${subject}七年级教学大纲：
                
                一、数与代数
                1. 有理数
                   - 正数、负数、相反数、绝对值
                   - 数轴表示
                   - 有理数的加减乘除运算
                   - 运算律（交换律、结合律、分配律）
                   - 科学记数法
                
                2. 整式
                   - 单项式、多项式
                   - 同类项合并
                   - 整式加减、乘法（乘法公式）
                
                3. 一元一次方程
                   - 解法步骤：去分母、去括号、移项、合并同类项、系数化为1
                   - 应用题：行程问题、工程问题、利润问题等
                
                二、图形与几何
                1. 几何基础
                   - 点、线、面、角
                   - 角的分类与度量
                   - 相交线与平行线
                
                2. 三角形
                   - 分类：按边、按角
                   - 性质：内角和180°、两边之和大于第三边
                   - 全等三角形判定：SSS、SAS、ASA、AAS、HL
            """.trimIndent()
            
            8 -> """
                ${subject}八年级教学大纲：
                
                一、数与代数
                1. 分式
                   - 分式的基本性质
                   - 分式的加减乘除
                   - 分式方程解法（注意验根）
                
                2. 一元一次不等式（组）
                   - 解法与数轴表示
                   - 解集、交集、并集
                
                3. 二元一次方程组
                   - 代入法、加减法
                   - 应用题：鸡兔同笼、年龄问题、溶液问题等
                
                4. 一次函数
                   - 形式：y = kx + b
                   - 图像：直线
                   - 性质：斜率k、截距b
                
                二、图形与几何
                1. 四边形
                   - 平行四边形、矩形、菱形、正方形性质与判定
                   - 梯形、等腰梯形
                
                2. 相似与投影
                   - 相似三角形判定：AA、SAS、SSS
                   - 相似比、面积比、体积比
            """.trimIndent()
            
            9 -> """
                ${subject}九年级教学大纲：
                
                一、数与代数
                1. 一元二次方程
                   - 解法：配方法、公式法、因式分解法
                   - 判别式：Δ = b² - 4ac
                   - 根与系数关系
                
                2. 二次函数
                   - 形式：y = ax² + bx + c
                   - 图像：抛物线
                   - 顶点坐标、开口方向、对称轴、最值
                
                二、图形与几何
                1. 圆
                   - 圆的基本性质：半径、直径、弦、弧、圆心角、圆周角
                   - 垂径定理
                   - 圆周角定理
                   - 切线性质与判定
                
                三、统计与概率
                1. 数据统计
                   - 平均数、中位数、众数
                   - 极差、方差、标准差
                
                2. 概率
                   - 事件、样本空间
                   - 概率计算公式
            """.trimIndent()
            
            else -> "暂不支持该年级的教学大纲"
        }
    }
    
    /**
     * 获取知识点
     */
    private fun getKnowledgePoints(grade: Int, subject: String, keyword: String?): String {
        val syllabus = getSyllabus(grade, subject)
        
        return if (keyword != null) {
            // 根据关键词过滤知识点
            val lines = syllabus.split("\n")
            val filteredLines = lines.filter { line ->
                line.contains(keyword, ignoreCase = true)
            }
            
            if (filteredLines.isNotEmpty()) {
                "找到相关知识点：\n${filteredLines.joinToString("\n")}"
            } else {
                "未找到包含关键词'$keyword'的知识点"
            }
        } else {
            syllabus
        }
    }
    
    /**
     * 获取章节信息
     */
    private fun getChapters(grade: Int, subject: String): String {
        return when (grade) {
            7 -> """
                ${subject}七年级章节：
                第一章 有理数
                第二章 整式
                第三章 一元一次方程
                第四章 几何图形初步
                第五章 相交线与平行线
                第六章 实数
                第七章 平面直角坐标系
                第八章 二元一次方程组
                第九章 不等式与不等式组
                第十章 数据的收集、整理与描述
            """.trimIndent()
            
            8 -> """
                ${subject}八年级章节：
                第一章 三角形
                第二章 全等三角形
                第三章 轴对称
                第四章 整式的乘法与因式分解
                第五章 分式
                第六章 二次根式
                第七章 勾股定理
                第八章 平行四边形
                第九章 一次函数
                第十章 数据的分析
            """.trimIndent()
            
            9 -> """
                ${subject}九年级章节：
                第一章 一元二次方程
                第二章 二次函数
                第三章 旋转
                第四章 圆
                第五章 概率初步
                第六章 反比例函数
                第七章 相似
                第八章 锐角三角函数
                第九章 投影与视图
            """.trimIndent()
            
            else -> "暂不支持该年级的章节信息"
        }
    }
}
