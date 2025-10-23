package com.aiteacher.data.local.dao

import androidx.room.*
import com.aiteacher.data.local.entity.WrongAnswerEntity
import kotlinx.coroutines.flow.Flow

/**
 * 错题记录数据访问对象
 */
@Dao
interface WrongAnswerDao {
    
    @Query("SELECT * FROM wrong_answers WHERE id = :id")
    suspend fun getWrongAnswerById(id: Long): WrongAnswerEntity?
    
    @Query("SELECT * FROM wrong_answers WHERE id = :id")
    fun getWrongAnswerByIdFlow(id: Long): Flow<WrongAnswerEntity?>
    
    @Query("SELECT * FROM wrong_answers WHERE student_id = :studentId ORDER BY created_at DESC")
    suspend fun getWrongAnswersByStudentId(studentId: String): List<WrongAnswerEntity>
    
    @Query("SELECT * FROM wrong_answers WHERE student_id = :studentId ORDER BY created_at DESC")
    fun getWrongAnswersByStudentIdFlow(studentId: String): Flow<List<WrongAnswerEntity>>
    
    @Query("SELECT * FROM wrong_answers WHERE student_id = :studentId AND question_id = :questionId ORDER BY created_at DESC")
    suspend fun getWrongAnswersByStudentIdAndQuestionId(studentId: String, questionId: String): List<WrongAnswerEntity>
    
    @Query("SELECT * FROM wrong_answers WHERE student_id = :studentId AND is_correct = 0 ORDER BY created_at DESC")
    suspend fun getIncorrectAnswersByStudentId(studentId: String): List<WrongAnswerEntity>
    
    @Query("SELECT * FROM wrong_answers WHERE student_id = :studentId AND is_correct = 0 ORDER BY created_at DESC")
    fun getIncorrectAnswersByStudentIdFlow(studentId: String): Flow<List<WrongAnswerEntity>>
    
    @Query("SELECT * FROM wrong_answers ORDER BY created_at DESC")
    suspend fun getAllWrongAnswers(): List<WrongAnswerEntity>
    
    @Query("SELECT * FROM wrong_answers ORDER BY created_at DESC")
    fun getAllWrongAnswersFlow(): Flow<List<WrongAnswerEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWrongAnswer(wrongAnswer: WrongAnswerEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWrongAnswers(wrongAnswers: List<WrongAnswerEntity>): List<Long>
    
    @Update
    suspend fun updateWrongAnswer(wrongAnswer: WrongAnswerEntity)
    
    @Delete
    suspend fun deleteWrongAnswer(wrongAnswer: WrongAnswerEntity)
    
    @Query("DELETE FROM wrong_answers WHERE id = :id")
    suspend fun deleteWrongAnswerById(id: Long)
    
    @Query("DELETE FROM wrong_answers WHERE student_id = :studentId")
    suspend fun deleteWrongAnswersByStudentId(studentId: String)
    
    @Query("DELETE FROM wrong_answers WHERE student_id = :studentId AND question_id = :questionId")
    suspend fun deleteWrongAnswersByStudentIdAndQuestionId(studentId: String, questionId: String)
}