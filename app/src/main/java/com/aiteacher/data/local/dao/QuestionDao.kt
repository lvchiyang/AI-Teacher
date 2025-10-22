package com.aiteacher.data.local.dao

import androidx.room.*
import com.aiteacher.data.local.entity.QuestionEntity
import kotlinx.coroutines.flow.Flow

/**
 * 题目数据访问对象
 */
@Dao
interface QuestionDao {
    
    @Query("SELECT * FROM question_base WHERE questionId = :questionId")
    suspend fun getQuestionById(questionId: String): QuestionEntity?
    
    @Query("SELECT * FROM question_base WHERE questionId = :questionId")
    fun getQuestionByIdFlow(questionId: String): Flow<QuestionEntity?>
    
    @Query("SELECT * FROM question_base WHERE subject = :subject AND grade = :grade")
    suspend fun getQuestionsBySubjectAndGrade(subject: String, grade: Int): List<QuestionEntity>
    
    @Query("SELECT * FROM question_base WHERE question_type = :questionType")
    suspend fun getQuestionsByType(questionType: String): List<QuestionEntity>
    
    @Query("SELECT * FROM question_base WHERE difficulty = :difficulty")
    suspend fun getQuestionsByDifficulty(difficulty: Int): List<QuestionEntity>
    
    @Query("SELECT * FROM question_base WHERE relatedKnowledgeIds LIKE '%' || :knowledgeId || '%'")
    suspend fun getQuestionsByKnowledgeId(knowledgeId: String): List<QuestionEntity>
    
    @Query("SELECT * FROM question_base")
    suspend fun getAllQuestions(): List<QuestionEntity>
    
    @Query("SELECT * FROM question_base")
    fun getAllQuestionsFlow(): Flow<List<QuestionEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllQuestions(questions: List<QuestionEntity>)
    
    @Update
    suspend fun updateQuestion(question: QuestionEntity)
    
    @Delete
    suspend fun deleteQuestion(question: QuestionEntity)
    
    @Query("DELETE FROM question_base WHERE questionId = :questionId")
    suspend fun deleteQuestionById(questionId: String)
    
    @Query("DELETE FROM question_base")
    suspend fun deleteAllQuestions()
}