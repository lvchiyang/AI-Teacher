package com.aiteacher.data.local.dao

import androidx.room.*
import com.aiteacher.data.local.entity.KnowledgeEntity
import kotlinx.coroutines.flow.Flow

/**
 * 知识点数据访问对象
 */
@Dao
interface KnowledgeDao {
    
    @Query("SELECT * FROM knowledge_base WHERE knowledgeId = :knowledgeId")
    suspend fun getKnowledgeById(knowledgeId: String): KnowledgeEntity?
    
    @Query("SELECT * FROM knowledge_base WHERE knowledgeId = :knowledgeId")
    fun getKnowledgeByIdFlow(knowledgeId: String): Flow<KnowledgeEntity?>
    
    @Query("SELECT * FROM knowledge_base WHERE subject = :subject AND grade = :grade")
    suspend fun getKnowledgeBySubjectAndGrade(subject: String, grade: Int): List<KnowledgeEntity>
    
    @Query("SELECT * FROM knowledge_base WHERE chapter = :chapter")
    suspend fun getKnowledgeByChapter(chapter: String): List<KnowledgeEntity>
    
    @Query("SELECT * FROM knowledge_base")
    suspend fun getAllKnowledge(): List<KnowledgeEntity>
    
    @Query("SELECT * FROM knowledge_base")
    fun getAllKnowledgeFlow(): Flow<List<KnowledgeEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKnowledge(knowledge: KnowledgeEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllKnowledge(knowledgeList: List<KnowledgeEntity>)
    
    @Update
    suspend fun updateKnowledge(knowledge: KnowledgeEntity)
    
    @Delete
    suspend fun deleteKnowledge(knowledge: KnowledgeEntity)
    
    @Query("DELETE FROM knowledge_base WHERE knowledgeId = :knowledgeId")
    suspend fun deleteKnowledgeById(knowledgeId: String)
    
    @Query("DELETE FROM knowledge_base")
    suspend fun deleteAllKnowledge()
}