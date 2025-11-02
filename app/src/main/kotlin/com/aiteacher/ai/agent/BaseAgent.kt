package com.aiteacher.ai.agent

import com.aiteacher.ai.tool.*
import com.aiteacher.ai.service.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
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
    // Agent可用的工具列表
    val tools: List<BaseTool> = emptyList(),
    memoryManagerName: String = "default",
    maxToolIterations: Int = 3
) : KoinComponent {
    val description: String = description ?: "An intelligent agent named $name capable of using tools and maintaining conversation context"

    protected val memoryManager: MemoryManager by inject(qualifier = named(memoryManagerName))
    protected val maxToolIterations: Int = maxOf(1, minOf(maxToolIterations, 10)) // 限制在合理范围内
    protected val running: AtomicBoolean = AtomicBoolean(false)
    
    // 状态管理
    private val _state = MutableStateFlow(AgentState.IDLE)
    val state: StateFlow<AgentState> = _state.asStateFlow()
    
    init {
        // 将工具规格添加到模型中
        tools.forEach { tool ->
            model.addTool(tool.toToolSpec())
        }
    }
    
    // 系统提示头 - 根据是否有工具动态生成
    protected val promptHead: Map<String, String> by lazy {
        val content = if (tools.isNotEmpty()) {
            buildString {
                // 基础角色描述
                appendLine("You are $name, an intelligent assistant with access to various tools.")

                // 详细工具列表（包含参数说明）
                appendLine("\n【可用工具列表】")
                tools.forEachIndexed { index, tool ->
                    appendLine("\n工具 ${index + 1}: ${tool.toolName}")
                    appendLine("描述：${tool.toolDescription}")

                    // 提取参数信息
                    val params = tool.parameters
                    val properties = (params["properties"] as? Map<*, *>) ?: emptyMap<String, Any>()
                    val required = (params["required"] as? List<*>) ?: emptyList<String>()

                    if (properties.isNotEmpty()) {
                        appendLine("参数说明：")
                        @Suppress("UNCHECKED_CAST")
                        (properties as Map<String, Any>).forEach { (paramName, paramInfo) ->
                            @Suppress("UNCHECKED_CAST")
                            val paramMap = (paramInfo as? Map<String, Any>) ?: emptyMap<String, Any>()
                            val paramType = paramMap["type"] as? String ?: "string"
                            val paramDesc = paramMap["description"] as? String ?: ""
                            val enumValues = paramMap["enum"] as? List<*>

                            val requiredMark = if (required.contains(paramName)) "【必需】" else "【可选】"
                            append("  - $paramName ($paramType) $requiredMark: $paramDesc")

                            if (enumValues != null && enumValues.isNotEmpty()) {
                                append(" 可选值: ${enumValues.joinToString(", ")}")
                            }
                            appendLine()
                        }

                        if (required.isNotEmpty()) {
                            appendLine("必需参数: ${required.joinToString(", ")}")
                        }
                    } else {
                        appendLine("该工具无需额外参数")
                    }
                }

                // 工具调用格式约定
                appendLine("\n【工具调用格式】")
                appendLine("当需要使用工具时，请严格按照以下格式回复（必须是有效的JSON）：")
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
                appendLine("\n重要提示：")
                appendLine("1. 必须使用上述JSON格式，不要添加其他文本")
                appendLine("2. 工具名称必须与可用工具列表中的名称完全一致")
                appendLine("3. 参数必须符合工具定义的参数要求")
                appendLine("4. 必需参数必须提供，可选参数可以省略")
                appendLine("5. 如果不需要调用工具，直接回复普通文本即可")

                appendLine("\n【使用指南】")
                appendLine("1. Use tools only when necessary")
                appendLine("2. When using a tool, provide clear and complete parameters")
                appendLine("3. After receiving tool results, incorporate them into your response appropriately")
                appendLine("4. If a task cannot be completed, explain why clearly")
            }
        } else {
            buildSystemPrompt()
        }
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
     * 添加工具
     */
    fun addTool(tool: BaseTool) {
        // 注意：这里只是添加到当前实例，不会持久化
        // 如果需要持久化，应该通过配置文件重新创建 Agent
        model.addTool(tool.toToolSpec())
    }
    
    /**
     * 获取工具
     */
    fun getTool(toolName: String): BaseTool? {
        return tools.find { it.toolName == toolName }
    }
    
    /**
     * 调用工具
     */
    suspend fun callTool(toolName: String, vararg args: Any): com.aiteacher.ai.tool.ToolResult? {
        val tool = getTool(toolName)
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
     * 检查工具是否可用
     */
    fun isToolAvailable(toolName: String): Boolean {
        return tools.any { it.toolName == toolName }
    }
    
    /**
     * 检查是否运行在无工具模式
     */
    fun isNoToolsMode(): Boolean {
        return tools.isEmpty()
    }
    
    /**
     * 根据记忆构造提交给模型的 prompt
     */
    protected fun buildPrompt(n: Int? = null): List<Map<String, String>> {
        val ctx = memoryManager.getMemory(n)
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
        val llmOutput = model.generateTextWithTools(prompt)

        if (llmOutput == null) {
            return RunOnceResult(
                llmOutput = null,
                hasToolCalls = false,
                hasExecuteTool = false,
                executeToolSuccess = false
            )
        }

        // 添加助手响应到记忆
        memoryManager.insertMessage(
            role = "assistant",
            content = llmOutput.content
        )

        // 解析工具调用
        val toolCalls = parseToolCall(llmOutput)

        if (toolCalls.isNullOrEmpty()) {
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
            val toolArguments = (call["arguments"] as? Map<*, *>)?.let {
                @Suppress("UNCHECKED_CAST")
                it as Map<String, Any>
            } ?: emptyMap()

            try {
                // 将整个 Map 作为第一个参数传递（这样工具可以访问所有键值对）
                val args = arrayOf(toolArguments)
                val result = callTool(toolName, *args)

                when (result) {
                    is com.aiteacher.ai.tool.ToolResult.QueryResult -> {
                        // 查询类结果：反馈给 LLM，继续思考
                        memoryManager.insertMessage(
                            role = "tool:$toolName",
                            content = result.data.toString()
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
                            role = "tool:$toolName",
                            content = if(result.success) "success" else "error" + result.message
                        )
                    }

                    null -> {
                        memoryManager.insertMessage(
                            role = "tool:$toolName",
                            content = "Tool result is null"
                        )
                    }
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unknown error"
                memoryManager.insertMessage(
                    role = "tool:$toolName",
                    content = "Error: $errorMsg"
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
     * 关闭Agent
     */
    fun close() {
        // 简化版本，无需清理 MCP 连接
        running.set(false)
        _state.value = AgentState.IDLE
    }

    /**
     * 解析工具调用
     * 从 LLM 输出中提取工具调用信息
     * 支持格式：
     * 1. JSON 代码块格式：```json { "tool_call": { "name": "...", "arguments": {...} } } ```
     * 2. 纯 JSON 格式：{ "tool_call": { "name": "...", "arguments": {...} } }
     */
    protected fun parseToolCall(output: LLMOutput?): List<Map<String, Any>>? {
        if (output == null) return null

        try {
            val content = output.content.trim()
            if (content.isEmpty()) return emptyList()

            val json = Json {
                ignoreUnknownKeys = true
                isLenient = true  // 允许宽松解析
            }

            var jsonContent: String? = null

            // 尝试1：查找 JSON 代码块
            if (content.contains("```json")) {
                val jsonStart = content.indexOf("```json") + 7
                val jsonEnd = content.indexOf("```", jsonStart)
                if (jsonStart > 6 && jsonEnd > jsonStart) {
                    jsonContent = content.substring(jsonStart, jsonEnd).trim()
                }
            }
            // 尝试2：查找第一个 { 到最后一个 } 之间的内容（纯 JSON）
            else if (content.startsWith("{") && content.contains("tool_call")) {
                val jsonStart = content.indexOf('{')
                val jsonEnd = content.lastIndexOf('}') + 1
                if (jsonStart >= 0 && jsonEnd > jsonStart) {
                    jsonContent = content.substring(jsonStart, jsonEnd)
                }
            }

            // 如果没有找到工具调用格式，返回空列表（表示普通回复）
            if (jsonContent == null) {
                return emptyList()
            }

            // 解析 JSON
            val jsonElement = json.parseToJsonElement(jsonContent)
            val jsonObject = jsonElement.jsonObject

            // 提取 tool_call
            val toolCallElement = jsonObject["tool_call"] ?: jsonObject["toolCall"]
            if (toolCallElement == null) {
                // 可能格式不同，尝试直接解析为 tool_call
                val toolCallDirect = jsonObject.get("name")?.let { name ->
                    val arguments = jsonObject["arguments"] ?: JsonObject(emptyMap())
                    mapOf(
                        "name" to name.jsonPrimitive.content,
                        "arguments" to arguments
                    )
                }
                if (toolCallDirect != null) {
                    return listOf(toolCallDirect)
                }
                return emptyList()
            }

            val toolCallObject = toolCallElement.jsonObject
            val toolName = toolCallObject["name"]?.jsonPrimitive?.content
            val argumentsElement = toolCallObject["arguments"]

            if (toolName.isNullOrBlank()) {
                return emptyList()
            }

            // 此时 toolName 一定非空（经过 isNullOrBlank 检查后）
            val finalToolName = toolName

            // 解析 arguments
            val argumentsMap = mutableMapOf<String, Any>()
            if (argumentsElement is JsonObject) {
                argumentsElement.forEach { (key, value) ->
                    argumentsMap[key] = when {
                        value is JsonPrimitive -> {
                            // 尝试解析为不同类型
                            when {
                                value.isString -> value.content
                                value.booleanOrNull != null -> value.boolean
                                value.doubleOrNull != null -> value.double
                                value.longOrNull != null -> value.long
                                else -> value.content
                            }
                        }
                        value is JsonArray -> {
                            value.map { element ->
                                if (element is JsonPrimitive) element.content else element.toString()
                            }
                        }
                        value is JsonObject -> {
                            // 递归处理嵌套对象
                            value.entries.associate { (k, v) ->
                                k to (if (v is JsonPrimitive) v.content else v.toString())
                            }
                        }
                        else -> value.toString()
                    }
                }
            }

            return listOf(
                mapOf(
                    "name" to finalToolName,
                    "arguments" to argumentsMap
                )
            )

        } catch (e: Exception) {
            android.util.Log.w("BaseAgent", "解析工具调用失败: ${e.message}\n内容: ${output.content}")
            return emptyList()  // 解析失败时返回空列表，当作普通回复处理
        }
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