package com.aiteacher.ai.service

import com.aiteacher.data.local.dao.MessageDao
import com.aiteacher.data.local.dao.SessionDao
import com.aiteacher.data.local.dao.UserDao
import com.aiteacher.data.local.entity.MessageEntity
import com.aiteacher.data.local.entity.SessionEntity
import com.aiteacher.data.local.entity.UserEntity
import com.aiteacher.data.local.entity.UserType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import android.util.Log
import java.util.Date
import java.util.UUID

class MemoryManager : KoinComponent {

    private var sessionId: String = "default"
    private val contextMemory = ContextMemory()
    
    // 使用Koin依赖注入获取DAO实例
    private val sessionDao: SessionDao by inject()
    private val messageDao: MessageDao by inject()
    private val userDao: UserDao by inject()
    
    private var messageObserverJob: Job? = null
    private var currentUserId: String? = null

    fun setSessionId(sessionId: String) {
        this.sessionId = sessionId
    }
    
    fun setUserId(userId: String) {
        this.currentUserId = userId
    }

    suspend fun initialize(userId: String, sessionId: String) {
        this.currentUserId = userId
        this.sessionId = sessionId
        ensureUserAndSession(userId, sessionId)
    }
    
    /**
     * 确保用户和会话存在，如果不存在则自动创建
     */
    private suspend fun ensureUserAndSession(userId: String, sessionId: String) {
        // 1. 确保用户存在
        var user = userDao.getUserById(userId)
        if (user == null) {
            // 创建默认用户（学生类型）
            user = UserEntity(
                userId = userId,
                userType = UserType.STUDENT,
                studentId = userId  // 默认使用 userId 作为 studentId
            )
            userDao.insertUser(user)
            Log.d("MemoryManager", "自动创建用户: $userId")
        }
        
        // 2. 检查会话是否存在，如果不存在则创建一个新的会话
        var session = sessionDao.getSessionById(sessionId)
        if (session == null) {
            // 创建新的会话
            session = SessionEntity(
                sessionId = sessionId,
                userId = userId,
                title = "新会话",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            sessionDao.insertSession(session)
            Log.d("MemoryManager", "自动创建会话: $sessionId, userId: $userId")
        }
    }
    
    fun observeSessionMessages(sessionId: String, coroutineScope: CoroutineScope) {
        // 取消之前的监听任务（如果存在）
        messageObserverJob?.cancel()
        
        // 启动一个新的协程来监听数据库变化
        messageObserverJob = coroutineScope.launch(Dispatchers.IO) {
            messageDao.getMessagesBySessionIdFlow(sessionId).collect { messages ->
                // 直接替换所有消息，提高效率
                contextMemory.replace(messages.map { toMemoryEntry(it) })
            }
        }
    }
    
    // 将MessageEntity转换为MemoryEntry的辅助函数
    private fun toMemoryEntry(message: MessageEntity): MemoryEntry {
        // 构建符合 API 格式的消息对象（可以直接 JSON 序列化）
        val contentMap = mutableMapOf<String, Any>(
            "role" to message.role,
            "content" to message.content
        )
        
        // 如果是 assistant 消息且有 tool_calls，从 metadata 中提取并添加
        if (message.role == "assistant" && message.metadata.containsKey("tool_calls_json")) {
            try {
                val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                val toolCallsJson = message.metadata["tool_calls_json"]
                if (toolCallsJson != null) {
                    val toolCalls = json.parseToJsonElement(toolCallsJson) as? kotlinx.serialization.json.JsonArray
                    if (toolCalls != null) {
                        // 转换为 List<Map<String, Any>>
                        val toolCallsList = toolCalls.map { element ->
                            val obj = element as? kotlinx.serialization.json.JsonObject ?: kotlinx.serialization.json.buildJsonObject { }
                            obj.entries.associate { (k, v) ->
                                k to when (v) {
                                    is kotlinx.serialization.json.JsonPrimitive -> {
                                        val content = v.content
                                        when {
                                            content == "true" || content == "false" -> content == "true"
                                            content.toDoubleOrNull() != null -> content.toDouble()
                                            content.toLongOrNull() != null -> content.toLong()
                                            else -> content
                                        }
                                    }
                                    is kotlinx.serialization.json.JsonObject -> v.entries.associate { (k2, v2) ->
                                        k2 to (if (v2 is kotlinx.serialization.json.JsonPrimitive) v2.content else v2.toString())
                                    }
                                    is kotlinx.serialization.json.JsonArray -> v.map { if (it is kotlinx.serialization.json.JsonPrimitive) it.content else it.toString() }
                                }
                            }
                        }
                        contentMap["tool_calls"] = toolCallsList
                    }
                }
            } catch (e: Exception) {
                Log.w("MemoryManager", "解析 tool_calls 失败: ${e.message}")
            }
        }
        
        // 如果是 tool 消息，从 metadata 中提取 tool_call_id 和 tool_name
        if (message.role == "tool" && message.metadata.isNotEmpty()) {
            message.metadata["tool_call_id"]?.let {
                contentMap["tool_call_id"] = it
            }
            message.metadata["tool_name"]?.let {
                contentMap["name"] = it
            }
        }
        
        return MemoryEntry(
            id = message.messageId,
            content = contentMap,
            timestamp = Date(message.createdAt),
            metadata = message.metadata
        )
    }
    
    /**
     * 将新的消息插入到数据库中
     * @param role 消息角色（如"user"或"assistant"）
     * @param content 消息内容
     * @param tokens 消息的token数量
     * @param metadata 消息的元数据
     * @return 插入的消息ID
     */
    suspend fun insertMessage(
        role: String,
        content: String,
        tokens: Int = 0,
        metadata: Map<String, String> = emptyMap()
    ): String {
        // 确保用户和会话存在（使用默认值或当前设置的值）
        val userId = currentUserId ?: "default_user"
        ensureUserAndSession(userId, sessionId)
        
        // 生成唯一的消息ID
        val messageId = UUID.randomUUID().toString()
        
        // 创建消息实体
        val messageEntity = MessageEntity(
            messageId = messageId,
            sessionId = sessionId,
            role = role,
            content = content,
            tokens = tokens,
            createdAt = System.currentTimeMillis(),
            metadata = metadata
        )
        
        // 插入到数据库
        messageDao.insertMessage(messageEntity)
        
        // 更新内存中的消息
        val memoryEntry = toMemoryEntry(messageEntity)
        contextMemory.addEntry(memoryEntry)
        
        return messageId
    }

    /**
     * 获取会话的记忆
     * 返回的消息格式：可以直接用于 JSON 序列化
     * - 普通消息：{"role": "user/assistant", "content": "..."}
     * - Tool 消息：{"role": "tool", "content": "...", "tool_call_id": "...", "name": "..."}
     * 
     * 这个格式直接符合 OpenAI/DashScope API 的 messages 字段要求，无需再次转换
     */
    fun getMemory(n: Int? = null): List<Map<String, Any>> {
        return contextMemory.getContext(n).map { entry ->
            // entry.content 已经是符合 API 格式的 Map<String, Any>
            // 包含 role, content, 以及 tool 消息的 tool_call_id 和 name
            entry.content
        }
    }

}

/**
 * 记忆条目
 */
data class MemoryEntry(
    val id: String,
    val content: Map<String, Any>,
    val timestamp: Date = Date(),
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * 上下文记忆
 */
class ContextMemory(private val maxMemorySize: Int = 100) {
    private val memories = mutableListOf<MemoryEntry>()

    /**
     * 添加记忆条目
     */
    fun addEntry(entry: MemoryEntry) {
        memories.add(entry)
        // 保持记忆大小在限制范围内
        while (memories.size > maxMemorySize) {
            memories.removeAt(0)
        }
    }

    /**
     * 获取上下文
     */
    fun getContext(n: Int? = null): List<MemoryEntry> {
        if (n != null) {
            return memories.takeLast(n)
        } else {
            return memories.toList()
        }
    }

    /**
     * 获取记忆数量
     */
    fun getMemoryCount(): Int = memories.size

    /**
     * 清空记忆
     */
    fun clear() {
        memories.clear()
    }

    /**
     * 获取所有记忆
     */
    fun getAllMemories(): List<MemoryEntry> = memories.toList()
    
    /**
     * 替换所有记忆条目，提高批量更新效率
     */
    fun replace(entries: List<MemoryEntry>) {
        memories.clear()
        memories.addAll(entries.takeLast(maxMemorySize))
    }
}