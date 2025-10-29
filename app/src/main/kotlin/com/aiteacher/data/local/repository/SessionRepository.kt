package com.aiteacher.data.local.repository

import com.aiteacher.data.local.dao.SessionDao
import com.aiteacher.data.local.entity.SessionEntity
import com.aiteacher.domain.model.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 会话数据仓库
 * 处理会话数据的业务逻辑
 */
class SessionRepository(private val sessionDao: SessionDao) {
    
    /**
     * 根据会话ID获取会话信息
     */
    suspend fun getSessionById(sessionId: String): Result<SessionEntity?> {
        return try {
            val session = sessionDao.getSessionById(sessionId)
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据会话ID获取会话信息（Flow版本）
     */
    fun getSessionByIdFlow(sessionId: String): Flow<Result<SessionEntity?>> {
        return sessionDao.getSessionByIdFlow(sessionId).map { session ->
            try {
                Result.success(session)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 根据用户ID获取会话列表
     */
    suspend fun getSessionsByUserId(userId: String): Result<List<SessionEntity>> {
        return try {
            val sessions = sessionDao.getSessionsByUserId(userId)
            Result.success(sessions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据用户ID获取会话列表（Flow版本）
     */
    fun getSessionsByUserIdFlow(userId: String): Flow<Result<List<SessionEntity>>> {
        return sessionDao.getSessionsByUserIdFlow(userId).map { sessions ->
            try {
                Result.success(sessions)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 获取所有会话
     */
    suspend fun getAllSessions(): Result<List<SessionEntity>> {
        return try {
            val sessions = sessionDao.getAllSessions()
            Result.success(sessions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取所有会话（Flow版本）
     */
    fun getAllSessionsFlow(): Flow<Result<List<SessionEntity>>> {
        return sessionDao.getAllSessionsFlow().map { sessions ->
            try {
                Result.success(sessions)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 插入或更新会话
     */
    suspend fun saveSession(session: SessionEntity): Result<Unit> {
        return try {
            sessionDao.insertSession(session)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 更新会话
     */
    suspend fun updateSession(session: SessionEntity): Result<Unit> {
        return try {
            sessionDao.updateSession(session)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 删除会话
     */
    suspend fun deleteSession(sessionId: String): Result<Unit> {
        return try {
            sessionDao.deleteSessionById(sessionId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据用户ID删除会话
     */
    suspend fun deleteSessionsByUserId(userId: String): Result<Unit> {
        return try {
            sessionDao.deleteSessionsByUserId(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据标签获取会话
     */
    suspend fun getSessionsByTag(tag: String): Result<List<SessionEntity>> {
        return try {
            val sessions = sessionDao.getSessionsByTag(tag)
            Result.success(sessions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取最近活跃的会话
     */
    suspend fun getRecentSessions(limit: Int): Result<List<SessionEntity>> {
        return try {
            val sessions = sessionDao.getRecentSessions(limit)
            Result.success(sessions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * 扩展函数：SessionEntity 转 Session
 */
private fun SessionEntity.toDomainModel(): Session {
    return Session(
        sessionId = this.sessionId,
        userId = this.userId,
        title = this.title,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        tags = this.tags
    )
}

/**
 * 扩展函数：Session 转 SessionEntity
 */
private fun Session.toEntity(): SessionEntity {
    return SessionEntity(
        sessionId = this.sessionId,
        userId = this.userId,
        title = this.title,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        tags = this.tags
    )
}