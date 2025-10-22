package com.aiteacher.ai.service

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * LLM模型类，用于调用大语言模型API
 * 支持工具调用功能
 * 基于OpenAI Kotlin SDK 4.0.1
 */
class LLMModel(
    private val modelName: String = "qwen-plus",
    private val temperature: Float = 0.7f,
    private val topP: Float = 0.9f,
    private val maxTokens: Int = 1000
) {
    private val client: OpenAI by lazy {
        val apiKey = System.getenv("DASHSCOPE_API_KEY")
        if (apiKey.isNullOrEmpty()) {
            throw IllegalStateException("DASHSCOPE_API_KEY environment variable not set")
        }
        
        OpenAI(
            token = apiKey
        )
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
     */
    suspend fun generateText(messages: List<Map<String, String>>): LLMOutput? {
        return withContext(Dispatchers.IO) {
            try {
                // 转换消息格式
                val chatMessages = messages.map { msg ->
                    val role = when (msg["role"]) {
                        "user" -> ChatRole.User
                        "assistant" -> ChatRole.Assistant
                        "system" -> ChatRole.System
                        "tool" -> ChatRole.Tool
                        else -> ChatRole.User
                    }
                    ChatMessage(
                        role = role,
                        content = msg["content"] ?: ""
                    )
                }
                
                // 构建请求
                val request = ChatCompletionRequest(
                    model = ModelId(modelName),
                    messages = chatMessages,
                    temperature = temperature.toDouble(),
                    topP = topP.toDouble(),
                    maxTokens = maxTokens
                )
                
                // 调用API
                val response = client.chatCompletion(request)
                
                // 提取响应内容
                val content = response.choices.first().message.content ?: ""
                
                LLMOutput(
                    content = content,
                    model = modelName,
                    usage = null
                )
            } catch (e: Exception) {
                println("Error generating text: ${e.message}")
                null
            }
        }
    }
    
    /**
     * 解析工具调用
     */
    fun parseToolCall(output: LLMOutput?): List<Map<String, Any>>? {
        if (output == null) return null
        
        try {
            // 简化实现，返回空列表
            // 在实际应用中，这里需要解析工具调用
            return emptyList()
        } catch (e: Exception) {
            println("Error parsing tool calls: ${e.message}")
            return null
        }
    }
}