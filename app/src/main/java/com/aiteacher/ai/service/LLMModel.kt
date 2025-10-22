package com.aiteacher.ai.service

import com.alibaba.dashscope.aigc.generation.Generation
import com.alibaba.dashscope.aigc.generation.GenerationParam
import com.alibaba.dashscope.aigc.generation.GenerationResult
import com.alibaba.dashscope.common.Message
import com.alibaba.dashscope.common.Role
import com.alibaba.dashscope.exception.ApiException
import com.alibaba.dashscope.exception.InputRequiredException
import com.alibaba.dashscope.exception.NoApiKeyException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Arrays

/**
 * LLM输出数据类
 */
data class LLMOutput(
    val content: String,
    val model: String,
    val usage: Map<String, Any>?
)

/**
 * LLM模型类，用于调用阿里云DashScope API
 * 支持工具调用功能
 * 基于阿里云DashScope SDK 2.21.12
 */
class LLMModel(
    private val modelName: String = "qwen-plus",
    private val temperature: Float = 0.7f,
    private val topP: Float = 0.9f,
    private val maxTokens: Int = 1000
) {
    private val generation: Generation by lazy {
        Generation()
    }
    
    private val tools = mutableListOf<Map<String, Any>>()
    
    /**
     * 添加工具
     */
    fun addTool(toolSpec: Map<String, Any>) {
        tools.add(toolSpec)
    }
    
    /**
     * 生成文本
     * 支持MCP工具调用功能
     */
    suspend fun generateText(messages: List<Map<String, String>>): LLMOutput? {
        return withContext(Dispatchers.IO) {
            try {
                // 优先使用gradle.properties中的配置，然后尝试环境变量
                val apiKey = System.getProperty("DASHSCOPE_API_KEY") 
                    ?: System.getenv("DASHSCOPE_API_KEY")
                    ?: "sk-29c2aa790a93483d80e43121151e5210" // 直接使用配置的API key
                
                if (apiKey.isNullOrEmpty()) {
                    throw IllegalStateException("DASHSCOPE_API_KEY not configured")
                }
                
                // 转换消息格式为DashScope格式
                val dashScopeMessages = messages.map { msg ->
                    val role = when (msg["role"]) {
                        "user" -> Role.USER
                        "assistant" -> Role.ASSISTANT
                        "system" -> Role.SYSTEM
                        else -> Role.USER
                    }
                    Message.builder()
                        .role(role.getValue())
                        .content(msg["content"] ?: "")
                        .build()
                }
                
                // 构建请求参数
                val param = GenerationParam.builder()
                    .apiKey(apiKey)
                    .model(modelName)
                    .messages(dashScopeMessages)
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .temperature(temperature)
                    .topP(topP.toDouble())
                    .maxTokens(maxTokens)
                    .build()
                
                // 调用API
                val result = generation.call(param)
                
                // 提取响应内容
                val content = result.output.choices.first().message.content
                
                LLMOutput(
                    content = content,
                    model = modelName,
                    usage = mapOf(
                        "input_tokens" to (result.usage?.inputTokens ?: 0),
                        "output_tokens" to (result.usage?.outputTokens ?: 0),
                        "total_tokens" to (result.usage?.totalTokens ?: 0)
                    )
                )
            } catch (e: ApiException) {
                println("DashScope API Error: ${e.message}")
                null
            } catch (e: NoApiKeyException) {
                println("No API Key Error: ${e.message}")
                null
            } catch (e: InputRequiredException) {
                println("Input Required Error: ${e.message}")
                null
            } catch (e: Exception) {
                println("Error generating text: ${e.message}")
                null
            }
        }
    }
    
    /**
     * 解析工具调用
     * qwen模型支持MCP功能，可以解析工具调用
     */
    fun parseToolCall(output: LLMOutput?): List<Map<String, Any>>? {
        if (output == null) return null
        
        try {
            val content = output.content
            
            // 检查是否包含工具调用标记
            if (content.contains("```json") && content.contains("tool_call")) {
                // 解析JSON格式的工具调用
                val jsonStart = content.indexOf("```json") + 7
                val jsonEnd = content.indexOf("```", jsonStart)
                
                if (jsonStart > 6 && jsonEnd > jsonStart) {
                    val jsonContent = content.substring(jsonStart, jsonEnd).trim()
                    
                    // 简单的JSON解析（实际项目中建议使用专门的JSON库）
                    if (jsonContent.contains("\"tool_call\"")) {
                        return listOf(
                            mapOf(
                                "type" to "tool_call",
                                "content" to jsonContent
                            )
                        )
                    }
                }
            }
            
            // 检查是否包含函数调用标记
            if (content.contains("function_call") || content.contains("tool_use")) {
                return listOf(
                    mapOf(
                        "type" to "function_call",
                        "content" to content
                    )
                )
            }
            
            return emptyList()
        } catch (e: Exception) {
            println("Error parsing tool calls: ${e.message}")
            return null
        }
    }
    
    /**
     * 获取可用工具列表
     */
    fun getAvailableTools(): List<Map<String, Any>> {
        return tools.toList()
    }
    
    /**
     * 清除所有工具
     */
    fun clearTools() {
        tools.clear()
    }
    
    /**
     * 生成支持MCP工具调用的文本
     * 为qwen模型添加工具调用提示
     */
    suspend fun generateTextWithTools(messages: List<Map<String, String>>): LLMOutput? {
        // 如果有工具，添加工具调用提示
        val enhancedMessages = if (tools.isNotEmpty()) {
            val toolPrompt = buildString {
                appendLine("你是一个AI助手，可以使用以下工具：")
                tools.forEach { tool ->
                    val name = tool["name"] as? String ?: "unknown"
                    val description = tool["description"] as? String ?: ""
                    appendLine("- $name: $description")
                }
                appendLine("\n当需要使用工具时，请以以下格式回复：")
                appendLine("```json")
                appendLine("{")
                appendLine("  \"tool_call\": {")
                appendLine("    \"name\": \"工具名称\",")
                appendLine("    \"arguments\": {")
                appendLine("      \"参数名\": \"参数值\"")
                appendLine("    }")
                appendLine("  }")
                appendLine("}")
                appendLine("```")
            }
            
            messages + mapOf("role" to "system", "content" to toolPrompt)
        } else {
            messages
        }
        
        return generateText(enhancedMessages)
    }
}