package com.aiteacher.data.local.repository

import com.aiteacher.data.local.dao.SessionDao
import com.aiteacher.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * 会话数据仓库
 * 处理会话数据的业务逻辑
 */
class SessionRepository(private val sessionDao: SessionDao) {
    
    /**
     * 根据会话ID获取会话信息
     */
    suspend fun getSessionById(sessionId: String): SessionEntity? {
        return sessionDao.getSessionById(sessionId)
    }
    
    /**
     * 根据会话ID获取会话信息（Flow版本）
     */
    fun getSessionByIdFlow(sessionId: String): Flow<SessionEntity?> {
        return sessionDao.getSessionByIdFlow(sessionId)
    }
    
    /**
     * 根据用户ID获取会话列表
     */
    suspend fun getSessionsByUserId(userId: String): List<SessionEntity> {
        return sessionDao.getSessionsByUserId(userId)
    }
    
    /**
     * 根据用户ID获取会话列表（Flow版本）
     */
    fun getSessionsByUserIdFlow(userId: String): Flow<List<SessionEntity>> {
        return sessionDao.getSessionsByUserIdFlow(userId)
    }
    
    /**
     * 获取所有会话
     */
    suspend fun getAllSessions(): List<SessionEntity> {
        return sessionDao.getAllSessions()
    }
    
    /**
     * 获取所有会话（Flow版本）
     */
    fun getAllSessionsFlow(): Flow<List<SessionEntity>> {
        return sessionDao.getAllSessionsFlow()
    }
    
    /**
     * 插入或更新会话
     */
    suspend fun saveSession(session: SessionEntity) {
        sessionDao.insertSession(session)
    }
    
    /**
     * 更新会话
     */
    suspend fun updateSession(session: SessionEntity) {
        sessionDao.updateSession(session)
    }
    
    /**
     * 删除会话
     */
    suspend fun deleteSession(sessionId: String) {
        sessionDao.deleteSessionById(sessionId)
    }
    
    /**
     * 根据用户ID删除会话
     */
    suspend fun deleteSessionsByUserId(userId: String) {
        sessionDao.deleteSessionsByUserId(userId)
    }
}