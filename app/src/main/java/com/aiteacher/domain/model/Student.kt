package com.aiteacher.domain.model

/**
 * 学生领域模型
 */
data class Student(
    val studentId: String,
    val studentName: String,
    val grade: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)