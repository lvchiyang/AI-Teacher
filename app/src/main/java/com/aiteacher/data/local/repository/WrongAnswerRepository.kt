package com.aiteacher.data.local.repository

import com.aiteacher.data.local.dao.WrongAnswerDao
import com.aiteacher.data.local.entity.WrongAnswerEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 错题记录数据仓库
 * 处理错题记录数据的业务逻辑
 */
class WrongAnswerRepository(private val wrongAnswerDao: WrongAnswerDao) {
    
    /**
     * 根据ID获取错题记录
     */
    suspend fun getWrongAnswerById(id: Long): Result<WrongAnswerEntity?> {
        return try {
            val wrongAnswer = wrongAnswerDao.getWrongAnswerById(id)
            Result.success(wrongAnswer)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据ID获取错题记录（Flow版本）
     */
    fun getWrongAnswerByIdFlow(id: Long): Flow<Result<WrongAnswerEntity?>> {
        return wrongAnswerDao.getWrongAnswerByIdFlow(id).map { wrongAnswer ->
            try {
                Result.success(wrongAnswer)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 根据学生ID获取错题记录
     */
    suspend fun getWrongAnswersByStudentId(studentId: String): Result<List<WrongAnswerEntity>> {
        return try {
            val wrongAnswers = wrongAnswerDao.getWrongAnswersByStudentId(studentId)
            Result.success(wrongAnswers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据学生ID获取错题记录（Flow版本）
     */
    fun getWrongAnswersByStudentIdFlow(studentId: String): Flow<Result<List<WrongAnswerEntity>>> {
        return wrongAnswerDao.getWrongAnswersByStudentIdFlow(studentId).map { wrongAnswers ->
            try {
                Result.success(wrongAnswers)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 根据学生ID和题目ID获取错题记录
     */
    suspend fun getWrongAnswersByStudentIdAndQuestionId(studentId: String, questionId: String): Result<List<WrongAnswerEntity>> {
        return try {
            val wrongAnswers = wrongAnswerDao.getWrongAnswersByStudentIdAndQuestionId(studentId, questionId)
            Result.success(wrongAnswers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取学生答错的题目记录
     */
    suspend fun getIncorrectAnswersByStudentId(studentId: String): Result<List<WrongAnswerEntity>> {
        return try {
            val wrongAnswers = wrongAnswerDao.getIncorrectAnswersByStudentId(studentId)
            Result.success(wrongAnswers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取学生答错的题目记录（Flow版本）
     */
    fun getIncorrectAnswersByStudentIdFlow(studentId: String): Flow<Result<List<WrongAnswerEntity>>> {
        return wrongAnswerDao.getIncorrectAnswersByStudentIdFlow(studentId).map { wrongAnswers ->
            try {
                Result.success(wrongAnswers)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 获取所有错题记录
     */
    suspend fun getAllWrongAnswers(): Result<List<WrongAnswerEntity>> {
        return try {
            val wrongAnswers = wrongAnswerDao.getAllWrongAnswers()
            Result.success(wrongAnswers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取所有错题记录（Flow版本）
     */
    fun getAllWrongAnswersFlow(): Flow<Result<List<WrongAnswerEntity>>> {
        return wrongAnswerDao.getAllWrongAnswersFlow().map { wrongAnswers ->
            try {
                Result.success(wrongAnswers)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 保存错题记录
     */
    suspend fun saveWrongAnswer(wrongAnswer: WrongAnswerEntity): Result<Long> {
        return try {
            val id = wrongAnswerDao.insertWrongAnswer(wrongAnswer)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 批量保存错题记录
     */
    suspend fun saveWrongAnswers(wrongAnswers: List<WrongAnswerEntity>): Result<List<Long>> {
        return try {
            val ids = wrongAnswerDao.insertWrongAnswers(wrongAnswers)
            Result.success(ids)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 更新错题记录
     */
    suspend fun updateWrongAnswer(wrongAnswer: WrongAnswerEntity): Result<Unit> {
        return try {
            wrongAnswerDao.updateWrongAnswer(wrongAnswer)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 删除错题记录
     */
    suspend fun deleteWrongAnswer(id: Long): Result<Unit> {
        return try {
            wrongAnswerDao.deleteWrongAnswerById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据学生ID删除错题记录
     */
    suspend fun deleteWrongAnswersByStudentId(studentId: String): Result<Unit> {
        return try {
            wrongAnswerDao.deleteWrongAnswersByStudentId(studentId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据学生ID和题目ID删除错题记录
     */
    suspend fun deleteWrongAnswersByStudentIdAndQuestionId(studentId: String, questionId: String): Result<Unit> {
        return try {
            wrongAnswerDao.deleteWrongAnswersByStudentIdAndQuestionId(studentId, questionId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据时间范围获取错题记录
     */
    suspend fun getWrongAnswersByDateRange(startDate: Long, endDate: Long): Result<List<WrongAnswerEntity>> {
        return try {
            val wrongAnswers = wrongAnswerDao.getWrongAnswersByDateRange(startDate, endDate)
            Result.success(wrongAnswers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取最近的错题记录
     */
    suspend fun getRecentWrongAnswers(limit: Int): Result<List<WrongAnswerEntity>> {
        return try {
            val wrongAnswers = wrongAnswerDao.getRecentWrongAnswers(limit)
            Result.success(wrongAnswers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}