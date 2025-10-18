package com.aiteacher.ai.agent

import com.aiteacher.ai.service.LLMModel
import com.aiteacher.ai.service.LLMOutput
import com.aiteacher.ai.mcp.MCPClientManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.mutableListOf

/**
 * �����������
 * ��ȫ��ӦPython�汾��base_agent��
 * - ʹ�� llm_model ��Ϊ��������ģ��
 * - ֧����Χ����ע������ã�base_tool��
 * - ���������ڣ������û����� -> ����ģ�� -> ��ģ��Ҫ����ù�����ִ�й��� -> �����߽��������ģ�� -> ����������Ӧ
 */
abstract class BaseAgent(
    val name: String,
    description: String? = null,
    protected val model: LLMModel = LLMModel("qwen-max"),
    private val configFilePath: String = "app/src/main/java/com/aiteacher/ai/mcp/server/mcp.json", // Ĭ�������ļ�·��
    memory: ContextMemory = UserManager().getCurrentUserMemory() ?: ContextMemory(maxMemorySize = 20),
    maxToolIterations: Int = 3
) {
    val description: String = description ?: "An intelligent agent named $name capable of using tools and maintaining conversation context"
    
    // Agent���õĹ����б� - ���ڴ�MCPClientManager��̬��ȡ
    protected val availableTools: List<String> = emptyList() // ����init�д�MCPClientManager��ȡ
    protected val memory: ContextMemory = memory
    protected val maxToolIterations: Int = maxOf(1, minOf(maxToolIterations, 10)) // �����ں���Χ��
    protected val running: AtomicBoolean = AtomicBoolean(false)
    
    // MCP Client������ - ÿ��Agentά���Լ���Client����
    private val mcpClientManager = MCPClientManager(name, configFilePath)
    
    init {
        // �����ù���ע�ᵽLLMģ����
        kotlinx.coroutines.runBlocking {
            mcpClientManager.start()
            val toolSpecs = mcpClientManager.getToolSpecs()
            toolSpecs.forEach { spec ->
                model.addTool(spec)
            }
        }
    }
    
    // ϵͳ��ʾͷ
    protected val promptHead: Map<String, String> = mapOf(
        "role" to "system",
        "content" to """You are $name, an intelligent assistant with access to various tools.
You can use these tools when needed to accomplish tasks. Always follow these guidelines:
1. Use tools only when necessary
2. When using a tool, provide clear and complete parameters
3. After receiving tool results, incorporate them into your response appropriately
4. If a task cannot be completed, explain why clearly"""
    )
    
    // ״̬����
    private val _state = MutableStateFlow(AgentState.IDLE)
    val state: StateFlow<AgentState> = _state.asStateFlow()
    
    
    /**
     * ����MCP����
     */
    suspend fun callTool(toolName: String, kwargs: Map<String, Any> = emptyMap()): Any {
        if (!mcpClientManager.isToolAvailable(toolName)) {
            throw IllegalArgumentException("Tool '$toolName' is not available for this agent")
        }
        
        // ͨ��MCPClientManager���ù���
        return mcpClientManager.callTool(toolName, kwargs)
    }
    
    /**
     * ���ݼ���͹�����Ϣ�����ύ��ģ�͵� prompt
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
     * �������У�
     * - ���� prompt�����������ģ�
     * - ����ģ��
     * - ���ģ�����󹤾ߵ��ã�ִ�й��߲������������ģ�ͣ������� maxToolIterations ��
     * - ���������ı���Ӧ
     */
    suspend fun runOnce(userInput: String): Result<String> {
        // �����Լ������������
        if (maxToolIterations <= 0) {
            println("Warning: max_tool_iterations should be positive integer.")
            return Result.failure(IllegalArgumentException("Invalid max_tool_iterations setting."))
        }
        
        _state.value = AgentState.RUNNING
        
        return try {
            // ��¼�û����뵽����
            memory.addMemory(mapOf("role" to "user", "content" to userInput))
            val prompt = buildPrompt()
            val modelOutput = model.generateText(prompt)
            
            // У��ģ������Ϸ���
            if (modelOutput == null) {
                println("Error: Model returned invalid output")
                return Result.failure(Exception("Model did not return a valid response."))
            }
            
            var iterations = 0
            var currentOutput = modelOutput
            
            // ѭ������ģ����������Ƿ���Ҫ���ߵ���
            while (iterations < maxToolIterations) {
                memory.addMemory(mapOf("role" to "assistant", "content" to currentOutput.content))
                val toolCalls = model.parseToolCall(currentOutput)
                
                if (toolCalls == null || toolCalls.isEmpty()) {
                    break
                }
                
                var hasError = false
                for (call in toolCalls) {
                    val toolName = call["name"] as? String ?: continue
                    val toolArguments = call["arguments"] as? Map<String, Any> ?: emptyMap()
                    
                    try {
                        val result = callTool(toolName, toolArguments)
                        memory.addMemory(mapOf(
                            "role" to "tool",
                            "name" to toolName,
                            "status" to "success",
                            "content" to result.toString()
                        ))
                    } catch (e: Exception) {
                        hasError = true
                        val errorMsg = e.message ?: "Unknown error"
                        memory.addMemory(mapOf(
                            "role" to "tool",
                            "name" to toolName,
                            "status" to "error",
                            "content" to errorMsg
                        ))
                        println("Warning: Tool '$toolName' failed with error: $errorMsg")
                    }
                }
                
                // �ѹ������д����䲢������ģ���Ա��������ջش�
                val followupPrompt = buildPrompt()
                val followupOutput = model.generateText(followupPrompt)
                
                // �ٴ���֤ģ�������Ч��
                if (followupOutput == null) {
                    println("Warning: Model returned invalid output during iteration.")
                    break
                }
                
                currentOutput = followupOutput
                iterations++
            }
            
            // �����������ջظ�д����䲢����
            memory.addMemory(mapOf("role" to "assistant", "content" to currentOutput.content))
            _state.value = AgentState.IDLE
            Result.success(currentOutput.content)
            
        } catch (e: Exception) {
            _state.value = AgentState.ERROR
            Result.failure(e)
        }
    }
    
    /**
     * ����״̬��������ѭ�������� inputIterable���ɵ������û����룩��������������Ӧ
     * ״̬������INITIALIZING, RUNNING, PAUSED, STOPPING, STOPPED, ERROR
     */
    suspend fun runLoop(
        inputIterable: Iterable<String>,
        stopOnException: Boolean = true
    ): List<String> {
        // ����״̬����״̬����
        val STATE_INITIALIZING = "initializing"
        val STATE_RUNNING = "running"
        val STATE_PAUSED = "paused"
        val STATE_STOPPING = "stopping"
        val STATE_STOPPED = "stopped"
        val STATE_ERROR = "error"
        
        // ��ʼ��״̬��
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
                                    
                                    // ����Ƿ�����ͣ����
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
                            
                            // ��������������봦��
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
                        // ��ͣ״̬���ȴ��ָ��ź�
                        // ���������ӵȴ��߼���ص�����
                        state = STATE_STOPPING // �򻯴���ֱ�ӽ���ֹͣ״̬
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
     * ֹͣ����
     */
    fun stop() {
        running.set(false)
    }
    
    /**
     * ��ͣ����
     */
    fun pause() {
        running.set(false)
    }
    
    /**
     * �ָ�����
     */
    fun resume() {
        running.set(true)
    }
    
    /**
     * �ر�Agent������MCP����
     */
    fun close() {
        kotlinx.coroutines.runBlocking {
            mcpClientManager.close()
        }
    }
}

/**
 * Agent״̬
 */
enum class AgentState {
    IDLE,
    RUNNING,
    ERROR
}


/**
 * ������Ŀ
 * ��ȫ��ӦPython�汾��MemoryEntry��
 */
data class MemoryEntry(
    val id: String,
    val content: Map<String, Any>,
    val timestamp: Date = Date(),
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * �����ļ���
 * ��ȫ��ӦPython�汾��ContextMemory��
 */
class ContextMemory(private val maxMemorySize: Int = 100) {
    private val memories: MutableList<MemoryEntry> = mutableListOf()
    private val memoryIndex: MutableMap<String, Int> = mutableMapOf() // id��������ӳ��
    
    /**
     * ����µļ�����Ŀ
     */
    fun addMemory(
        content: Map<String, Any>,
        memoryId: String? = null,
        metadata: Map<String, Any>? = null
    ): String {
        val id = memoryId ?: UUID.randomUUID().toString() // ʹ��UUIDȷ��Ψһ��
        
        // �����µļ�����Ŀ
        val entry = MemoryEntry(
            id = id,
            content = content,
            timestamp = Date(),
            metadata = metadata ?: emptyMap()
        )
        
        // ����Ѵﵽ�����������Ƴ���ɵļ���
        if (memories.size >= maxMemorySize) {
            val removedEntry = memories.removeAt(0)
            if (removedEntry.id in memoryIndex) {
                memoryIndex.remove(removedEntry.id)
            }
        }
        
        // ����¼���
        memories.add(entry)
        memoryIndex[id] = memories.size - 1
        
        return id
    }
    
    /**
     * ����ID��ȡ�ض�����
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
     * ��ȡ����ļ�����Ŀ
     */
    fun getRecentMemories(count: Int = 5): List<MemoryEntry> {
        return if (memories.isNotEmpty()) memories.takeLast(count) else emptyList()
    }
    
    /**
     * ���ݹؼ�����������
     */
    fun searchMemories(keyword: String): List<MemoryEntry> {
        val results = mutableListOf<MemoryEntry>()
        for (entry in memories) {
            // �����ݺ�Ԫ�����������ؼ���
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
     * �������м���
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
     * ɾ���ض�����
     */
    fun deleteMemory(memoryId: String): Boolean {
        val index = memoryIndex[memoryId]
        if (index != null && index >= 0 && index < memories.size) {
            val entry = memories[index]
            if (entry.id == memoryId) {
                memories.removeAt(index)
                memoryIndex.remove(memoryId)
                // ��������
                rebuildIndex()
                return true
            }
        }
        return false
    }
    
    /**
     * �ؽ���������
     */
    private fun rebuildIndex() {
        memoryIndex.clear()
        for ((i, entry) in memories.withIndex()) {
            memoryIndex[entry.id] = i
        }
    }
    
    /**
     * ��ȡ���м�����Ŀ
     */
    fun getAllMemories(): List<MemoryEntry> {
        return memories.toList()
    }
    
    /**
     * ������м���
     */
    fun clearMemories() {
        memories.clear()
        memoryIndex.clear()
    }
    
    /**
     * ��ȡ������Ŀ����
     */
    fun getMemoryCount(): Int {
        return memories.size
    }
    
    /**
     * ��ȡ��������Ϣ�����ڶԻ�ϵͳ
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
 * �û�������
 * ��ȫ��ӦPython�汾��UserManager��
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
        // �����洢Ŀ¼
        // TODO: ʵ���ļ�ϵͳ����
        // �����Ѵ��ڵļ����
        loadAllMemories()
    }
    
    /**
     * �Զ����õ�ǰ�û������û���û��򴴽����û�
     */
    fun autoSetCurrentUser(): String {
        if (currentUserId == null) {
            currentUserId = UUID.randomUUID().toString()
            usersMemory[currentUserId!!] = ContextMemory(defaultMemorySize)
            println("Ϊ���û�����ID: $currentUserId")
        }
        return currentUserId!!
    }
    
    /**
     * ���õ�ǰ�û�
     */
    fun setCurrentUser(userId: String) {
        currentUserId = userId
        // ����û�����ⲻ���ڣ��򴴽�
        if (userId !in usersMemory) {
            usersMemory[userId] = ContextMemory(defaultMemorySize)
        }
    }
    
    /**
     * ��ȡ��ǰ�û��ļ���⣬���δ�����û����Զ�����
     */
    fun getCurrentUserMemory(): ContextMemory? {
        if (currentUserId == null) {
            autoSetCurrentUser()
        }
        return currentUserId?.let { usersMemory[it] }
    }
    
    /**
     * ��ȡָ���û��ļ����
     */
    fun getUserMemory(userId: String): ContextMemory? {
        return usersMemory[userId]
    }
    
    /**
     * Ϊ�û����������
     */
    fun createUserMemory(userId: String, maxMemorySize: Int = 100): ContextMemory {
        val memory = ContextMemory(maxMemorySize)
        usersMemory[userId] = memory
        return memory
    }
    
    /**
     * �л���ǰ�û������δ�ṩ�û�ID���Զ��������û�
     */
    fun switchUser(userId: String? = null): ContextMemory {
        val finalUserId = userId ?: autoSetCurrentUser()
        setCurrentUser(finalUserId)
        return usersMemory[finalUserId]!!
    }
    
    /**
     * ����ָ���û��ļ���⵽�����ļ�
     */
    fun saveUserMemory(userId: String) {
        // TODO: ʵ���ļ������߼�
        println("�����û� $userId �ļ����")
    }
    
    /**
     * �ӱ����ļ�����ָ���û��ļ����
     */
    fun loadUserMemory(userId: String): ContextMemory? {
        // TODO: ʵ���ļ������߼�
        println("�����û� $userId �ļ����")
        return null
    }
    
    /**
     * ���������û��ļ����
     */
    fun loadAllMemories() {
        // TODO: ʵ�����������߼�
        println("���������û��ļ����")
    }
    
    /**
     * ���������û��ļ���⵽�����ļ�
     */
    fun saveAllMemories() {
        for (userId in usersMemory.keys) {
            saveUserMemory(userId)
        }
    }
    
    /**
     * ��ȡ��ǰ�û�ID�����û���������Զ��������û�
     */
    fun getOrCreateUser(): String {
        if (currentUserId == null) {
            // �Զ������µ��û�ID
            currentUserId = UUID.randomUUID().toString()
            // Ϊ���û����������
            usersMemory[currentUserId!!] = ContextMemory(defaultMemorySize)
            println("Ϊ���û�����ID: $currentUserId")
        }
        return currentUserId!!
    }
}