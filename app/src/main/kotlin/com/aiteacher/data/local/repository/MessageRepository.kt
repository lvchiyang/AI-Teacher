package com.aiteacher.data.local.repository

import com.aiteacher.data.local.dao.MessageDao
import com.aiteacher.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 消息数据仓库
 * 处理消息数据的业务逻辑
 */
class MessageRepository(private val messageDao: MessageDao) {

    /**
     * 根据会话ID获取消息列表
     */
    suspend fun getMessagesBySessionId(sessionId: String): Result<List<MessageEntity>> {
        return try {
            val messages = messageDao.getMessagesBySessionId(sessionId)
            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据会话ID获取消息列表（Flow版本）
     */
    fun getMessagesBySessionIdFlow(sessionId: String): Flow<Result<List<MessageEntity>>> {
        return messageDao.getMessagesBySessionIdFlow(sessionId).map { messages ->
            try {
                Result.success(messages)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 根据会话ID和角色获取消息列表
     */
    suspend fun getMessagesBySessionIdAndRole(sessionId: String, role: String): Result<List<MessageEntity>> {
        return try {
            val messages = messageDao.getMessagesBySessionIdAndRole(sessionId, role)
            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 保存消息
     */
    suspend fun saveMessage(message: MessageEntity): Result<Unit> {
        return try {
            messageDao.insertMessage(message)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 批量保存消息
     */
    suspend fun saveMessages(messages: List<MessageEntity>): Result<Unit> {
        return try {
            messageDao.insertMessages(messages)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 更新消息
     */
    suspend fun updateMessage(message: MessageEntity): Result<Unit> {
        return try {
            messageDao.updateMessage(message)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 删除消息
     */
    suspend fun deleteMessage(messageId: String): Result<Unit> {
        return try {
            messageDao.deleteMessageById(messageId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据会话ID删除消息
     */
    suspend fun deleteMessagesBySessionId(sessionId: String): Result<Unit> {
        return try {
            messageDao.deleteMessagesBySessionId(sessionId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取指定数量的最新消息
     */
    suspend fun getLatestMessages(sessionId: String, limit: Int): Result<List<MessageEntity>> {
        return try {
            val messages = messageDao.getLatestMessagesBySessionId(sessionId, limit)
            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}