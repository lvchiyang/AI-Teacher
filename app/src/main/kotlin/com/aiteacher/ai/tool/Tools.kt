package com.aiteacher.ai.tool

/**
 * 工具索引文件
 * 提供所有工具的便捷导入和工具注册
 * 
 * 【重要说明：工具配置文件格式】
 * 
 * Agent 从工具配置文件中读取的是工具的 `toolName`，而不是文件名或类名。
 * 
 * 工具名称映射关系：
 * - 配置文件中的工具名 = 工具类构造函数中的 `toolName` 参数值
 * - MathTool.kt → toolName = "math_calculator"
 * - TimeTool.kt → toolName = "time_utils"
 * 
 * 配置文件示例：
 * ```json
 * {
 *   "tools": ["math_calculator", "time_utils"]
 * }
 * ```
 * 
 * 【可配置文件加载的工具】
 * 只有无依赖、可以通过无参构造函数创建的工具才能通过配置文件加载：
 * - math_calculator (MathTool)
 * - time_utils (TimeTool)
 * 
 * 【需要工具工厂的工具】
 * 需要依赖注入或构造函数参数的工具必须通过工具工厂（toolFactory）创建：
 * - navigate_to_screen (NavigationTool) - 需要 getCurrentStudent 和 navigateTo 参数
 *   在创建 Agent 时，通过 toolFactory 参数提供创建函数即可从配置文件加载
 */

// 基础工具类和管理器
// BaseTool 和 ToolManager 在 BaseTool.kt 中定义

// 具体工具实现
// MathTool 在 MathTool.kt 中定义，toolName = "math_calculator"
// TimeTool 在 TimeTool.kt 中定义，toolName = "time_utils"
// NavigationTool 在 NavigationTool.kt 中定义，toolName = "navigate_to_screen"（需要依赖，不能通过配置加载）

/**
 * 获取所有内置工具（无依赖的工具）
 */
fun getAllBuiltinTools(): List<BaseTool> {
    return listOf(
        MathTool(),
        TimeTool(),
        KnowledgeBaseTool(),
        FinishTool(),
        QuestionRetrievalTool(),
        GradingTool()
    )
}

/**
 * 根据工具名称（toolName）获取工具实例
 * 
 * 注意：只能创建无依赖的工具。需要依赖的工具（如 NavigationTool）必须通过工具工厂创建。
 * 
 * 对于需要依赖的工具，应在 Agent 初始化时提供 toolFactory 函数来创建。
 * 
 * @param name 工具名称（工具类的 toolName 参数值），不是类名或文件名
 * @return 工具实例，如果工具不存在或需要依赖则返回 null
 */
fun getToolByName(name: String): BaseTool? {
    return when (name) {
        "math_calculator" -> MathTool()
        "time_utils" -> TimeTool()
        // navigate_to_screen (NavigationTool) 需要依赖注入，不能通过此方法创建
        // 需要通过工具工厂（toolFactory）创建，提供 getCurrentStudent 和 navigateTo 参数
        else -> null
    }
}

/**
 * 获取所有可以通过配置文件加载的工具名称列表
 * 
 * 返回所有可以通过 getToolByName() 创建的工具名称
 */
fun getAllToolNames(): List<String> {
    return listOf(
        "math_calculator",
        "time_utils"
    )
}
