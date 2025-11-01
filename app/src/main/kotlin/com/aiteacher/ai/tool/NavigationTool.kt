package com.aiteacher.ai.tool

import com.aiteacher.domain.model.Student
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 导航工具
 * 用于Agent跳转到不同的功能界面
 */
class NavigationTool(
    private val getCurrentStudent: () -> Student?,
    private val navigateTo: (route: String) -> Unit
) : BaseTool(
    toolName = "navigate_to_screen",
    toolDescription = """
        导航到指定界面。根据用户意图智能跳转到对应功能界面。
        
        【可用界面】
        - teaching_outline: 学科大纲界面（学习入口），需要 subject 参数
        - testing: 检验/做题界面（独立入口），需要 subject 参数
        - profile: 个人中心（无需额外参数）
        - view_statistics: 学习统计（无需额外参数）
        
        【参数说明】
        - screen: 目标界面标识（必需）
        - subject: 学科名称，如"数学"/"语文"/"英语"/"物理"/"化学"（仅 teaching_outline 和 testing 需要）
    """.trimIndent(),
    parameters = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "screen" to mapOf(
                "type" to "string",
                "enum" to listOf("teaching_outline", "testing", "profile", "view_statistics"),
                "description" to "目标界面标识"
            ),
            "subject" to mapOf(
                "type" to "string",
                "enum" to listOf("数学", "语文", "英语", "物理", "化学"),
                "description" to "学科名称（仅 teaching_outline 和 testing 需要此参数）"
            )
        ),
        "required" to listOf("screen")
    )
) {
    override suspend fun toolFunction(vararg args: Any): ToolResult {
        // BaseAgent会将整个toolArguments Map作为第一个参数传递
        val params = when {
            args.isEmpty() -> emptyMap<String, Any>()
            args[0] is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                args[0] as Map<String, Any>
            }
            else -> parseNavigationParams(args.firstOrNull())
        }
        val screen = params["screen"] as? String ?: return ToolResult.ExecuteResult.failure("错误：缺少screen参数")
        
        // 根据界面类型执行跳转
        return when (screen) {
            "teaching_outline" -> {
                val subject = params["subject"] as? String
                if (subject == null) {
                    return ToolResult.ExecuteResult.failure("错误：teaching_outline 需要 subject 参数。请提供学科名称（数学/语文/英语/物理/化学）。")
                }
                
                val student = getCurrentStudent()
                if (student == null) {
                    return ToolResult.ExecuteResult.failure("错误：需要登录学生信息才能访问学习页面。请先登录。")
                }
                
                // 在UI线程执行导航
                withContext(Dispatchers.Main) {
                    navigateTo("teaching_outline/$subject/${student.studentId}")
                }
                
                ToolResult.ExecuteResult.success("正在为您打开${subject}学习页面，从这里您可以查看学习大纲、开始教学和检验...")
            }
            
            "testing" -> {
                val subject = params["subject"] as? String
                if (subject == null) {
                    return ToolResult.ExecuteResult.failure("错误：testing 需要 subject 参数。请提供学科名称（数学/语文/英语/物理/化学）。")
                }
                
                val student = getCurrentStudent()
                if (student == null) {
                    return ToolResult.ExecuteResult.failure("错误：需要登录学生信息才能访问测试页面。请先登录。")
                }
                
                // 在UI线程执行导航
                withContext(Dispatchers.Main) {
                    navigateTo("testing/$subject/${student.studentId}/${student.studentName}/${student.grade}")
                }
                
                ToolResult.ExecuteResult.success("正在为您打开${subject}测试页面，您可以直接开始做题...")
            }
            
            "profile" -> {
                // profile 不需要额外检查，直接跳转
                withContext(Dispatchers.Main) {
                    navigateTo("profile")
                }
                ToolResult.ExecuteResult.success("正在打开个人中心...")
            }
            
            "view_statistics" -> {
                // view_statistics 不需要额外检查，直接跳转
                withContext(Dispatchers.Main) {
                    navigateTo("statistics")
                }
                ToolResult.ExecuteResult.success("正在查看您的学习统计...")
            }
            
            else -> ToolResult.ExecuteResult.failure("错误：未知的界面类型 '$screen'。可用界面：teaching_outline, testing, profile, view_statistics")
        }
    }
    
    /**
     * 解析导航参数
     */
    private fun parseNavigationParams(args: Any?): Map<String, Any> {
        return when (args) {
            is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                (args as Map<String, Any>)
            }
            is String -> {
                // 如果是JSON字符串，尝试解析（简化处理）
                emptyMap()
            }
            else -> emptyMap()
        }
    }
}

