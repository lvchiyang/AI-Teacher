package com.aiteacher.ai.service

import com.openai.client.OpenAIClient
import com.openai.client.okhttp.OpenAIOkHttpClient
import com.openai.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * LLM响应对象
 * 完全对应Python版本的模型输出格式
 */
data class LLMOutput(
    val content: String,
    val toolCalls: List<Map<String, Any>>? = null
)

/**
 * LLM模型类
 * 完全对应Python版本的llm_model类
 */
class LLMModel(
    modelName: String = "qwen-max",
    kwargs: Map<String, Any> = emptyMap()
) {
    
    private val modelName: String = modelName
    private val temperature: Float = (kwargs["temperature"] as? Number)?.toFloat() ?: 0.7f
    private val topK: Int = (kwargs["top_k"] as? Number)?.toInt() ?: 50
    private val topP: Float = (kwargs["top_p"] as? Number)?.toFloat() ?: 1.0f
    private val responseFormat: Map<String, String> = (kwargs["response_format"] as? Map<String, String>) ?: mapOf("type" to "text")
    private val maxTokens: Int = (kwargs["max_tokens"] as? Number)?.toInt() ?: 1024
    private val maxInputTokens: Int = (kwargs["max_input_tokens"] as? Number)?.toInt() ?: 1024
    private val enableThinking: Boolean = (kwargs["enable_thinking"] as? Boolean) ?: false
    private val initialTools: List<Map<String, Any>> = (kwargs["tools"] as? List<Map<String, Any>>) ?: emptyList()
    
    private val tools: MutableList<Map<String, Any>> = initialTools.toMutableList()
    private val client: OpenAIClient by lazy {
        val apiKey = System.getenv("DASHSCOPE_API_KEY")
        if (apiKey.isNullOrEmpty()) {
            throw IllegalStateException("DASHSCOPE_API_KEY environment variable not set")
        }
        
        OpenAIOkHttpClient.builder()
            .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
            .apiKey(apiKey)
            .build()
    }
    
    /**
     * 使用实例配置调用Qwen API生成文本
     * 完全对应Python版本的generate_text方法
     * 返回LLMOutput，与Python版本一致
     */
    suspend fun generateText(prompt: List<Map<String, String>>): LLMOutput? {
        return withContext(Dispatchers.IO) {
            try {
                // 转换消息格式
                val messages = prompt.map { msg ->
                    val role = when (msg["role"]) {
                        "user" -> ChatCompletionRole.USER
                        "assistant" -> ChatCompletionRole.ASSISTANT
                        "system" -> ChatCompletionRole.SYSTEM
                        "tool" -> ChatCompletionRole.TOOL
                        else -> ChatCompletionRole.USER
                    }
                    ChatCompletionMessage.builder()
                        .role(role)
                        .content(msg["content"] ?: "")
                        .build()
                }
                
                // 构建请求参数
                val paramsBuilder = ChatCompletionCreateParams.builder()
                    .model(modelName)
                    .messages(messages)
                    .temperature(temperature.toDouble())
                    .topP(topP.toDouble())
                    .maxTokens(maxTokens)
                    .stream(false)
                
                // 添加DashScope特有的参数
                // 注意：OpenAI SDK可能不支持这些参数，需要检查实际支持情况
                try {
                    // 尝试添加top_k参数（如果SDK支持）
                    // paramsBuilder.putExtraBody("top_k", topK)
                } catch (e: Exception) {
                    // SDK不支持extraBody，忽略top_k参数
                    println("Warning: top_k parameter not supported by current SDK")
                }
                
                // 添加工具支持 - 完全对应Python版本的tools参数
                if (tools.isNotEmpty()) {
                    val openAITools = tools.map { toolSpec ->
                        ChatCompletionTool.builder()
                            .type(ChatCompletionTool.Type.FUNCTION)
                            .function(
                                ChatCompletionFunction.builder()
                                    .name(toolSpec["name"] as? String ?: "")
                                    .description(toolSpec["description"] as? String ?: "")
                                    .parameters(toolSpec["parameters"] as? Map<String, Any> ?: emptyMap())
                                    .build()
                            )
                            .build()
                    }
                    paramsBuilder.tools(openAITools)
                }
                
                val completion = client.chat().completions().create(
                    paramsBuilder.build()
                )
                
                // 解析响应
                val content = completion.choices().first().message().content().orElse("")
                val toolCalls = parseToolCallsFromCompletion(completion)
                
                // 返回LLMOutput对象，与Python版本一致
                LLMOutput(content = content, toolCalls = toolCalls)
                
            } catch (e: Exception) {
                println("Error calling Qwen API: ${e.message}")
                null
            }
        }
    }
    
    /**
     * 尝试从模型输出解析工具调用请求
     * 完全对应Python版本的parse_tool_call方法
     * 返回List<Map<String, Any>>?，与Python版本一致
     */
    fun parseToolCall(modelOutput: Any?): List<Map<String, Any>>? {
        // 处理model_output为None的情况
        if (modelOutput == null) {
            return null
        }
        
        // 检查是否是LLMOutput对象
        if (modelOutput !is LLMOutput) {
            return null
        }
        
        return modelOutput.toolCalls
    }
    
    /**
     * 添加工具到模型中
     * 完全对应Python版本的add_tool方法
     */
    fun addTool(tool: Map<String, Any>) {
        tools.add(tool)
    }
    
    /**
     * 从ChatCompletion解析工具调用
     * 完全对应Python版本的parse_tool_call方法，包含JSON解析
     */
    private fun parseToolCallsFromCompletion(completion: ChatCompletion): List<Map<String, Any>>? {
        val toolCalls = mutableListOf<Map<String, Any>>()
        
        try {
            val choice = completion.choices()[0]
            val message = choice.message()
            val toolCallsFromMessage = message.toolCalls()
            
            if (!toolCallsFromMessage.isNullOrEmpty()) {
                for (toolCall in toolCallsFromMessage) {
                    val function = toolCall.function()
                    val name = function.name() ?: ""
                    val argumentsStr = function.arguments() ?: "{}"
                    
                    // 解析JSON字符串为Map，完全对应Python版本的行为
                    val arguments = try {
                        @Suppress("UNCHECKED_CAST")
                        Json.decodeFromString<Map<String, Any>>(argumentsStr)
                    } catch (e: Exception) {
                        // 如果JSON解析失败，返回空Map
                        emptyMap<String, Any>()
                    }
                    
                    toolCalls.add(mapOf(
                        "name" to name,
                        "arguments" to arguments
                    ))
                }
            }
        } catch (e: Exception) {
            println("Error parsing tool calls: ${e.message}")
            return null
        }
        
        return if (toolCalls.isNotEmpty()) toolCalls else null
    }
}
