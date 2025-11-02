package com.aiteacher.domain.model

/**
 * 学生领域模型
 */
data class Student(
    val studentId: String,
    val studentName: String,
    var grade: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    var currentTaskId: String? = null,
    var currentChapter: String? = null,  // 当前学习章节
    var learningProgress: LearningProgress? = null

)

data class LearningProgress(
    val notTaught: List<String>,
    val taughtToReview: List<String>,
    val notMastered: List<String>,
    val basicMastery: List<String>,
    val fullMastery: List<String>,
    val lastUpdateTime: String,
)