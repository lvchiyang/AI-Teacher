package com.aiteacher.ai.service

import com.aiteacher.data.local.dao.MessageDao
import com.aiteacher.data.local.dao.SessionDao
import com.aiteacher.data.local.entity.MessageEntity
import com.aiteacher.data.local.entity.SessionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Date
import java.util.UUID

class MemoryManager : KoinComponent {

    private var sessionId: String = "default"
    private val contextMemory = ContextMemory()
    
    // 使用Koin依赖注入获取DAO实例
    private val sessionDao: SessionDao by inject()
    private val messageDao: MessageDao by inject()
    
    private var messageObserverJob: Job? = null

    fun setSessionId(sessionId: String) {
        this.sessionId = sessionId
    }

    suspend fun initialize(userId: String, sessionId: String) {
        // 检查会话是否存在，如果不存在则创建一个新的会话
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
        return MemoryEntry(
            id = message.messageId,
            content = mapOf(
                "role" to message.role,
                "content" to message.content
            ),
            timestamp = Date(message.createdAt),
            metadata = message.metadata
        )
    }
    
    /**
     * 将新的消息插入到数据库中
     * @param sessionId 会话ID
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
     */
    fun getMemory(n: Int? = null): List<Map<String, String>> {
        return contextMemory.getContext(n)
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
    fun getContext(n: Int? = null): List<Map<String, String>> {
        if (n != null) {
            val recentMemories = memories.takeLast(n)
            return recentMemories.map { entry ->
                mapOf(
                    "role" to (entry.content["role"] as? String ?: ""),
                    "content" to (entry.content["content"] as? String ?: "")
                )
            }
        } else {
            return memories.map { entry ->
                mapOf(
                    "role" to (entry.content["role"] as? String ?: ""),
                    "content" to (entry.content["content"] as? String ?: "")
                )
            }
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