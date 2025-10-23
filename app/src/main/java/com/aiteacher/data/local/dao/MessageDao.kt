package com.aiteacher.data.local.dao

import androidx.room.*
import com.aiteacher.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

/**
 * 消息数据访问对象
 */
@Dao
interface MessageDao {
    
    @Query("SELECT * FROM messages WHERE messageId = :messageId")
    suspend fun getMessageById(messageId: String): MessageEntity?
    
    @Query("SELECT * FROM messages WHERE messageId = :messageId")
    fun getMessageByIdFlow(messageId: String): Flow<MessageEntity?>
    
    @Query("SELECT * FROM messages WHERE sessionId = :sessionId ORDER BY created_at ASC")
    suspend fun getMessagesBySessionId(sessionId: String): List<MessageEntity>
    
    @Query("SELECT * FROM messages WHERE sessionId = :sessionId ORDER BY created_at ASC")
    fun getMessagesBySessionIdFlow(sessionId: String): Flow<List<MessageEntity>>
    
    @Query("SELECT * FROM messages WHERE sessionId = :sessionId AND role = :role ORDER BY created_at ASC")
    suspend fun getMessagesBySessionIdAndRole(sessionId: String, role: String): List<MessageEntity>
    
    @Query("SELECT * FROM messages ORDER BY created_at ASC")
    suspend fun getAllMessages(): List<MessageEntity>
    
    @Query("SELECT * FROM messages ORDER BY created_at ASC")
    fun getAllMessagesFlow(): Flow<List<MessageEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)
    
    @Update
    suspend fun updateMessage(message: MessageEntity)
    
    @Delete
    suspend fun deleteMessage(message: MessageEntity)
    
    @Query("DELETE FROM messages WHERE messageId = :messageId")
    suspend fun deleteMessageById(messageId: String)
    
    @Query("DELETE FROM messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesBySessionId(sessionId: String)
}