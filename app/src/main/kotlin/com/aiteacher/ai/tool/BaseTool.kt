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
     */
    abstract suspend fun toolFunction(vararg args: Any): Any
    
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
        fun fromSpec(
            spec: Map<String, Any>,
            toolFunction: suspend (Array<Any>) -> Any
        ): BaseTool {
            val functionBlock = spec["function"] as? Map<String, Any> ?: emptyMap()
            val name = functionBlock["name"] as? String ?: ""
            val description = functionBlock["description"] as? String ?: ""
            val parameters = functionBlock["parameters"] as? Map<String, Any> ?: emptyMap()
            val type = spec["type"] as? String ?: "function"
            
            return object : BaseTool(name, description, parameters, type) {
                override suspend fun toolFunction(vararg args: Any): Any {
                    return toolFunction(args)
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
    suspend fun callTool(toolName: String, vararg args: Any): Any? {
        val tool = getTool(toolName)
        return if (tool != null) {
            try {
                val result = tool.toolFunction(*args)
                tool.toolOutput = result
                result
            } catch (e: Exception) {
                tool.toolOutput = "Error: ${e.message}"
                throw e
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
