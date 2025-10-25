package com.aiteacher.ai.tool

/**
 * 工具索引文件
 * 提供所有工具的便捷导入
 */

// 基础工具类和管理器
// BaseTool 和 ToolManager 在 BaseTool.kt 中定义

// 具体工具实现
// MathTool 在 MathTool.kt 中定义
// StringTool 在 StringTool.kt 中定义  
// TimeTool 在 TimeTool.kt 中定义

/**
 * 获取所有内置工具
 */
fun getAllBuiltinTools(): List<BaseTool> {
    return listOf(
        MathTool(),
        StringTool(),
        TimeTool()
    )
}

/**
 * 根据名称获取工具实例
 */
fun getToolByName(name: String): BaseTool? {
    return when (name) {
        "math_calculator" -> MathTool()
        "string_processor" -> StringTool()
        "time_utils" -> TimeTool()
        else -> null
    }
}

/**
 * 获取所有工具名称
 */
fun getAllToolNames(): List<String> {
    return listOf(
        "math_calculator",
        "string_processor", 
        "time_utils"
    )
}
