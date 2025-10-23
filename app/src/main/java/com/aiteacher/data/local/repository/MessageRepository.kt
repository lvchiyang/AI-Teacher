package com.aiteacher.data.local.repository

import com.aiteacher.data.local.dao.MessageDao
import com.aiteacher.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

/**
 * 消息数据仓库
 * 处理消息数据的业务逻辑
 */
class MessageRepository(private val messageDao: MessageDao) {
    
    /**
     * 根据消息ID获取消息
     */
    suspend fun getMessageById(messageId: String): MessageEntity? {
        return messageDao.getMessageById(messageId)
    }
    
    /**
     * 根据消息ID获取消息（Flow版本）
     */
    fun getMessageByIdFlow(messageId: String): Flow<MessageEntity?> {
        return messageDao.getMessageByIdFlow(messageId)
    }
    
    /**
     * 根据会话ID获取消息列表
     */
    suspend fun getMessagesBySessionId(sessionId: String): List<MessageEntity> {
        return messageDao.getMessagesBySessionId(sessionId)
    }
    
    /**
     * 根据会话ID获取消息列表（Flow版本）
     */
    fun getMessagesBySessionIdFlow(sessionId: String): Flow<List<MessageEntity>> {
        return messageDao.getMessagesBySessionIdFlow(sessionId)
    }
    
    /**
     * 根据会话ID和角色获取消息列表
     */
    suspend fun getMessagesBySessionIdAndRole(sessionId: String, role: String): List<MessageEntity> {
        return messageDao.getMessagesBySessionIdAndRole(sessionId, role)
    }
    
    /**
     * 获取所有消息
     */
    suspend fun getAllMessages(): List<MessageEntity> {
        return messageDao.getAllMessages()
    }
    
    /**
     * 获取所有消息（Flow版本）
     */
    fun getAllMessagesFlow(): Flow<List<MessageEntity>> {
        return messageDao.getAllMessagesFlow()
    }
    
    /**
     * 保存消息
     */
    suspend fun saveMessage(message: MessageEntity) {
        messageDao.insertMessage(message)
    }
    
    /**
     * 批量保存消息
     */
    suspend fun saveMessages(messages: List<MessageEntity>) {
        messageDao.insertMessages(messages)
    }
    
    /**
     * 更新消息
     */
    suspend fun updateMessage(message: MessageEntity) {
        messageDao.updateMessage(message)
    }
    
    /**
     * 删除消息
     */
    suspend fun deleteMessage(messageId: String) {
        messageDao.deleteMessageById(messageId)
    }
    
    /**
     * 根据会话ID删除消息
     */
    suspend fun deleteMessagesBySessionId(sessionId: String) {
        messageDao.deleteMessagesBySessionId(sessionId)
    }
}