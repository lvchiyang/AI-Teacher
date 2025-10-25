package com.aiteacher.domain.repository

import com.aiteacher.domain.model.Question

/**
 * 题目数据仓库接口
 */
interface QuestionRepository {
    suspend fun getAllQuestions(): List<Question>
    suspend fun getQuestionById(questionId: String): Question?
    suspend fun getQuestionsBySubjectAndGrade(subject: String, grade: Int): List<Question>
    suspend fun getQuestionsByKnowledgeId(knowledgeId: String): List<Question>
    suspend fun insertQuestion(question: Question)
    suspend fun updateQuestion(question: Question)
    suspend fun deleteQuestion(questionId: String)
}