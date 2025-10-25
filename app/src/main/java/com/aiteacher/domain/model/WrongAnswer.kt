package com.aiteacher.domain.model

/**
 * 错题记录领域模型
 */
data class WrongAnswer(
    val id: Long = 0,
    val studentId: String,
    val questionId: String,
    val studentAnswer: String,
    val isCorrect: Boolean,
    val timeSpent: Int,
    val attempt: Int,
    val createdAt: Long,
    val updatedAt: Long
)