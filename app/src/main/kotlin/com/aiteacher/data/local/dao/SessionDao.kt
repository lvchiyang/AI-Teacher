package com.aiteacher.data.local.dao

import androidx.room.*
import com.aiteacher.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * 会话数据访问对象
 */
@Dao
interface SessionDao {
    
    @Query("SELECT * FROM sessions WHERE sessionId = :sessionId")
    suspend fun getSessionById(sessionId: String): SessionEntity?
    
    @Query("SELECT * FROM sessions WHERE sessionId = :sessionId")
    fun getSessionByIdFlow(sessionId: String): Flow<SessionEntity?>
    
    @Query("SELECT * FROM sessions WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getSessionsByUserId(userId: String): List<SessionEntity>
    
    @Query("SELECT * FROM sessions WHERE userId = :userId ORDER BY createdAt DESC")
    fun getSessionsByUserIdFlow(userId: String): Flow<List<SessionEntity>>
    
    @Query("SELECT * FROM sessions ORDER BY createdAt DESC")
    suspend fun getAllSessions(): List<SessionEntity>
    
    @Query("SELECT * FROM sessions ORDER BY createdAt DESC")
    fun getAllSessionsFlow(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE userId = :userId ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentSessionsByUserId(userId: String, limit: Int): List<SessionEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)
    
    @Update
    suspend fun updateSession(session: SessionEntity)
    
    @Delete
    suspend fun deleteSession(session: SessionEntity)
    
    @Query("DELETE FROM sessions WHERE sessionId = :sessionId")
    suspend fun deleteSessionById(sessionId: String)
    
    @Query("DELETE FROM sessions WHERE userId = :userId")
    suspend fun deleteSessionsByUserId(userId: String)
}