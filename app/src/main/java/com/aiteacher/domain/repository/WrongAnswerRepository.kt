package com.aiteacher.domain.repository

import com.aiteacher.domain.model.WrongAnswer

/**
 * 错题记录数据仓库接口
 */
interface WrongAnswerRepository {
    suspend fun getAllWrongAnswers(): List<WrongAnswer>
    suspend fun getWrongAnswerById(id: Long): WrongAnswer?
    suspend fun getWrongAnswersByStudentId(studentId: String): List<WrongAnswer>
    suspend fun getWrongAnswersByQuestionId(questionId: String): List<WrongAnswer>
    suspend fun insertWrongAnswer(wrongAnswer: WrongAnswer)
    suspend fun updateWrongAnswer(wrongAnswer: WrongAnswer)
    suspend fun deleteWrongAnswer(id: Long)
    suspend fun deleteWrongAnswersByStudentId(studentId: String)
}