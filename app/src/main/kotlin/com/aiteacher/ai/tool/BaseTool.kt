package com.aiteacher.ai.tool

import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/**
 * 基础工具类
 * 仿照 Python 版本的 base_tool 实现
 * 支持工具规格定义和转换
 */
@Serializable
data class ToolSpec(
    val type: String = "function",
    val function: ToolFunction
)

@Serializable
data class ToolFunction(
    val name: String,
    val description: String,
    val parameters: Map<String, @Contextual Any>
)

/**
 * 工具结果类型
 * 用于区分查询类工具和执行类工具
 */
sealed class ToolResult {
    /**
     * 查询类结果 - 需要LLM进一步处理和解释
     * 例如：获取天气数据、查询信息等
     */
    data class QueryResult(val data: Any) : ToolResult() {
        override fun toString(): String = data.toString()
    }
    
    /**
     * 执行类结果 - 操作已执行或执行失败
     * 例如：导航跳转、文件操作等
     * 
     * @param message 结果消息（成功或失败的描述）
     * @param success 是否成功执行，true 表示成功，false 表示失败
     *                成功时：可以直接返回给用户，Agent 结束循环
     *                失败时：需要继续 LLM 循环，让 LLM 处理错误并告知用户
     */
    data class ExecuteResult(
        val message: String,
        val success: Boolean = true
    ) : ToolResult() {
        override fun toString(): String = message
        
        companion object {
            /**
             * 成功执行的快捷方法
             */
            fun success(message: String) = ExecuteResult(message, success = true)
            
            /**
             * 执行失败的快捷方法
             */
            fun failure(message: String) = ExecuteResult(message, success = false)
        }
    }
}

/**
 * 基础工具抽象类
 * 
 * 每个工具必须提供一个符合 DashScope API 标准的 JSON Schema 结构，用于描述工具的使用方式。
 * 这个结构会被传递给 LLM，让模型知道如何调用该工具。
 * 
 * **工具 JSON Schema 结构要求：**
 * 
 * ```json
 * {
 *   "type": "function",
 *   "function": {
 *     "name": "工具名称",
 *     "description": "工具描述，说明工具的用途",
 *     "parameters": {
 *       "type": "object",
 *       "properties": {
 *         "参数名1": {
 *           "type": "string|number|boolean|array|object",
 *           "description": "参数说明",
 *           "enum": ["可选值1", "可选值2"]  // 可选：枚举值
 *         },
 *         "参数名2": {
 *           "type": "string",
 *           "description": "参数说明"
 *         }
 *       },
 *       "required": ["参数名1"]  // 必需参数列表
 *     }
 *   }
 * }
 * ```
 * 
 * **parameters 字段说明：**
 * - `parameters` 必须是一个完整的 JSON Schema 对象
 * - 必须包含 `type: "object"`
 * - `properties` 定义每个参数的类型、描述等信息
 * - `required` 数组列出所有必需参数
 * 
 * **实现示例：**
 * ```kotlin
 * class WeatherTool : BaseTool(
 *     toolName = "get_weather",
 *     toolDescription = "获取指定城市的天气信息",
 *     parameters = mapOf(
 *         "type" to "object",
 *         "properties" to mapOf(
 *             "city" to mapOf(
 *                 "type" to "string",
 *                 "description" to "城市名称，例如：北京、上海"
 *             ),
 *             "date" to mapOf(
 *                 "type" to "string",
 *                 "description" to "日期，格式：YYYY-MM-DD，可选"
 *             ),
 *             "unit" to mapOf(
 *                 "type" to "string",
 *                 "enum" to listOf("celsius", "fahrenheit"),
 *                 "description" to "温度单位，默认为celsius"
 *             )
 *         ),
 *         "required" to listOf("city")
 *     )
 * ) {
 *     override suspend fun toolFunction(vararg args: Any): ToolResult {
 *         // 实现工具逻辑
 *         val params = args[0] as? Map<String, Any> ?: emptyMap()
 *         val city = params["city"] as? String ?: ""
 *         // ... 执行工具逻辑
 *         return ToolResult.QueryResult("天气信息")
 *     }
 * }
 * ```
 */
abstract class BaseTool(
    val toolName: String,
    val toolDescription: String,
    /**
     * 工具的 JSON Schema 参数定义
     * 
     * 必须是一个完整的 JSON Schema 对象，格式如下：
     * ```kotlin
     * mapOf(
     *     "type" to "object",
     *     "properties" to mapOf(
     *         "参数名" to mapOf(
     *             "type" to "string",
     *             "description" to "参数说明"
     *         )
     *     ),
     *     "required" to listOf("参数名")  // 必需参数
     * )
     * ```
     */
    val parameters: Map<String, Any> = emptyMap(),
    val toolType: String = "function"
) {
    
    // 工具输出结果
    var toolOutput: Any? = null
    
    /**
     * 工具执行函数 - 子类必须实现
     * 
     * @param args 工具参数数组，第一个元素通常是 Map<String, Any>，包含工具调用的参数
     * @return ToolResult - QueryResult 表示需要LLM进一步处理，ExecuteResult 表示操作已完成
     * 
     * 为了向后兼容，如果返回 String 或其他类型，BaseAgent 会自动包装为 QueryResult
     */
    abstract suspend fun toolFunction(vararg args: Any): ToolResult
    
    /**
     * 将工具转换为工具规格（符合 DashScope API 格式）
     * 
     * 返回的格式会被传递给 LLM，可以直接用于 DashScope API 的 tools 参数。
     * 
     * 生成的 JSON 结构：
     * ```json
     * {
     *   "type": "function",
     *   "function": {
     *     "name": "工具名称",
     *     "description": "工具描述",
     *     "parameters": {
     *       "type": "object",
     *       "properties": {...},
     *       "required": [...]
     *     }
     *   }
     * }
     * ```
     * 
     * @return Map<String, Any> 工具规格，可直接用于 DashScope API 的 tools 参数
     */
    fun toToolSpec(): Map<String, Any> {
        return mapOf(
            "type" to toolType,
            "function" to mapOf(
                "name" to toolName,
                "description" to toolDescription,
                "parameters" to parameters
            )
        )
    }
    
    /**
     * 获取工具的完整 JSON Schema 结构（可直接使用的格式）
     * 
     * 这个方法返回与 toToolSpec() 相同的结果，但名称更明确，表示这是一个可以直接使用的 JSON 结构体。
     * 
     * @return Map<String, Any> 完整的工具 JSON Schema，可直接传递给 LLM
     */
    fun getToolSchema(): Map<String, Any> = toToolSpec()
    
    /**
     * 验证工具结构是否正确
     * 
     * 检查工具定义是否符合 DashScope API 要求：
     * 1. parameters 必须包含 type: "object"
     * 2. parameters 必须包含 properties
     * 3. 如果定义了 required，必须是数组
     * 
     * @return Pair<Boolean, String> 第一个值表示是否有效，第二个值是错误信息（如果无效）
     */
    fun validateToolSchema(): Pair<Boolean, String> {
        // 检查基本字段
        if (toolName.isBlank()) {
            return false to "工具名称不能为空"
        }
        
        if (toolDescription.isBlank()) {
            return false to "工具描述不能为空"
        }
        
        // 检查 parameters 结构
        if (parameters.isEmpty()) {
            return true to "工具没有参数（这是允许的）"
        }
        
        val paramType = parameters["type"] as? String
        if (paramType != "object") {
            return false to "parameters.type 必须是 'object'，当前值为: $paramType"
        }
        
        val properties = parameters["properties"] as? Map<*, *>
        if (properties == null) {
            return false to "parameters 必须包含 'properties' 字段"
        }
        
        // 检查 required 字段（如果存在）
        val required = parameters["required"]
        if (required != null && required !is List<*>) {
            return false to "parameters.required 必须是数组类型"
        }
        
        return true to "工具结构有效"
    }
    
    /**
     * 从规格创建工具实例
     * 对应 Python 版本的 from_spec() 类方法
     */
    companion object {
        @Suppress("UNUSED_PARAMETER")
        fun fromSpec(
            spec: Map<String, Any>,
            toolFunction: suspend (Array<Any>) -> Any
        ): BaseTool {
            @Suppress("UNCHECKED_CAST")
            val functionBlock = (spec["function"] as? Map<*, *>) as? Map<String, Any> ?: emptyMap()
            val name = functionBlock["name"] as? String ?: ""
            val description = functionBlock["description"] as? String ?: ""
            @Suppress("UNCHECKED_CAST")
            val parameters = (functionBlock["parameters"] as? Map<*, *>) as? Map<String, Any> ?: emptyMap()
            val type = spec["type"] as? String ?: "function"
            
            return object : BaseTool(name, description, parameters, type) {
                override suspend fun toolFunction(vararg args: Any): ToolResult {
                    val result = toolFunction(args)
                    // toolFunction 参数类型是 (Array<Any>) -> Any，返回的可能是 ToolResult 或其他类型
                    // 如果已经是 ToolResult，直接返回；否则包装为 QueryResult
                    @Suppress("USELESS_IS_CHECK")
                    return if (result is ToolResult) {
                        result
                    } else {
                        ToolResult.QueryResult(result)
                    }
                }
            }
        }
    }
}


/**
 * 工具管理器
 */
class ToolManager {
    private val tools = mutableMapOf<String, BaseTool>()
    
    /**
     * 注册工具
     */
    fun registerTool(tool: BaseTool) {
        tools[tool.toolName] = tool
    }
    
    /**
     * 获取工具
     */
    fun getTool(toolName: String): BaseTool? {
        return tools[toolName]
    }
    
    /**
     * 获取所有工具
     */
    fun getAllTools(): List<BaseTool> {
        return tools.values.toList()
    }
    
    /**
     * 获取所有工具规格
     */
    fun getAllToolSpecs(): List<Map<String, Any>> {
        return tools.values.map { it.toToolSpec() }
    }
    
    /**
     * 调用工具
     */
    suspend fun callTool(toolName: String, vararg args: Any): ToolResult? {
        val tool = getTool(toolName)
        return if (tool != null) {
            try {
                val result = tool.toolFunction(*args)
                tool.toolOutput = result
                result
            } catch (e: Exception) {
                val errorResult = ToolResult.ExecuteResult("Error: ${e.message}")
                tool.toolOutput = errorResult
                errorResult
            }
        } else {
            throw IllegalArgumentException("Tool not found: $toolName")
        }
    }
    
    /**
     * 检查工具是否存在
     */
    fun hasTool(toolName: String): Boolean {
        return tools.containsKey(toolName)
    }
    
    /**
     * 获取工具列表
     */
    fun getToolNames(): List<String> {
        return tools.keys.toList()
    }
}
