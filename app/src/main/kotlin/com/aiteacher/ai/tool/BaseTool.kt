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
 */
abstract class BaseTool(
    val toolName: String,
    val toolDescription: String,
    val parameters: Map<String, Any> = emptyMap(),
    val toolType: String = "function"
) {
    
    // 工具输出结果
    var toolOutput: Any? = null
    
    /**
     * 工具执行函数 - 子类必须实现
     * 返回 ToolResult，可以是 QueryResult（需要LLM处理）或 ExecuteResult（直接返回）
     * 
     * 为了向后兼容，如果返回 String 或其他类型，BaseAgent 会自动包装为 QueryResult
     */
    abstract suspend fun toolFunction(vararg args: Any): ToolResult
    
    /**
     * 将工具转换为工具规格
     * 对应 Python 版本的 to_tool_spec() 方法
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
                    return when (result) {
                        is ToolResult -> result
                        else -> ToolResult.QueryResult(result)
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
