package com.aiteacher.data.local.repository

import com.aiteacher.data.local.dao.WrongAnswerDao
import com.aiteacher.data.local.entity.WrongAnswerEntity
import kotlinx.coroutines.flow.Flow

/**
 * 错题记录数据仓库
 * 处理错题记录数据的业务逻辑
 */
class WrongAnswerRepository(private val wrongAnswerDao: WrongAnswerDao) {
    
    /**
     * 根据ID获取错题记录
     */
    suspend fun getWrongAnswerById(id: Long): WrongAnswerEntity? {
        return wrongAnswerDao.getWrongAnswerById(id)
    }
    
    /**
     * 根据ID获取错题记录（Flow版本）
     */
    fun getWrongAnswerByIdFlow(id: Long): Flow<WrongAnswerEntity?> {
        return wrongAnswerDao.getWrongAnswerByIdFlow(id)
    }
    
    /**
     * 根据学生ID获取错题记录
     */
    suspend fun getWrongAnswersByStudentId(studentId: String): List<WrongAnswerEntity> {
        return wrongAnswerDao.getWrongAnswersByStudentId(studentId)
    }
    
    /**
     * 根据学生ID获取错题记录（Flow版本）
     */
    fun getWrongAnswersByStudentIdFlow(studentId: String): Flow<List<WrongAnswerEntity>> {
        return wrongAnswerDao.getWrongAnswersByStudentIdFlow(studentId)
    }
    
    /**
     * 根据学生ID和题目ID获取错题记录
     */
    suspend fun getWrongAnswersByStudentIdAndQuestionId(studentId: String, questionId: String): List<WrongAnswerEntity> {
        return wrongAnswerDao.getWrongAnswersByStudentIdAndQuestionId(studentId, questionId)
    }
    
    /**
     * 获取学生答错的题目记录
     */
    suspend fun getIncorrectAnswersByStudentId(studentId: String): List<WrongAnswerEntity> {
        return wrongAnswerDao.getIncorrectAnswersByStudentId(studentId)
    }
    
    /**
     * 获取学生答错的题目记录（Flow版本）
     */
    fun getIncorrectAnswersByStudentIdFlow(studentId: String): Flow<List<WrongAnswerEntity>> {
        return wrongAnswerDao.getIncorrectAnswersByStudentIdFlow(studentId)
    }
    
    /**
     * 获取所有错题记录
     */
    suspend fun getAllWrongAnswers(): List<WrongAnswerEntity> {
        return wrongAnswerDao.getAllWrongAnswers()
    }
    
    /**
     * 获取所有错题记录（Flow版本）
     */
    fun getAllWrongAnswersFlow(): Flow<List<WrongAnswerEntity>> {
        return wrongAnswerDao.getAllWrongAnswersFlow()
    }
    
    /**
     * 保存错题记录
     */
    suspend fun saveWrongAnswer(wrongAnswer: WrongAnswerEntity): Long {
        return wrongAnswerDao.insertWrongAnswer(wrongAnswer)
    }
    
    /**
     * 批量保存错题记录
     */
    suspend fun saveWrongAnswers(wrongAnswers: List<WrongAnswerEntity>): List<Long> {
        return wrongAnswerDao.insertWrongAnswers(wrongAnswers)
    }
    
    /**
     * 更新错题记录
     */
    suspend fun updateWrongAnswer(wrongAnswer: WrongAnswerEntity) {
        wrongAnswerDao.updateWrongAnswer(wrongAnswer)
    }
    
    /**
     * 删除错题记录
     */
    suspend fun deleteWrongAnswer(id: Long) {
        wrongAnswerDao.deleteWrongAnswerById(id)
    }
    
    /**
     * 根据学生ID删除错题记录
     */
    suspend fun deleteWrongAnswersByStudentId(studentId: String) {
        wrongAnswerDao.deleteWrongAnswersByStudentId(studentId)
    }
    
    /**
     * 根据学生ID和题目ID删除错题记录
     */
    suspend fun deleteWrongAnswersByStudentIdAndQuestionId(studentId: String, questionId: String) {
        wrongAnswerDao.deleteWrongAnswersByStudentIdAndQuestionId(studentId, questionId)
    }
}