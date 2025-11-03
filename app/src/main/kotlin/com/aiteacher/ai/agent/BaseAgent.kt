package com.aiteacher.ai.agent

import com.aiteacher.ai.service.AgentRequest
import com.aiteacher.ai.service.LLMModel
import com.aiteacher.ai.service.LLMOutput
import com.aiteacher.ai.service.MemoryManager
import com.aiteacher.ai.tool.BaseTool
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.*
import java.io.File
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.koin.android.ext.koin.androidContext
import org.koin.core.parameter.parametersOf
import android.content.Context
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 智能体基础类
 * 仿照 Python 版本的 base_agent 实现
 * - 使用 llm_model 作为核心推理模型
 * - 支持外围工具注册与调用（base_tool）
 * - 简单运行周期：接收用户输入 -> 调用模型 -> 如模型要求调用工具则执行工具 -> 将工具结果反馈给模型 -> 返回最终响应
 */
abstract class BaseAgent(
    val name: String,
    description: String? = null,
    protected val model: LLMModel = LLMModel("qwen-max"),
    tools: List<BaseTool> = emptyList(),
    memoryManagerName: String = "default",
    maxToolIterations: Int = 3
) : KoinComponent {
    val description: String = description ?: "An intelligent agent named $name capable of using tools and maintaining conversation context"
    
    // Agent可用的工具列表（保存在Agent中，不在Model中，支持动态添加）
    protected val tools: MutableList<BaseTool> = tools.toMutableList()
    protected val memoryManager: MemoryManager by inject(qualifier = named(memoryManagerName))
    protected val maxToolIterations: Int = maxOf(1, minOf(maxToolIterations, 10)) // 限制在合理范围内
    protected val running: AtomicBoolean = AtomicBoolean(false)
    
    // 状态管理
    private val _state = MutableStateFlow(AgentState.IDLE)
    val state: StateFlow<AgentState> = _state.asStateFlow()
    
    // 系统提示头 - 简化版本，DashScope API 会自动处理工具调用
    protected val promptHead: Map<String, Any> by lazy {
        val content = buildSystemPrompt()
        mapOf(
            "role" to "system",
            "content" to content
        )
    }
    
    /**
     * 构建系统提示词 - 子类必须实现
     */
    abstract fun buildSystemPrompt(): String
    
    /**
     * 动态添加工具
     * 将工具添加到Agent的工具列表中，工具规格会自动在下次调用模型时传递给LLM
     */
    fun addTool(tool: BaseTool) {
        if (tools.none { it.toolName == tool.toolName }) {
            tools.add(tool)
            android.util.Log.d("BaseAgent", "已添加工具: ${tool.toolName}")
        } else {
            android.util.Log.w("BaseAgent", "工具 ${tool.toolName} 已存在，跳过添加")
        }
    }
    
    /**
     * 从配置文件动态加载工具
     * 
     * 【配置文件格式】
     * JSON格式：{"tools": ["tool_name1", "tool_name2", ...]}
     * 
     * 【重要说明】
     * 1. 配置文件中的工具名必须是工具类的 `toolName` 参数值（不是文件名或类名）
     *    示例：MathTool 的 toolName = "math_calculator"，配置文件中应写 "math_calculator"
     * 
     * 2. 工具加载顺序：
     *    - 首先尝试通过 getToolByName() 创建无依赖的工具
     *    - 如果失败，尝试通过 toolFactory 创建需要依赖的工具
     * 
     * 3. 工具名称必须在 Tools.kt 的 getToolByName() 或 toolFactory 中注册才能使用
     * 
     * 4. 配置文件路径：
     *    - 如果是 assets 路径（如 "home_tools.json"），会从 assets 目录读取
     *    - 如果是文件系统路径（如 "app/src/.../file.json"），会尝试从文件系统读取
     * 
     * @param configPath 配置文件路径（assets 路径或文件系统路径）
     * @param toolFactory 工具工厂函数，用于创建需要依赖的工具（toolName -> BaseTool?）
     * @return 成功加载的工具数量
     */
    fun loadToolsFromConfig(
        configPath: String,
        toolFactory: ((String) -> BaseTool?)? = null
    ): Int {
        return try {
            val configContent = readConfigFile(configPath)
            if (configContent == null) {
                android.util.Log.w("BaseAgent", "无法读取配置文件: $configPath")
                return 0
            }
            val json = Json { ignoreUnknownKeys = true }
            val config = json.parseToJsonElement(configContent) as JsonObject
            
            // 解析工具列表
            val toolNames = config["tools"]?.let { toolsElement ->
                if (toolsElement is kotlinx.serialization.json.JsonArray && toolsElement.isNotEmpty()) {
                    toolsElement.map { toolElement -> 
                        toolElement.jsonPrimitive.content 
                    }
                } else {
                    emptyList()
                }
            } ?: emptyList()
            
            // 根据工具名称获取工具实例并添加到Agent
            var loadedCount = 0
            toolNames.forEach { toolName ->
                // 清理工具名称（移除空格）
                val cleanToolName = toolName.trim()
                if (cleanToolName.isBlank()) {
                    android.util.Log.w("BaseAgent", "跳过空工具名")
                    return@forEach
                }
                
                // 首先尝试通过 getToolByName 创建无依赖的工具
                var tool = com.aiteacher.ai.tool.getToolByName(cleanToolName)
                
                // 如果失败且提供了工具工厂，尝试通过工厂创建
                if (tool == null && toolFactory != null) {
                    tool = toolFactory(cleanToolName)
                }
                
                if (tool != null) {
                    addTool(tool)
                    loadedCount++
                } else {
                    android.util.Log.w("BaseAgent", "未找到工具: $cleanToolName")
                }
            }
            
            android.util.Log.d("BaseAgent", "从配置文件加载了 $loadedCount 个工具")
            
            // 打印加载的工具规格
            printToolsSpecs()
            
            loadedCount
        } catch (e: Exception) {
            android.util.Log.e("BaseAgent", "解析配置文件失败: ${e.message}", e)
            0
        }
    }
    
    /**
     * 调用工具
     */
    suspend fun callTool(toolName: String, vararg args: Any): com.aiteacher.ai.tool.ToolResult? {
        val tool = tools.find { it.toolName == toolName }
        return if (tool != null) {
            try {
                val result = tool.toolFunction(*args)
                tool.toolOutput = result
                result
            } catch (e: Exception) {
                val errorResult = com.aiteacher.ai.tool.ToolResult.ExecuteResult("Error: ${e.message}")
                tool.toolOutput = errorResult
                errorResult
            }
        } else {
            throw IllegalArgumentException("Tool not found: $toolName")
        }
    }
    
    /**
     * 根据记忆构造提交给模型的 prompt
     */
    protected fun buildPrompt(n: Int? = null): List<Map<String, Any>> {
        val ctx: List<Map<String, Any>> = memoryManager.getMemory(n)
        return listOf(promptHead) + ctx
    }
    
    /**
     * 单次思考+工具调用的结果
     */
    data class RunOnceResult(
        val llmOutput: LLMOutput?,           // LLM 输出（null 表示调用失败）
        val hasToolCalls: Boolean,            // 是否调用了工具
        val hasExecuteTool: Boolean,           // 是否调用了执行类工具
        val executeToolSuccess: Boolean        // 执行类工具是否成功（只有在 hasExecuteTool=true 时才有意义）
    )

    /**
     * 单次思考+工具调用（最基础的操作）
     * - 构建 prompt 并调用 LLM（Think）
     * - 解析工具调用（如果有）
     * - 执行工具并将结果反馈到记忆（Act + Observe）
     *
     * @return RunOnceResult 包含 LLM 输出和工具调用情况
     */
    suspend fun runOnce(): RunOnceResult {
        // 构建提示词并调用模型
        val prompt = buildPrompt()
        
        // 将工具转换为工具规格
        val toolSpecs = tools.map { it.toToolSpec() }
        
        // 打印工具规格（用于调试）
        // printToolsSpecsForRequest(toolSpecs)
        
        // 构建AgentRequest，只传入必需参数，其他使用LLMModel的默认值
        val request = AgentRequest(
            messages = prompt,
            tools = toolSpecs
        )
        
        val llmOutput = model.generateText(request)
        
        if (llmOutput == null) {
            return RunOnceResult(
                llmOutput = null,
                hasToolCalls = false,
                hasExecuteTool = false,
                executeToolSuccess = false
            )
        }

        // 直接从 LLM 输出中获取工具调用（如果 LLM 支持工具调用，会直接返回）
        val toolCalls = llmOutput.toolCalls
        
        // 如果有工具调用，需要在 assistant 消息中保存 tool_calls 信息
        // 这样才能让后续的 tool 消息正确关联
        if (toolCalls.isNotEmpty()) {
            // 将 toolCalls 转换为符合 API 格式的 JSON 字符串
            val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            
            // 直接构建 JSON 数组，避免中间类型推断问题
            val toolCallsJsonElement = kotlinx.serialization.json.buildJsonArray {
                toolCalls.forEach { call ->
                    val callId = call["id"] as? String ?: ""
                    val callName = call["name"] as? String ?: ""
                    val callArgs = call["arguments"] as? Map<*, *>
                    
                    // 将 arguments Map 转换为 JSON 字符串
                    val argumentsStr = callArgs?.let { args ->
                        @Suppress("UNCHECKED_CAST")
                        val argsMap = args as Map<String, Any>
                        val argsJsonObj = kotlinx.serialization.json.buildJsonObject {
                            argsMap.forEach { (key: String, value: Any) ->
                                put(key, when (value) {
                                    is String -> kotlinx.serialization.json.JsonPrimitive(value)
                                    is Number -> kotlinx.serialization.json.JsonPrimitive(value)
                                    is Boolean -> kotlinx.serialization.json.JsonPrimitive(value)
                                    else -> kotlinx.serialization.json.JsonPrimitive(value.toString())
                                })
                            }
                        }
                        json.encodeToString(kotlinx.serialization.json.JsonObject.serializer(), argsJsonObj)
                    } ?: "{}"
                    
                    // 构建 tool_call 对象
                    add(kotlinx.serialization.json.buildJsonObject {
                        put("id", callId)
                        put("type", "function")
                        put("function", kotlinx.serialization.json.buildJsonObject {
                            put("name", callName)
                            put("arguments", argumentsStr)
                        })
                    })
                }
            }
            
            val toolCallsJson = json.encodeToString(
                kotlinx.serialization.json.JsonArray.serializer(),
                toolCallsJsonElement
            )
            
            val assistantMetadata = mapOf(
                "tool_calls_json" to toolCallsJson
            )
            
            memoryManager.insertMessage(
                role = "assistant",
                content = llmOutput.content,
                metadata = assistantMetadata
            )
        } else {
            // 没有工具调用，正常保存
            memoryManager.insertMessage(
                role = "assistant",
                content = llmOutput.content
            )
        }
        
        if (toolCalls.isEmpty()) {
            // 没有工具调用，LLM 可能给出了最终回复，但需要由 runReAct 中的 LLM 判断是否完成任务
            return RunOnceResult(
                llmOutput = llmOutput,
                hasToolCalls = false,
                hasExecuteTool = false,
                executeToolSuccess = false
            )
        }

        // 有工具调用，执行所有工具
        var hasExecuteTool = false
        var executeToolSuccess = false
        var hasFailedExecuteTool = false

        for (call in toolCalls) {
            val toolName = call["name"] as? String ?: ""
            val toolCallId = call["id"] as? String ?: ""
            if (toolName.isBlank()) {
                android.util.Log.w("BaseAgent", "工具调用缺少 name 字段: $call")
                continue
            }
            if (toolCallId.isBlank()) {
                android.util.Log.w("BaseAgent", "工具调用缺少 id 字段: $call")
                continue
            }
            
            val toolArguments = (call["arguments"] as? Map<*, *>)?.let { 
                @Suppress("UNCHECKED_CAST")
                it as Map<String, Any>
            } ?: emptyMap()

            try {
                // 将整个 Map 作为第一个参数传递（这样工具可以访问所有键值对）
                val args = arrayOf(toolArguments)
                val result = callTool(toolName, *args)

                // 工具消息的 metadata 需要包含 tool_call_id 和 tool_name
                val toolMetadata = mapOf(
                    "tool_call_id" to toolCallId,
                    "tool_name" to toolName
                )

                when (result) {
                    is com.aiteacher.ai.tool.ToolResult.QueryResult -> {
                        // 查询类结果：反馈给 LLM，继续思考
                        memoryManager.insertMessage(
                            role = "tool",
                            content = result.data.toString(),
                            metadata = toolMetadata
                        )
                    }

                    is com.aiteacher.ai.tool.ToolResult.ExecuteResult -> {
                        // 执行类结果：无论成功/失败都反馈给 LLM
                        hasExecuteTool = true
                        if (result.success) {
                            executeToolSuccess = true
                        } else {
                            hasFailedExecuteTool = true
                        }

                        memoryManager.insertMessage(
                            role = "tool",
                            content = if(result.success) "success: ${result.message}" else "error: ${result.message}",
                            metadata = toolMetadata
                        )
                    }

                    null -> {
                        memoryManager.insertMessage(
                            role = "tool",
                            content = "Tool result is null",
                            metadata = toolMetadata
                        )
                    }
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unknown error"
                memoryManager.insertMessage(
                    role = "tool",
                    content = "Error: $errorMsg",
                    metadata = mapOf(
                        "tool_call_id" to toolCallId,
                        "tool_name" to toolName
                    )
                )
            }
        }

        // 执行了工具，返回详细信息
        return RunOnceResult(
            llmOutput = llmOutput,
            hasToolCalls = true,
            hasExecuteTool = hasExecuteTool,
            executeToolSuccess = executeToolSuccess && !hasFailedExecuteTool  // 所有执行类工具都成功才算成功
        )
    }

    /**
     * ReAct 循环：由 LLM 判断任务是否完成
     * - 接收用户输入并添加到记忆
     * - 循环执行：Think（思考）→ Act（调用工具）→ Observe（观察结果）→ Think（再次思考）
     * - LLM 自己判断任务是否完成（通过明确回复或特殊标记）
     * - maxToolIterations 仅作为安全上限，防止无限循环
     *
     * ReAct 流程：
     * 1. Think: LLM 分析用户输入和上下文，决定是否需要工具
     * 2. Act: 如果需要，调用相应工具（查询类或执行类）
     * 3. Observe: 获取工具结果（无论成功/失败都反馈给 LLM）
     * 4. Think again: LLM 根据工具结果判断任务是否完成
     *    - 如果有执行类工具成功：让 LLM 判断是否可以结束任务
     *    - 如果有执行类工具失败：继续循环，让 LLM 处理错误
     *    - 如果只有查询类工具：继续思考，可能还需要更多信息或行动
     *    - 如果没有工具调用：让 LLM 明确判断任务是否完成
     */
    suspend fun runReAct(userInput: String): Result<String> {
        if (running.get()) {
            return Result.failure(Exception("Agent is already running"))
        }

        running.set(true)
        _state.value = AgentState.RUNNING

        try {
            // 添加用户输入到记忆
            memoryManager.insertMessage(
                role = "user",
                content = userInput
            )

            var iterations = 0
            var lastOutput: LLMOutput? = null

            // ReAct 循环：循环调用 runOnce，直到 LLM 决定任务完成
            while (iterations < maxToolIterations) {
                val result = runOnce()

                if (result.llmOutput == null) {
                    // LLM 调用失败
                    _state.value = AgentState.ERROR
                    return Result.failure(Exception("Model returned invalid output"))
                }

                lastOutput = result.llmOutput

                // 判断是否应该结束循环
                if (!result.hasToolCalls) {
                    // 没有工具调用，LLM 给出了最终回复，任务完成
                    val content = result.llmOutput.content.trim()
                    if (content.isNotBlank()) {
                        // LLM 给出了回复且没有调用工具，认为任务完成
                        break
                    } else {
                        // LLM 没有给出内容，可能出错，但仍然结束
                        break
                    }
                } else if (result.hasExecuteTool && result.executeToolSuccess) {
                    // 执行类工具成功，任务完成，结束循环
                    // 执行类工具的成功结果已经在 runOnce 中写入记忆
                    break
                } else if (result.hasExecuteTool && !result.executeToolSuccess) {
                    // 执行类工具失败，必须继续循环，让 LLM 处理错误
                    iterations++
                    continue
                } else {
                    // 只有查询类工具，继续思考
                    iterations++
                    continue
                }
            }

            // 检查是否达到最大迭代次数
            if (iterations >= maxToolIterations) {
                android.util.Log.w("BaseAgent", "Reached max tool iterations ($maxToolIterations)")
            }

            // 获取最终回复（已经在 runOnce 中写入记忆，不需要再次写入）
            val finalResponse = lastOutput?.content?.takeIf { it.isNotBlank() }
                ?: "Sorry, I couldn't generate a response."

            _state.value = AgentState.IDLE
            return Result.success(finalResponse)
            
        } catch (e: Exception) {
            _state.value = AgentState.ERROR
            return Result.failure(e)
        } finally {
            running.set(false)
        }
    }
    
    /**
     * 连续运行：处理多轮对话（兼容旧接口）
     * 实际使用 ReAct 循环
     */
    suspend fun runContinuous(userInput: String): Result<String> {
        return runReAct(userInput)
    }
    
    /**
     * 初始化 MemoryManager（设置 userId 和 sessionId）
     * @param userId 用户ID
     * @param sessionId 会话ID（可选，默认使用 "agent_${name}_${userId}"）
     */
    suspend fun initializeMemory(userId: String, sessionId: String? = null) {
        val finalSessionId = sessionId ?: "${name}_${userId}"
        memoryManager.initialize(userId, finalSessionId)
    }
    
    /**
     * 打印工具规格（用于调试）
     * 在 Agent 初始化完成后调用，查看加载的工具
     */
    fun printToolsSpecs() {
        android.util.Log.d("BaseAgent", "========== Agent 工具列表 ($name) ==========")
        android.util.Log.d("BaseAgent", "工具数量: ${tools.size}")
        
        tools.forEachIndexed { index, tool ->
            val spec = tool.toToolSpec()
            val json = kotlinx.serialization.json.Json { 
                ignoreUnknownKeys = true
                prettyPrint = true
            }
            try {
                // 将 Map 转换为 JsonObject 以便格式化输出
                val jsonObject = buildJsonObject {
                    spec.forEach { (key, value) ->
                        put(key, valueToJsonElement(value))
                    }
                }
                val formattedJson = json.encodeToString(kotlinx.serialization.json.JsonElement.serializer(), jsonObject)
                android.util.Log.d("BaseAgent", "工具[$index] ${tool.toolName}:")
                android.util.Log.d("BaseAgent", formattedJson)
            } catch (e: Exception) {
                android.util.Log.d("BaseAgent", "工具[$index] ${tool.toolName}: $spec")
            }
        }
        android.util.Log.d("BaseAgent", "=====================================")
    }
    
    /**
     * 打印请求中的工具规格（用于调试）
     * 在发送请求前调用，查看实际发送给 LLM 的工具规格
     */
    private fun printToolsSpecsForRequest(toolSpecs: List<Map<String, Any>>) {
        android.util.Log.d("BaseAgent", "========== 发送给 LLM 的工具规格 ($name) ==========")
        android.util.Log.d("BaseAgent", "工具数量: ${toolSpecs.size}")
        
        val json = kotlinx.serialization.json.Json { 
            ignoreUnknownKeys = true
            prettyPrint = true
        }
        
        toolSpecs.forEachIndexed { index, spec ->
            try {
                // 将 Map 转换为 JsonObject 以便格式化输出
                val jsonObject = buildJsonObject {
                    spec.forEach { (key, value) ->
                        put(key, valueToJsonElement(value))
                    }
                }
                val formattedJson = json.encodeToString(kotlinx.serialization.json.JsonElement.serializer(), jsonObject)
                android.util.Log.d("BaseAgent", "工具规格[$index]:")
                android.util.Log.d("BaseAgent", formattedJson)
            } catch (e: Exception) {
                android.util.Log.d("BaseAgent", "工具规格[$index]: $spec")
                android.util.Log.w("BaseAgent", "格式化工具规格失败: ${e.message}")
            }
        }
        android.util.Log.d("BaseAgent", "=====================================")
    }
    
    /**
     * 读取配置文件内容
     * 支持从 assets 目录或文件系统读取
     * 
     * @param configPath 配置路径：
     *   - assets 路径：如 "home_tools.json" 或 "configs/home_tools.json"
     *   - 文件系统路径：如 "app/src/main/kotlin/..."
     * @return 文件内容，如果读取失败返回 null
     */
    private fun readConfigFile(configPath: String): String? {
        return try {
            // 先尝试从 assets 读取（适用于 Android 应用）
            try {
                // 通过 Application 单例获取 Context
                val context = try {
                    com.aiteacher.AITeacherApplication.getInstance()
                } catch (e: Exception) {
                    android.util.Log.w("BaseAgent", "无法获取 Application 实例，尝试文件系统读取: ${e.message}")
                    throw e
                }
                val assetManager = context.assets
                
                // 如果路径包含 "/"，尝试直接打开；否则尝试在 assets 根目录查找
                val assetPath = if (configPath.contains("/")) {
                    configPath
                } else {
                    // 尝试在 assets 根目录查找
                    configPath
                }
                
                assetManager.open(assetPath).use { inputStream ->
                    inputStream.bufferedReader().use { it.readText() }
                }?.also {
                    android.util.Log.d("BaseAgent", "从 assets 读取配置文件: $assetPath")
                }
            } catch (e: Exception) {
                // assets 读取失败，尝试从文件系统读取（开发/调试时）
                android.util.Log.d("BaseAgent", "从 assets 读取失败，尝试文件系统: ${e.message}")
                
                val configFile = java.io.File(configPath)
                if (configFile.exists()) {
                    configFile.readText().also {
                        android.util.Log.d("BaseAgent", "从文件系统读取配置文件: $configPath")
                    }
                } else {
                    android.util.Log.w("BaseAgent", "配置文件不存在: $configPath")
                    null
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("BaseAgent", "读取配置文件失败: $configPath", e)
            null
        }
    }
    
    /**
     * 将任意值转换为 JsonElement（用于格式化输出）
     */
    private fun valueToJsonElement(value: Any?): kotlinx.serialization.json.JsonElement {
        return when (value) {
            null -> kotlinx.serialization.json.JsonNull
            is String -> kotlinx.serialization.json.JsonPrimitive(value)
            is Number -> kotlinx.serialization.json.JsonPrimitive(value)
            is Boolean -> kotlinx.serialization.json.JsonPrimitive(value)
            is Map<*, *> -> {
                kotlinx.serialization.json.buildJsonObject {
                    @Suppress("UNCHECKED_CAST")
                    (value as Map<String, Any?>).forEach { (k, v) ->
                        put(k, valueToJsonElement(v))
                    }
                }
            }
            is List<*> -> {
                kotlinx.serialization.json.buildJsonArray {
                    value.forEach { item ->
                        add(valueToJsonElement(item))
                    }
                }
            }
            else -> kotlinx.serialization.json.JsonPrimitive(value.toString())
        }
    }
    
    /**
     * 关闭Agent
     */
    fun close() {
        // 简化版本，无需清理 MCP 连接
        running.set(false)
        _state.value = AgentState.IDLE
    }
    
}

/**
 * Agent状态
 */
enum class AgentState {
    IDLE,
    RUNNING,
    ERROR
}
