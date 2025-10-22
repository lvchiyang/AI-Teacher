package com.aiteacher.data.local.repository

import com.aiteacher.data.local.dao.QuestionDao
import com.aiteacher.data.local.entity.QuestionEntity

/**
 * 题目仓库类
 * 处理题目数据的业务逻辑
 */
class QuestionRepository(private val questionDao: QuestionDao) {
    
    /**
     * 根据ID获取题目
     */
    suspend fun getQuestionById(questionId: String): QuestionEntity? {
        return questionDao.getQuestionById(questionId)
    }
    
    /**
     * 根据学科和年级获取题目
     */
    suspend fun getQuestionsBySubjectAndGrade(subject: String, grade: Int): List<QuestionEntity> {
        return questionDao.getQuestionsBySubjectAndGrade(subject, grade)
    }
    
    /**
     * 根据题目类型获取题目
     */
    suspend fun getQuestionsByType(questionType: String): List<QuestionEntity> {
        return questionDao.getQuestionsByType(questionType)
    }
    
    /**
     * 根据难度获取题目
     */
    suspend fun getQuestionsByDifficulty(difficulty: Int): List<QuestionEntity> {
        return questionDao.getQuestionsByDifficulty(difficulty)
    }
    
    /**
     * 根据知识点ID获取相关题目
     */
    suspend fun getQuestionsByKnowledgeId(knowledgeId: String): List<QuestionEntity> {
        return questionDao.getQuestionsByKnowledgeId(knowledgeId)
    }
    
    /**
     * 获取所有题目
     */
    suspend fun getAllQuestions(): List<QuestionEntity> {
        return questionDao.getAllQuestions()
    }
    
    /**
     * 添加题目
     */
    suspend fun insertQuestion(question: QuestionEntity): Result<Unit> {
        return try {
            questionDao.insertQuestion(question)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 批量添加题目
     */
    suspend fun insertAllQuestions(questions: List<QuestionEntity>): Result<Unit> {
        return try {
            questionDao.insertAllQuestions(questions)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 更新题目
     */
    suspend fun updateQuestion(question: QuestionEntity): Result<Unit> {
        return try {
            questionDao.updateQuestion(question)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 删除题目
     */
    suspend fun deleteQuestion(questionId: String): Result<Unit> {
        return try {
            questionDao.deleteQuestionById(questionId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}