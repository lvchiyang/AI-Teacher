package com.aiteacher.domain.repository

import com.aiteacher.domain.model.Message

/**
 * 消息数据仓库接口
 */
interface MessageRepository {
    suspend fun getAllMessages(): List<Message>
    suspend fun getMessagesBySessionId(sessionId: String): List<Message>
    suspend fun getMessageById(messageId: String): Message?
    suspend fun insertMessage(message: Message)
    suspend fun updateMessage(message: Message)
    suspend fun deleteMessage(messageId: String)
    suspend fun deleteMessagesBySessionId(sessionId: String)
}