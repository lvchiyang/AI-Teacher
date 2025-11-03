package com.aiteacher.ai.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import android.util.Log

/**
 * LLM输出数据类
 */
data class LLMOutput(
    val content: String,
    val model: String,
    val usage: Map<String, Any>?,
    val toolCalls: List<Map<String, Any>> = emptyList()
)

/**
 * Agent调用模型的请求体数据结构
 * 只包含Agent需要指定的参数，其他参数使用LLMModel的默认值
 */
data class AgentRequest(
    val messages: List<Map<String, String>>,
    val tools: List<Map<String, Any>> = emptyList(),
    val model: String? = null,  // 如果为null，使用LLMModel的默认modelName
    val temperature: Float? = null,  // 如果为null，使用LLMModel的默认temperature
    val topP: Float? = null,
    val topK: Int? = null,
    val maxTokens: Int? = null,
    val repetitionPenalty: Float? = null,
    val seed: Int? = null,
    val stop: List<String>? = null,
    val responseFormat: String? = null,
    val incrementalOutput: Boolean? = null,
    val enableSearch: Boolean? = null,
    val n: Int? = null,
    val toolChoice: Map<String, Any>? = null
)

/**
 * LLM模型类，用于调用阿里云DashScope API
 * 支持工具调用功能
 * 
 * 直接构建 JSON 请求体，使用 HTTP 客户端调用 API，不依赖 SDK 的 GenerationParam
 */
class LLMModel(
    // 默认请求参数（保存在LLMModel中）
    private val defaultModelName: String = "qwen-max",
    private val defaultTemperature: Float = 0.7f,
    private val defaultTopP: Float = 0.9f,
    private val defaultMaxTokens: Int = 2000,
    private val defaultTopK: Int? = null,
    private val defaultRepetitionPenalty: Float? = null,
    private val defaultSeed: Int? = null,
    private val defaultStop: List<String>? = null,
    private val defaultResponseFormat: String? = null,
    private val defaultIncrementalOutput: Boolean = false,
    private val defaultEnableSearch: Boolean = false,
    private val defaultN: Int = 1,
    private val defaultToolChoice: Map<String, Any>? = null
) {
    // DashScope API 端点
    private val apiUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions"
    
    // HTTP 客户端（使用 Ktor）
    private val httpClient = HttpClient(CIO)
    
    /**
     * 将任意值转换为 JsonElement（递归处理嵌套结构）
     */
    private fun valueToJsonElement(value: Any?): JsonElement {
        return when (value) {
            null -> JsonNull
            is String -> JsonPrimitive(value)
            is Number -> JsonPrimitive(value)
            is Boolean -> JsonPrimitive(value)
            is Map<*, *> -> {
                buildJsonObject {
                    // 安全的类型转换：先检查是否为 Map<String, Any>
                    val stringMap = value.entries.associate { entry ->
                        val key = entry.key?.toString() ?: ""
                        val mapValue = entry.value
                        key to mapValue
                    }
                    stringMap.forEach { (k, v) ->
                        put(k, valueToJsonElement(v))
                    }
                }
            }
            is List<*> -> {
                buildJsonArray {
                    value.forEach { item ->
                        add(valueToJsonElement(item))
                    }
                }
            }
            else -> JsonPrimitive(value.toString())
        }
    }
    
    /**
     * 生成文本（唯一接口，接受AgentRequest）
     * 将AgentRequest中的参数与LLMModel的默认参数合并，然后构建 JSON 请求体调用 DashScope API
     */
    suspend fun generateText(request: AgentRequest): LLMOutput? {
        return withContext(Dispatchers.IO) {
            try {
                // 合并请求参数：AgentRequest中的参数优先，如果为null则使用默认值
                val modelName = request.model ?: defaultModelName
                val temperature = request.temperature ?: defaultTemperature
                val topP = request.topP ?: defaultTopP
                val maxTokens = request.maxTokens ?: defaultMaxTokens
                val topK = request.topK ?: defaultTopK
                val repetitionPenalty = request.repetitionPenalty ?: defaultRepetitionPenalty
                val seed = request.seed ?: defaultSeed
                val stop = request.stop ?: defaultStop
                val responseFormat = request.responseFormat ?: defaultResponseFormat
                val incrementalOutput = request.incrementalOutput ?: defaultIncrementalOutput
                val enableSearch = request.enableSearch ?: defaultEnableSearch
                val n = request.n ?: defaultN
                val toolChoice = request.toolChoice ?: defaultToolChoice
                val tools = request.tools
                
                // 获取API Key
                val apiKey = System.getProperty("DASHSCOPE_API_KEY") 
                    ?: System.getenv("DASHSCOPE_API_KEY")
                    ?: "sk-29c2aa790a93483d80e43121151e5210"
                
                if (apiKey.isNullOrEmpty()) {
                    throw IllegalStateException("DASHSCOPE_API_KEY not configured")
                }
                
                // 构建请求体 JSON 对象（根据请求体文档）
                val requestBody = buildJsonObject {
                    put("model", modelName)
                    put("messages", buildJsonArray {
                        request.messages.forEach { msg ->
                            add(buildJsonObject {
                                put("role", msg["role"] ?: "user")
                                put("content", msg["content"] ?: "")
                            })
                        }
                    })
                    
                    // 可选参数：只在有值时添加
                    if (tools.isNotEmpty()) {
                        put("tools", buildJsonArray {
                            tools.forEach { tool ->
                                // 将 Map 转换为 JsonObject（递归处理嵌套结构）
                                add(buildJsonObject {
                                    tool.forEach { (key, value) ->
                                        put(key, valueToJsonElement(value))
                                    }
                                })
                            }
                        })
                    }
                    
                    if (toolChoice != null) {
                        put("tool_choice", buildJsonObject {
                            toolChoice.forEach { (key, value) ->
                                put(key, valueToJsonElement(value))
                            }
                        })
                    }
                    
                    put("temperature", temperature)
                    put("top_p", topP)
                    
                    topK?.let { put("top_k", it) }
                    put("max_tokens", maxTokens)
                    repetitionPenalty?.let { put("repetition_penalty", it) }
                    seed?.let { put("seed", it) }
                    stop?.takeIf { it.isNotEmpty() }?.let { 
                        put("stop", buildJsonArray { 
                            it.forEach { stopStr -> add(stopStr) }
                        })
                    }
                    put("result_format", "message")
                    responseFormat?.let { put("response_format", it) }
                    put("incremental_output", incrementalOutput)
                    put("stream", false)
                    put("enable_search", enableSearch)
                    if (n > 1) put("n", n)
                }
                
                // 将 JsonObject 转换为 JSON 字符串
                val json = Json { ignoreUnknownKeys = true; isLenient = true }
                val requestBodyJson = json.encodeToString(JsonElement.serializer(), requestBody)
                
                // 打印请求信息
                Log.d("LLMModel", "========== 发送请求 ==========")
                Log.d("LLMModel", "URL: $apiUrl")
                Log.d("LLMModel", "请求体: $requestBodyJson")
                
                // 发送 HTTP 请求
                val response = httpClient.post(apiUrl) {
                    headers {
                        append("Authorization", "Bearer $apiKey")
                        append(HttpHeaders.ContentType, "application/json")
                    }
                    setBody(requestBodyJson)
                }
                
                // 解析响应
                val responseText = response.body<String>()
                
                // 打印原始响应
                Log.d("LLMModel", "========== 收到响应 ==========")
                Log.d("LLMModel", "HTTP 状态码: ${response.status.value}")
                Log.d("LLMModel", "原始响应文本: $responseText")
                
                val responseJson = json.parseToJsonElement(responseText) as JsonObject
                Log.d("LLMModel", "解析后的 JSON 对象键: ${responseJson.keys.joinToString()}")
                
                // 检查是否有错误
                if (response.status.value >= 400) {
                    val errorMessage = responseJson["message"]?.jsonPrimitive?.content 
                        ?: responseJson["error"]?.jsonObject?.get("message")?.jsonPrimitive?.content
                        ?: "API调用失败: HTTP ${response.status.value}"
                    Log.e("LLMModel", "API 错误: $errorMessage")
                    throw Exception(errorMessage)
                }
                
                // 提取输出（兼容两种响应格式）
                // 格式1: DashScope 原生格式 {"output": {"choices": [...]}}
                // 格式2: OpenAI 兼容格式 {"choices": [...]}
                val choices: JsonArray = if (responseJson.containsKey("output")) {
                    // DashScope 原生格式
                    val output = responseJson["output"] as? JsonObject
                        ?: throw Exception("响应格式错误：缺少 output 字段")
                    val choicesArray = output["choices"] as? JsonArray
                        ?: throw Exception("响应格式错误：缺少 choices 字段")
                    Log.d("LLMModel", "检测到 DashScope 原生格式")
                    choicesArray
                } else if (responseJson.containsKey("choices")) {
                    // OpenAI 兼容格式
                    val choicesArray = responseJson["choices"] as? JsonArray
                        ?: throw Exception("响应格式错误：缺少 choices 字段")
                    Log.d("LLMModel", "检测到 OpenAI 兼容格式")
                    choicesArray
                } else {
                    throw Exception("响应格式错误：缺少 output 或 choices 字段。可用字段: ${responseJson.keys.joinToString(", ")}")
                }
                
                if (choices.isEmpty()) {
                    throw Exception("响应格式错误：choices 为空")
                }
                
                Log.d("LLMModel", "choices 数量: ${choices.size}")
                
                val firstChoice = choices[0] as JsonObject
                Log.d("LLMModel", "firstChoice 键: ${firstChoice.keys.joinToString()}")
                
                // 兼容两种格式：DashScope 使用 message，OpenAI 兼容格式也可能不同
                val message = firstChoice["message"] as? JsonObject
                    ?: firstChoice["delta"] as? JsonObject  // 流式响应可能使用 delta
                    ?: throw Exception("响应格式错误：缺少 message/delta 字段。firstChoice 键: ${firstChoice.keys.joinToString()}")
                
                Log.d("LLMModel", "message 键: ${message.keys.joinToString()}")
                
                // 提取 content（可能是 String 或 Array）
                val contentValue = message["content"]
                Log.d("LLMModel", "content 类型: ${contentValue?.javaClass?.simpleName}")
                val content: String = when (contentValue) {
                    is JsonPrimitive -> contentValue.content
                    is JsonArray -> {
                        // 如果是数组（qwen-vl/qwen-audio模型），提取text字段
                        val firstItem = contentValue.firstOrNull()
                        when (firstItem) {
                            is JsonObject -> {
                                val textValue = firstItem["text"]
                                when (textValue) {
                                    is JsonPrimitive -> textValue.content
                                    is JsonObject -> {
                                        val value = textValue["value"]
                                        (value as? JsonPrimitive)?.content 
                                            ?: contentValue.joinToString(" ") { item -> 
                                                item?.toString() ?: "" 
                                            }
                                    }
                                    else -> contentValue.joinToString(" ") { item -> 
                                        item?.toString() ?: "" 
                                    }
                                }
                            }
                            else -> contentValue.joinToString(" ") { item -> 
                                item?.toString() ?: "" 
                            }
                        }
                    }
                    else -> contentValue?.toString() ?: ""
                }
                
                Log.d("LLMModel", "提取的 content: $content")
                
                // 提取 tool_calls（如果有）
                val toolCalls = mutableListOf<Map<String, Any>>()
                val toolCallsArray = message["tool_calls"] as? JsonArray
                Log.d("LLMModel", "tool_calls 数量: ${toolCallsArray?.size ?: 0}")
                
                toolCallsArray?.forEachIndexed { index, toolCallElement ->
                    val toolCall = toolCallElement as? JsonObject
                    if (toolCall != null) {
                        val functionObj = toolCall["function"] as? JsonObject
                        val argumentsStr = functionObj?.get("arguments")?.jsonPrimitive?.content ?: "{}"
                        
                        // 解析 arguments JSON 字符串为 Map
                        val arguments = try {
                            val argumentsJson = json.parseToJsonElement(argumentsStr)
                            if (argumentsJson is JsonObject) {
                                argumentsJson.entries.associate { (k, v) ->
                                    k to when (v) {
                                        is JsonPrimitive -> {
                                            when {
                                                v.booleanOrNull != null -> v.boolean
                                                v.doubleOrNull != null -> v.double
                                                v.longOrNull != null -> v.long
                                                else -> v.content
                                            }
                                        }
                                        is JsonArray -> v.map { arrayItem -> 
                                            if (arrayItem is JsonPrimitive) arrayItem.content else arrayItem.toString() 
                                        }
                                        is JsonObject -> v.entries.associate { (k2, v2) -> 
                                            k2 to (if (v2 is JsonPrimitive) v2.content else v2.toString()) 
                                        }
                                    }
                                }
                            } else {
                                emptyMap<String, Any>()
                            }
                        } catch (e: Exception) {
                            emptyMap<String, Any>()
                        }
                        
                        val toolCallMap = mapOf(
                            "id" to (toolCall["id"]?.jsonPrimitive?.content ?: ""),
                            "name" to (functionObj?.get("name")?.jsonPrimitive?.content ?: ""),
                            "arguments" to arguments
                        )
                        toolCalls.add(toolCallMap)
                        Log.d("LLMModel", "工具调用[$index]: name=${toolCallMap["name"]}, id=${toolCallMap["id"]}")
                        Log.d("LLMModel", "工具调用[$index] 参数: $arguments")
                    }
                }
                
                // 提取 usage 信息（兼容不同格式）
                val usageObj = responseJson["usage"] as? JsonObject
                val usage = usageObj?.let {
                    val usageMap = mapOf(
                        "input_tokens" to (it["prompt_tokens"]?.jsonPrimitive?.longOrNull  // OpenAI 格式
                            ?: it["input_tokens"]?.jsonPrimitive?.longOrNull ?: 0),  // DashScope 格式
                        "output_tokens" to (it["completion_tokens"]?.jsonPrimitive?.longOrNull  // OpenAI 格式
                            ?: it["output_tokens"]?.jsonPrimitive?.longOrNull ?: 0),  // DashScope 格式
                        "total_tokens" to (it["total_tokens"]?.jsonPrimitive?.longOrNull ?: 0),
                        "request_id" to (responseJson["request_id"]?.jsonPrimitive?.content ?: responseJson["id"]?.jsonPrimitive?.content ?: ""),
                        "finish_reason" to (firstChoice["finish_reason"]?.jsonPrimitive?.content ?: "")
                    )
                    Log.d("LLMModel", "Token 使用: input=${usageMap["input_tokens"]}, output=${usageMap["output_tokens"]}, total=${usageMap["total_tokens"]}")
                    usageMap
                }
                
                val llmOutput = LLMOutput(
                    content = content,
                    model = modelName,
                    usage = usage,
                    toolCalls = toolCalls
                )
                
                Log.d("LLMModel", "========== 最终输出 ==========")
                Log.d("LLMModel", "模型: ${llmOutput.model}")
                Log.d("LLMModel", "内容长度: ${llmOutput.content.length} 字符")
                Log.d("LLMModel", "内容预览: ${llmOutput.content.take(200)}...")
                Log.d("LLMModel", "工具调用数量: ${llmOutput.toolCalls.size}")
                Log.d("LLMModel", "=====================================")
                
                llmOutput
            } catch (e: Exception) {
                android.util.Log.e("LLMModel", "Error generating text: ${e.message}", e)
                throw Exception("生成文本失败: ${e.message}")
            }
        }
    }
    
    /**
     * 关闭 HTTP 客户端（资源清理）
     */
    fun close() {
        httpClient.close()
    }
}