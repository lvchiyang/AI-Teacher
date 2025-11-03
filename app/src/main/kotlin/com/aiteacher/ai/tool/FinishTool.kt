package com.aiteacher.ai.tool

/**
 * 结束工具 - 用于结束对话或教学过程
 */
class FinishTool : BaseTool(
    toolName = "finish",
    toolDescription = "结束当前对话或教学过程，表示任务已完成",
    parameters = emptyMap()
) {
    
    override suspend fun toolFunction(vararg args: Any): ToolResult {
        return ToolResult.ExecuteResult.success("结束")
    }
}