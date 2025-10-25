package com.aiteacher.domain.repository

import com.aiteacher.domain.model.Session

/**
 * 会话数据仓库接口
 */
interface SessionRepository {
    suspend fun getAllSessions(): List<Session>
    suspend fun getSessionsByUserId(userId: String): List<Session>
    suspend fun getSessionById(sessionId: String): Session?
    suspend fun insertSession(session: Session)
    suspend fun updateSession(session: Session)
    suspend fun deleteSession(sessionId: String)
}