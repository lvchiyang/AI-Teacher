package com.aiteacher.ai.tool

/**
 * 字符串处理工具
 * 提供字符串操作功能
 */
class StringTool : BaseTool(
    toolName = "string_processor",
    toolDescription = "处理字符串操作",
    parameters = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "operation" to mapOf(
                "type" to "string",
                "description" to "字符串操作类型",
                "enum" to listOf("uppercase", "lowercase", "reverse", "length")
            ),
            "text" to mapOf(
                "type" to "string",
                "description" to "要处理的文本"
            )
        ),
        "required" to listOf("operation", "text")
    )
) {
    override suspend fun toolFunction(vararg args: Any): Any {
        val operation = args.getOrNull(0) as? String ?: "uppercase"
        val text = args.getOrNull(1) as? String ?: ""
        
        return when (operation) {
            "uppercase" -> text.uppercase()
            "lowercase" -> text.lowercase()
            "reverse" -> text.reversed()
            "length" -> text.length
            else -> throw IllegalArgumentException("Unknown operation: $operation")
        }
    }
}
