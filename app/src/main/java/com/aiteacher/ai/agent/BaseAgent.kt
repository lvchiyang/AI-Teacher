package com.aiteacher.ai.agent

import com.aiteacher.ai.service.LLMModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.mutableListOf

/**
 * 智能体基础类
 * 完全对应Python版本的base_agent类
 * - 使用 llm_model 作为核心推理模型
 * - 支持外围工具注册与调用（base_tool）
 * - 简单运行周期：接收用户输入 -> 调用模型 -> 如模型要求调用工具则执行工具 -> 将工具结果反馈给模型 -> 返回最终响应
 */
abstract class BaseAgent(
    val name: String,
    description: String? = null,
    protected val model: LLMModel = LLMModel("qwen-max"),
    memory: ContextMemory = UserManager().getCurrentUserMemory() ?: ContextMemory(maxMemorySize = 20),
    maxToolIterations: Int = 3
) {
    val description: String = description ?: "An intelligent agent named $name capable of using tools and maintaining conversation context"
    
    // Agent可用的工具列表
    protected val availableTools: List<String> = emptyList()
    protected val memory: ContextMemory = memory
    protected val maxToolIterations: Int = maxOf(1, minOf(maxToolIterations, 10)) // 限制在合理范围内
    protected val running: AtomicBoolean = AtomicBoolean(false)
    
    init {
        // Agent初始化完成
        println("Agent '$name' initialized (no-tools mode)")
    }
    
    // 系统提示头
    protected val promptHead: Map<String, String> by lazy {
        val content = """You are $name, an intelligent assistant.
You do not have access to any external tools, so you must rely on your knowledge and reasoning abilities to help users.
Please provide helpful responses based on your training and knowledge."""
        
        mapOf(
            "role" to "system",
            "content" to content
        )
    }
    
    // 状态管理
    private val _state = MutableStateFlow(AgentState.IDLE)
    val state: StateFlow<AgentState> = _state.asStateFlow()
    
    
    /**
     * 获取Agent可用的工具列表
     */
    fun getAvailableToolsList(): List<String> {
        return availableTools
    }
    
    /**
     * 检查是否运行在无工具模式
     */
    fun isNoToolsMode(): Boolean {
        return true
    }
    
    /**
     * 构建系统提示词 - 子类必须实现
     */
    abstract fun buildSystemPrompt(): String
    
    /**
     * 根据记忆和工具信息构造提交给模型的 prompt
     */
    protected fun buildPrompt(n: Int? = null): List<Map<String, String>> {
        val ctx = if (n == null) {
            memory.getContext(memory.getMemoryCount())
        } else {
            memory.getContext(n)
        }
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
        // 防御性检查最大迭代次数
        if (maxToolIterations <= 0) {
            println("Warning: max_tool_iterations should be positive integer.")
            return Result.failure(IllegalArgumentException("Invalid max_tool_iterations setting."))
        }
        
        _state.value = AgentState.RUNNING
        
        return try {
            // 记录用户输入到记忆
            memory.addMemory(mapOf("role" to "user", "content" to userInput))
            val prompt = buildPrompt()
            val modelOutput = model.generateText(prompt)
            
            // 校验模型输出合法性
            if (modelOutput == null) {
                println("Error: Model returned invalid output")
                return Result.failure(Exception("Model did not return a valid response."))
            }
            
            var iterations = 0
            var currentOutput = modelOutput
            
            // 将智能体回复写入记忆并返回
            memory.addMemory(mapOf("role" to "assistant", "content" to (currentOutput?.content ?: "")))
            _state.value = AgentState.IDLE
            Result.success(currentOutput?.content ?: "")
            
        } catch (e: Exception) {
            _state.value = AgentState.ERROR
            Result.failure(e)
        }
    }
    
    /**
     * 基于状态机的运行循环：按照 inputIterable（可迭代的用户输入）逐条处理并产出响应
     * 状态包括：INITIALIZING, RUNNING, PAUSED, STOPPING, STOPPED, ERROR
     */
    suspend fun runLoop(
        inputIterable: Iterable<String>,
        stopOnException: Boolean = true
    ): List<String> {
        // 定义状态机的状态常量
        val STATE_INITIALIZING = "initializing"
        val STATE_RUNNING = "running"
        val STATE_PAUSED = "paused"
        val STATE_STOPPING = "stopping"
        val STATE_STOPPED = "stopped"
        val STATE_ERROR = "error"
        
        // 初始化状态机
        var state = STATE_INITIALIZING
        val outputs = mutableListOf<String>()
        running.set(true)
        
        try {
            while (state != STATE_STOPPED && state != STATE_ERROR) {
                when (state) {
                    STATE_INITIALIZING -> {
                        state = STATE_RUNNING
                        continue
                    }
                    
                    STATE_RUNNING -> {
                        try {
                            for (userInput in inputIterable) {
                                if (!running.get()) {
                                    state = STATE_STOPPING
                                    break
                                }
                                
                                try {
                                    val response = runOnce(userInput)
                                    if (response.isSuccess) {
                                        outputs.add(response.getOrThrow())
                                    } else {
                                        val errorMsg = response.exceptionOrNull()?.message ?: "Unknown error"
                                        outputs.add("Agent error: $errorMsg")
                                    }
                                    
                                    // 检查是否有暂停请求
                                    if (!running.get()) {
                                        state = STATE_PAUSED
                                        break
                                    }
                                } catch (e: Exception) {
                                    if (stopOnException) {
                                        state = STATE_ERROR
                                        throw e
                                    }
                                    val response = "Agent error: ${e.message}"
                                    outputs.add(response)
                                }
                            }
                            
                            // 正常完成所有输入处理
                            if (state == STATE_RUNNING) {
                                state = STATE_STOPPING
                            }
                        } catch (e: Exception) {
                            if (stopOnException) {
                                state = STATE_ERROR
                                throw e
                            }
                            state = STATE_STOPPING
                        }
                    }
                    
                    STATE_PAUSED -> {
                        // 暂停状态，等待恢复信号
                        // 这里可以添加等待逻辑或回调机制
                        state = STATE_STOPPING // 简化处理，直接进入停止状态
                    }
                    
                    STATE_STOPPING -> {
                        state = STATE_STOPPED
                    }
                    
                    STATE_STOPPED -> {
                        break
                    }
                    
                    STATE_ERROR -> {
                        break
                    }
                }
            }
        } finally {
            running.set(false)
            state = STATE_STOPPED
        }
        
        return outputs
    }
    
    /**
     * 停止运行
     */
    fun stop() {
        running.set(false)
    }
    
    /**
     * 暂停运行
     */
    fun pause() {
        running.set(false)
    }
    
    /**
     * 恢复运行
     */
    fun resume() {
        running.set(true)
    }
    
    /**
     * 关闭Agent
     */
    fun close() {
        // Agent关闭，无需特殊处理
        println("Agent '$name' closed")
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


/**
 * 记忆条目
 * 完全对应Python版本的MemoryEntry类
 */
data class MemoryEntry(
    val id: String,
    val content: Map<String, Any>,
    val timestamp: Date = Date(),
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * 上下文记忆
 * 完全对应Python版本的ContextMemory类
 */
class ContextMemory(private val maxMemorySize: Int = 100) {
    private val memories: MutableList<MemoryEntry> = mutableListOf()
    private val memoryIndex: MutableMap<String, Int> = mutableMapOf() // id到索引的映射
    
    /**
     * 添加新的记忆条目
     */
    fun addMemory(
        content: Map<String, Any>,
        memoryId: String? = null,
        metadata: Map<String, Any>? = null
    ): String {
        val id = memoryId ?: UUID.randomUUID().toString() // 使用UUID确保唯一性
        
        // 创建新的记忆条目
        val entry = MemoryEntry(
            id = id,
            content = content,
            timestamp = Date(),
            metadata = metadata ?: emptyMap()
        )
        
        // 如果已达到最大记忆数，移除最旧的记忆
        if (memories.size >= maxMemorySize) {
            val removedEntry = memories.removeAt(0)
            if (removedEntry.id in memoryIndex) {
                memoryIndex.remove(removedEntry.id)
            }
        }
        
        // 添加新记忆
        memories.add(entry)
        memoryIndex[id] = memories.size - 1
        
        return id
    }
    
    /**
     * 根据ID获取特定记忆
     */
    fun getMemory(memoryId: String): MemoryEntry? {
        val index = memoryIndex[memoryId]
        if (index != null && index >= 0 && index < memories.size) {
            val entry = memories[index]
            if (entry.id == memoryId) {
                return entry
            }
        }
        return null
    }
    
    /**
     * 获取最近的记忆条目
     */
    fun getRecentMemories(count: Int = 5): List<MemoryEntry> {
        return if (memories.isNotEmpty()) memories.takeLast(count) else emptyList()
    }
    
    /**
     * 根据关键字搜索记忆
     */
    fun searchMemories(keyword: String): List<MemoryEntry> {
        val results = mutableListOf<MemoryEntry>()
        for (entry in memories) {
            // 在内容和元数据中搜索关键字
            val contentStr = entry.content.toString()
            val metadataStr = entry.metadata.toString()
            
            if (keyword.lowercase() in contentStr.lowercase() || 
                keyword.lowercase() in metadataStr.lowercase()) {
                results.add(entry)
            }
        }
        return results
    }
    
    /**
     * 更新现有记忆
     */
    fun updateMemory(
        memoryId: String,
        content: Map<String, Any>? = null,
        metadata: Map<String, Any>? = null
    ): Boolean {
        val entry = getMemory(memoryId) ?: return false
        
        val updatedEntry = entry.copy(
            content = content ?: entry.content,
            metadata = metadata ?: entry.metadata,
            timestamp = Date()
        )
        
        val index = memoryIndex[memoryId]
        if (index != null && index >= 0 && index < memories.size) {
            memories[index] = updatedEntry
            return true
        }
        return false
    }
    
    /**
     * 删除特定记忆
     */
    fun deleteMemory(memoryId: String): Boolean {
        val index = memoryIndex[memoryId]
        if (index != null && index >= 0 && index < memories.size) {
            val entry = memories[index]
            if (entry.id == memoryId) {
                memories.removeAt(index)
                memoryIndex.remove(memoryId)
                // 更新索引
                rebuildIndex()
                return true
            }
        }
        return false
    }
    
    /**
     * 重建记忆索引
     */
    private fun rebuildIndex() {
        memoryIndex.clear()
        for ((i, entry) in memories.withIndex()) {
            memoryIndex[entry.id] = i
        }
    }
    
    /**
     * 获取所有记忆条目
     */
    fun getAllMemories(): List<MemoryEntry> {
        return memories.toList()
    }
    
    /**
     * 清空所有记忆
     */
    fun clearMemories() {
        memories.clear()
        memoryIndex.clear()
    }
    
    /**
     * 获取记忆条目数量
     */
    fun getMemoryCount(): Int {
        return memories.size
    }
    
    /**
     * 获取上下文信息，用于对话系统
     */
    fun getContext(count: Int = 5): List<Map<String, String>> {
        if (count <= 0) return emptyList()
        
        val recentMemories = getRecentMemories(count)
        return recentMemories.map { entry ->
            entry.content.mapValues { it.value.toString() }
        }
    }
}

/**
 * 用户管理类
 * 完全对应Python版本的UserManager类
 */
class UserManager {
    companion object {
        @Volatile
        private var INSTANCE: UserManager? = null
        
        fun getInstance(): UserManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserManager().also { INSTANCE = it }
            }
        }
    }
    
    private val usersMemory: MutableMap<String, ContextMemory> = mutableMapOf()
    private var currentUserId: String? = null
    private val defaultMemorySize = 100
    private val memoryStoragePath = "user_memories"
    
    init {
        // 创建存储目录
        // TODO: 实现文件系统操作
        // 加载已存在的记忆库
        loadAllMemories()
    }
    
    /**
     * 自动设置当前用户，如果没有用户则创建新用户
     */
    fun autoSetCurrentUser(): String {
        if (currentUserId == null) {
            currentUserId = UUID.randomUUID().toString()
            usersMemory[currentUserId!!] = ContextMemory(defaultMemorySize)
            println("为新用户分配ID: $currentUserId")
        }
        return currentUserId!!
    }
    
    /**
     * 设置当前用户
     */
    fun setCurrentUser(userId: String) {
        currentUserId = userId
        // 如果用户记忆库不存在，则创建
        if (userId !in usersMemory) {
            usersMemory[userId] = ContextMemory(defaultMemorySize)
        }
    }
    
    /**
     * 获取当前用户的记忆库，如果未设置用户则自动创建
     */
    fun getCurrentUserMemory(): ContextMemory? {
        if (currentUserId == null) {
            autoSetCurrentUser()
        }
        return currentUserId?.let { usersMemory[it] }
    }
    
    /**
     * 获取指定用户的记忆库
     */
    fun getUserMemory(userId: String): ContextMemory? {
        return usersMemory[userId]
    }
    
    /**
     * 为用户创建记忆库
     */
    fun createUserMemory(userId: String, maxMemorySize: Int = 100): ContextMemory {
        val memory = ContextMemory(maxMemorySize)
        usersMemory[userId] = memory
        return memory
    }
    
    /**
     * 切换当前用户，如果未提供用户ID则自动创建新用户
     */
    fun switchUser(userId: String? = null): ContextMemory {
        val finalUserId = userId ?: autoSetCurrentUser()
        setCurrentUser(finalUserId)
        return usersMemory[finalUserId]!!
    }
    
    /**
     * 保存指定用户的记忆库到本地文件
     */
    fun saveUserMemory(userId: String) {
        // TODO: 实现文件保存逻辑
        println("保存用户 $userId 的记忆库")
    }
    
    /**
     * 从本地文件加载指定用户的记忆库
     */
    fun loadUserMemory(userId: String): ContextMemory? {
        // TODO: 实现文件加载逻辑
        println("加载用户 $userId 的记忆库")
        return null
    }
    
    /**
     * 加载所有用户的记忆库
     */
    fun loadAllMemories() {
        // TODO: 实现批量加载逻辑
        println("加载所有用户的记忆库")
    }
    
    /**
     * 保存所有用户的记忆库到本地文件
     */
    fun saveAllMemories() {
        for (userId in usersMemory.keys) {
            saveUserMemory(userId)
        }
    }
    
    /**
     * 获取当前用户ID，如果没有设置则自动创建新用户
     */
    fun getOrCreateUser(): String {
        if (currentUserId == null) {
            // 自动生成新的用户ID
            currentUserId = UUID.randomUUID().toString()
            // 为新用户创建记忆库
            usersMemory[currentUserId!!] = ContextMemory(defaultMemorySize)
            println("为新用户分配ID: $currentUserId")
        }
        return currentUserId!!
    }
}