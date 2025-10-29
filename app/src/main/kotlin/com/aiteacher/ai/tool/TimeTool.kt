package com.aiteacher.ai.tool

/**
 * 时间工具
 * 提供时间相关操作功能
 */
class TimeTool : BaseTool(
    toolName = "time_utils",
    toolDescription = "时间相关操作",
    parameters = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "operation" to mapOf(
                "type" to "string",
                "description" to "时间操作类型",
                "enum" to listOf("current_time", "format_time", "parse_time")
            ),
            "format" to mapOf(
                "type" to "string",
                "description" to "时间格式（可选）"
            )
        ),
        "required" to listOf("operation")
    )
) {
    override suspend fun toolFunction(vararg args: Any): Any {
        val operation = args.getOrNull(0) as? String ?: "current_time"
        val format = args.getOrNull(1) as? String ?: "yyyy-MM-dd HH:mm:ss"
        
        return when (operation) {
            "current_time" -> {
                val now = java.time.LocalDateTime.now()
                now.toString()
            }
            "format_time" -> {
                val now = java.time.LocalDateTime.now()
                now.format(java.time.format.DateTimeFormatter.ofPattern(format))
            }
            "parse_time" -> {
                // 简单的时间解析示例
                "Time parsing not implemented yet"
            }
            else -> throw IllegalArgumentException("Unknown operation: $operation")
        }
    }
}
