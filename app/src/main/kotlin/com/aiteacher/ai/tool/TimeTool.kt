package com.aiteacher.ai.tool

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 时间处理工具
 * 提供时间相关的操作，如获取当前时间、格式化、计算时间差等
 * 
 * 工具 JSON Schema 结构：
 * ```json
 * {
 *   "type": "function",
 *   "function": {
 *     "name": "time_utils",
 *     "description": "处理时间相关操作，如获取当前时间、格式化、计算时间差等",
 *     "parameters": {
 *       "type": "object",
 *       "properties": {
 *         "operation": {
 *           "type": "string",
 *           "enum": ["current", "format", "parse", "add_days", "diff"],
 *           "description": "操作类型"
 *         },
 *         "format": {
 *           "type": "string",
 *           "description": "时间格式，例如：'yyyy-MM-dd HH:mm:ss'、'yyyy年MM月dd日'"
 *         },
 *         "time_string": {
 *           "type": "string",
 *           "description": "时间字符串（用于parse操作）"
 *         },
 *         "days": {
 *           "type": "integer",
 *           "description": "天数（用于add_days操作）"
 *         },
 *         "time1": {
 *           "type": "string",
 *           "description": "第一个时间字符串（用于diff操作）"
 *         },
 *         "time2": {
 *           "type": "string",
 *           "description": "第二个时间字符串（用于diff操作）"
 *         }
 *       },
 *       "required": ["operation"]
 *     }
 *   }
 * }
 * ```
 */
class TimeTool : BaseTool(
    toolName = "time_utils",
    toolDescription = "处理时间相关操作，如获取当前时间、格式化、解析时间字符串、计算时间差等",
    parameters = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "operation" to mapOf(
                "type" to "string",
                "enum" to listOf("current", "format", "parse", "add_days", "diff"),
                "description" to "操作类型：current(当前时间)、format(格式化)、parse(解析)、add_days(加天数)、diff(时间差)"
            ),
            "format" to mapOf(
                "type" to "string",
                "description" to "时间格式，例如：'yyyy-MM-dd HH:mm:ss'、'yyyy年MM月dd日'"
            ),
            "time_string" to mapOf(
                "type" to "string",
                "description" to "时间字符串（用于parse操作）"
            ),
            "days" to mapOf(
                "type" to "integer",
                "description" to "天数（用于add_days操作）"
            ),
            "time1" to mapOf(
                "type" to "string",
                "description" to "第一个时间字符串（用于diff操作）"
            ),
            "time2" to mapOf(
                "type" to "string",
                "description" to "第二个时间字符串（用于diff操作）"
            )
        ),
        "required" to listOf("operation")
    )
) {
    override suspend fun toolFunction(vararg args: Any): ToolResult {
        val params = when {
            args.isEmpty() -> emptyMap<String, Any>()
            args[0] is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                args[0] as Map<String, Any>
            }
            else -> emptyMap()
        }
        
        val operation = params["operation"] as? String
            ?: return ToolResult.QueryResult("错误：缺少operation参数")
        
        val result = when (operation) {
            "current" -> {
                val formatStr = params["format"] as? String ?: "yyyy-MM-dd HH:mm:ss"
                try {
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern(formatStr))
                } catch (e: Exception) {
                    "错误：时间格式无效 - ${e.message}"
                }
            }
            "format" -> {
                val timeStr = params["time_string"] as? String
                val formatStr = params["format"] as? String ?: "yyyy-MM-dd HH:mm:ss"
                if (timeStr == null) {
                    "错误：format操作需要time_string参数"
                } else {
                    try {
                        val time = LocalDateTime.parse(timeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        time.format(DateTimeFormatter.ofPattern(formatStr))
                    } catch (e: Exception) {
                        "错误：时间解析失败 - ${e.message}"
                    }
                }
            }
            "parse" -> {
                val timeStr = params["time_string"] as? String
                if (timeStr == null) {
                    "错误：parse操作需要time_string参数"
                } else {
                    try {
                        LocalDateTime.parse(timeStr).toString()
                    } catch (e: Exception) {
                        "错误：时间解析失败 - ${e.message}"
                    }
                }
            }
            "add_days" -> {
                val days = (params["days"] as? Number)?.toInt()
                if (days == null) {
                    "错误：add_days操作需要days参数"
                } else {
                    try {
                        LocalDateTime.now().plusDays(days.toLong())
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    } catch (e: Exception) {
                        "错误：计算失败 - ${e.message}"
                    }
                }
            }
            "diff" -> {
                val time1Str = params["time1"] as? String
                val time2Str = params["time2"] as? String
                if (time1Str == null || time2Str == null) {
                    "错误：diff操作需要time1和time2参数"
                } else {
                    try {
                        val time1 = LocalDateTime.parse(time1Str)
                        val time2 = LocalDateTime.parse(time2Str)
                        val diff = java.time.Duration.between(time1, time2)
                        "时间差：${diff.toHours()}小时 ${diff.toMinutes() % 60}分钟"
                    } catch (e: Exception) {
                        "错误：时间差计算失败 - ${e.message}"
                    }
                }
            }
            else -> "错误：未知的操作类型 '$operation'"
        }
        
        return ToolResult.QueryResult(result)
    }
}

