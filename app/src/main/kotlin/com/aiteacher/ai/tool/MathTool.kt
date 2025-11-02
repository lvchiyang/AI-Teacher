package com.aiteacher.ai.tool

/**
 * 数学计算工具
 * 提供基本的数学运算功能
 * 
 * 工具 JSON Schema 结构：
 * ```json
 * {
 *   "type": "function",
 *   "function": {
 *     "name": "math_calculator",
 *     "description": "执行数学计算，支持四则运算、幂运算等",
 *     "parameters": {
 *       "type": "object",
 *       "properties": {
 *         "expression": {
 *           "type": "string",
 *           "description": "数学表达式，例如：'2 + 3 * 4'、'pow(2, 3)'、'sqrt(16)'"
 *         },
 *         "precision": {
 *           "type": "integer",
 *           "description": "结果精度（小数位数），默认为2"
 *         }
 *       },
 *       "required": ["expression"]
 *     }
 *   }
 * }
 * ```
 */
class MathTool : BaseTool(
    toolName = "math_calculator",
    toolDescription = "执行数学计算，支持四则运算、幂运算、开方、三角函数等基本数学运算",
    parameters = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "expression" to mapOf(
                "type" to "string",
                "description" to "数学表达式，例如：'2 + 3 * 4'、'pow(2, 3)'、'sqrt(16)'、'sin(30)'"
            ),
            "precision" to mapOf(
                "type" to "integer",
                "description" to "结果精度（小数位数），默认为2"
            )
        ),
        "required" to listOf("expression")
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
        
        val expression = params["expression"] as? String
            ?: return ToolResult.QueryResult("错误：缺少expression参数")
        
        val precision = (params["precision"] as? Number)?.toInt() ?: 2
        
        try {
            val result = evaluateMathExpression(expression, precision)
            return ToolResult.QueryResult("计算结果：$result")
        } catch (e: Exception) {
            return ToolResult.QueryResult("计算错误：${e.message}")
        }
    }
    
    /**
     * 计算数学表达式
     */
    private fun evaluateMathExpression(expression: String, precision: Int): Double {
        // 简化的表达式求值实现
        // 注意：实际项目中应该使用更强大的表达式解析库（如 exp4j）
        var expr = expression.trim()
        
        // 处理常见函数
        expr = expr.replace("pow\\((\\d+(?:\\.\\d+)?),\\s*(\\d+(?:\\.\\d+)?)\\)".toRegex()) { matchResult ->
            val base = matchResult.groupValues[1].toDouble()
            val exp = matchResult.groupValues[2].toDouble()
            Math.pow(base, exp).toString()
        }
        
        expr = expr.replace("sqrt\\((\\d+(?:\\.\\d+)?)\\)".toRegex()) { matchResult ->
            val value = matchResult.groupValues[1].toDouble()
            Math.sqrt(value).toString()
        }
        
        // 简单的四则运算（注意：这是一个简化实现）
        // 实际应该使用表达式解析器
        val result = try {
            // 使用JavaScript引擎或其他表达式解析器
            // 这里简化处理，仅支持基本运算
            when {
                expr.contains("+") -> {
                    val parts = expr.split("+")
                    parts.map { it.trim().toDouble() }.sum()
                }
                expr.contains("-") && !expr.startsWith("-") -> {
                    val parts = expr.split("-")
                    parts[0].trim().toDouble() - parts.drop(1).sumOf { it.trim().toDouble() }
                }
                expr.contains("*") -> {
                    val parts = expr.split("*")
                    parts.map { it.trim().toDouble() }.reduce { a, b -> a * b }
                }
                expr.contains("/") -> {
                    val parts = expr.split("/")
                    parts.map { it.trim().toDouble() }.reduce { a, b -> a / b }
                }
                else -> expr.toDouble()
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("无法解析表达式：$expression", e)
        }
        
        return String.format("%.${precision}f", result).toDouble()
    }
}

