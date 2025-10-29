package com.aiteacher.data.local.repository

import com.aiteacher.data.local.dao.QuestionDao
import com.aiteacher.data.local.entity.QuestionEntity
import com.aiteacher.domain.model.Question

/**
 * 题目仓库类
 * 处理题目数据的业务逻辑
 */
class QuestionRepository(private val questionDao: QuestionDao) {
    
    /**
     * 根据ID获取题目
     */
    suspend fun getQuestionById(questionId: String): Result<Question?> {
        return try {
            val question = questionDao.getQuestionById(questionId)
            Result.success(question?.toDomainModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据学科和年级获取题目
     */
    suspend fun getQuestionsBySubjectAndGrade(subject: String, grade: Int): Result<List<Question>> {
        return try {
            val questions = questionDao.getQuestionsBySubjectAndGrade(subject, grade)
            Result.success(questions.map { it.toDomainModel() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据题目类型获取题目
     */
    suspend fun getQuestionsByType(questionType: String): Result<List<Question>> {
        return try {
            val questions = questionDao.getQuestionsByType(questionType)
            Result.success(questions.map { it.toDomainModel() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据难度获取题目
     */
    suspend fun getQuestionsByDifficulty(difficulty: Int): Result<List<Question>> {
        return try {
            val questions = questionDao.getQuestionsByDifficulty(difficulty)
            Result.success(questions.map { it.toDomainModel() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据知识点ID获取题目
     */
    suspend fun getQuestionsByKnowledgeId(knowledgeId: String): Result<List<Question>> {
        return try {
            val questions = questionDao.getQuestionsByKnowledgeId(knowledgeId)
            Result.success(questions.map { it.toDomainModel() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取所有题目
     */
    suspend fun getAllQuestions(): Result<List<Question>> {
        return try {
            val questions = questionDao.getAllQuestions()
            Result.success(questions.map { it.toDomainModel() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 添加题目
     */
    suspend fun insertQuestion(question: Question): Result<Unit> {
        return try {
            val entity = question.toEntity()
            questionDao.insertQuestion(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 批量添加题目
     */
    suspend fun insertAllQuestions(questions: List<Question>): Result<Unit> {
        return try {
            val entities = questions.map { it.toEntity() }
            questionDao.insertAllQuestions(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 更新题目
     */
    suspend fun updateQuestion(question: Question): Result<Unit> {
        return try {
            val entity = question.toEntity()
            questionDao.updateQuestion(entity)
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
    
    /**
     * 搜索题目
     */
    suspend fun searchQuestions(keyword: String): Result<List<Question>> {
        return try {
            val questions = questionDao.searchQuestions(keyword)
            Result.success(questions.map { it.toDomainModel() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据学科、年级和类型获取题目
     */
    suspend fun getQuestionsBySubjectGradeAndType(subject: String, grade: Int, type: String): Result<List<Question>> {
        return try {
            val questions = questionDao.getQuestionsBySubjectGradeAndType(subject, grade, type)
            Result.success(questions.map { it.toDomainModel() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * 扩展函数：QuestionEntity 转 Question
 */
private fun QuestionEntity.toDomainModel(): Question {
    return Question(
        questionId = this.questionId,
        subject = this.subject,
        grade = this.grade,
        questionText = this.questionText,
        answer = this.answer,
        questionType = this.questionType,
        difficulty = this.difficulty,
        relatedKnowledgeIds = this.relatedKnowledgeIds
    )
}

/**
 * 扩展函数：Question 转 QuestionEntity
 */
private fun Question.toEntity(): QuestionEntity {
    return QuestionEntity(
        questionId = this.questionId,
        subject = this.subject,
        grade = this.grade,
        questionText = this.questionText,
        answer = this.answer,
        questionType = this.questionType,
        difficulty = this.difficulty,
        relatedKnowledgeIds = this.relatedKnowledgeIds
    )
}