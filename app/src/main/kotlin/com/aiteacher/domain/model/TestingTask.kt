package com.aiteacher.domain.model

/**
 * 测试任务领域模型
 */
data class TestingTask(
    val taskId: String,
    val studentId: String,
    val title: String,
    val description: String,
    val questionIds: List<String> = emptyList(), // 测试题目的ID列表
    val questions: List<Question> = emptyList(), // 测试题目列表
    val totalScore: Int? = 100, // 总分
    val passingScore: Int? = 60, // 通过分数
    val timeLimit: Int? = 60, // 时间限制（分钟）
    val startedAt: Long? =  null, // 开始时间
    val completedAt: Long? =  null, // 完成时间
    val score: Int? = null, // 得分
    val completed: Boolean = false, // 是否完成
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 批改结果
 */
data class GradingResult(
    val questionId: String,   // 题目ID
    val score: Int,           // 学生得分
    val feedback: String      // 反馈意见
)