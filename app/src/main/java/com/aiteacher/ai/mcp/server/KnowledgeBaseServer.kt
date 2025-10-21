package com.aiteacher.ai.mcp.server

import io.modelcontextprotocol.kotlin.sdk.server.*
import io.modelcontextprotocol.kotlin.sdk.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 知识库检索MCP服务器
 * 提供教学大纲和知识点检索功能
 */
class KnowledgeBaseServer {
    
    private val running = AtomicBoolean(false)
    
    /**
     * 启动知识库服务器
     */
    fun start() {
        running.set(true)
        println("Knowledge Base Server started")
    }
    
    /**
     * 停止知识库服务器
     */
    fun stop() {
        running.set(false)
        println("Knowledge Base Server stopped")
    }
    
    /**
     * 检查服务器是否运行
     */
    fun isRunning(): Boolean = running.get()
    
    /**
     * 添加知识库工具到MCP服务器
     */
    fun McpServerScope.addKnowledgeTools() {
        tool(
            name = "searchKBase",
            description = "搜索知识点",
            inputSchema = ToolInputSchema.Json(json {})
        ) { params ->
            val result = searchKnowledgeBase(params)
            CallToolResult(listOf(TextContent(result)), isError = false)
        }
    }

/**
 * 知识库检索功能
 * 用于检索教学大纲和知识点信息
 */
fun searchKnowledgeBase(params: JsonObject): String {
    val type = params["type"]?.toString()?.removeSurrounding("\"") ?: "syllabus"
    val grade = params["grade"]?.toString()?.toIntOrNull() ?: 7
    val subject = params["subject"]?.toString()?.removeSurrounding("\"") ?: "数学"
    val keyword = params["keyword"]?.toString()?.removeSurrounding("\"")
    
    return when (type) {
        "syllabus" -> getSyllabus(grade, subject)
        "knowledge_point" -> getKnowledgePoints(grade, subject, keyword)
        "chapter" -> getChapters(grade, subject)
        else -> "未知的检索类型: $type"
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
