package com.aiteacher.ai.tool

/**
 * 数学计算工具
 * 提供基本的数学运算功能
 */
class MathTool : BaseTool(
    toolName = "math_calculator",
    toolDescription = "执行基本数学计算",
    parameters = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "operation" to mapOf(
                "type" to "string",
                "description" to "数学运算类型",
                "enum" to listOf("add", "subtract", "multiply", "divide")
            ),
            "a" to mapOf(
                "type" to "number",
                "description" to "第一个数字"
            ),
            "b" to mapOf(
                "type" to "number", 
                "description" to "第二个数字"
            )
        ),
        "required" to listOf("operation", "a", "b")
    )
) {
    override suspend fun toolFunction(vararg args: Any): ToolResult {
        val operation = args.getOrNull(0) as? String ?: "add"
        val a = (args.getOrNull(1) as? Number)?.toDouble() ?: 0.0
        val b = (args.getOrNull(2) as? Number)?.toDouble() ?: 0.0
        
        val result = when (operation) {
            "add" -> a + b
            "subtract" -> a - b
            "multiply" -> a * b
            "divide" -> if (b != 0.0) a / b else throw IllegalArgumentException("Division by zero")
            else -> throw IllegalArgumentException("Unknown operation: $operation")
        }
        
        // 数学计算结果是查询类，需要LLM解释
        return ToolResult.QueryResult(result)
    }
}
