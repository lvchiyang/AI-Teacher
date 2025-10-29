package com.aiteacher.domain.model

/**
 * 测试任务领域模型
 */
data class TestingTask(
    val taskId: String,
    val studentId: String,
    val title: String,
    val description: String,
    val questionIds: List<String>, // 测试题目的ID列表
    val questions: List<Question>, // 测试题目列表
    val totalScore: Int, // 总分
    val passingScore: Int, // 通过分数
    val timeLimit: Int?, // 时间限制（分钟）
    val startedAt: Long?, // 开始时间
    val completedAt: Long?, // 完成时间
    val score: Int?, // 得分
    val completed: Boolean, // 是否完成
    val createdAt: Long,
    val updatedAt: Long
)