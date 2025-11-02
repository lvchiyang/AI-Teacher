package com.aiteacher.ai.agent

import com.aiteacher.ai.tool.*
import com.aiteacher.ai.service.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
            """You are $name, an intelligent assistant with access to various tools.
Available tools: ${tools.map { it.toolName }.joinToString(", ")}
You can use these tools when needed to accomplish tasks. Always follow these guidelines:
1. Use tools only when necessary
2. When using a tool, provide clear and complete parameters
3. After receiving tool results, incorporate them into your response appropriately
4. If a task cannot be completed, explain why clearly"""
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
     * 单次运行：
     * - 构造 prompt（包含上下文）
     * - 调用模型
     * - 如果模型请求工具调用，执行工具并将结果反馈给模型，最多迭代 maxToolIterations 次
     * - 返回最终文本响应
     */
    suspend fun runOnce(userInput: String): Result<String> {
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
            
            // 构建提示词
            val prompt = buildPrompt()
            
            // 调用模型
            val llmOutput = model.generateTextWithTools(prompt)
            if (llmOutput == null) {
                return Result.failure(Exception("Model returned invalid output"))
            }
            
            var modelOutput = llmOutput
            var iterations = 0
            
            // 循环解析模型输出，看是否需要工具调用
            while (iterations < maxToolIterations) {
                // 添加助手响应到记忆
                memoryManager.insertMessage(
                    role = "assistant",
                    content = modelOutput?.content ?: ""
                )
                
                // 解析工具调用
                val toolCalls = modelOutput?.let { model.parseToolCall(it) }
                if (toolCalls.isNullOrEmpty()) {
                    break
                }
                
                for (call in toolCalls) {
                    val toolName = call["name"] as? String ?: ""
                    val toolArguments = (call["arguments"] as? Map<*, *>)?.let { 
                        @Suppress("UNCHECKED_CAST")
                        it as Map<String, Any>
                    } ?: emptyMap()
                    
                    try {
                        // 将 Map 参数转换为 vararg 参数
                        val args = toolArguments.values.toTypedArray()
                        val result = callTool(toolName, *args)
                        memoryManager.insertMessage(
                            role = "tool:$toolName",
                            content = result.toString()
                        )
                    } catch (e: Exception) {
                        val errorMsg = e.message ?: "Unknown error"
                        memoryManager.insertMessage(
                            role = "tool:$toolName",
                            content = "error: $errorMsg"
                        )
                    }
                }
                
                // 把工具输出反馈给模型以便生成最终回答
                val followupPrompt = buildPrompt()
                val followupOutput = model.generateTextWithTools(followupPrompt)
                if (followupOutput == null) {
                    break
                }
                modelOutput = followupOutput
                iterations++
            }
            
            // 将智能体最终回复写入记忆并返回
            val finalResponse = modelOutput?.content ?: "Sorry, I couldn't generate a response."
            memoryManager.insertMessage(
                role = "assistant",
                content = finalResponse
            )
            
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
     * 连续运行：处理多轮对话
     */
    suspend fun runContinuous(userInput: String): Result<String> {
        return runOnce(userInput)
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