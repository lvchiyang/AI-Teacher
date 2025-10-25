package com.aiteacher.domain.model

/**
 * 教学计划领域模型
 */
data class TeachingPlan(
    val planId: String,
    val studentId: String,
    val title: String,
    val description: String,
    val subject: String,
    val gradeLevel: String,
    val totalDays: Int,
    val startDate: String,
    val endDate: String,
    val status: String, // active, completed, paused
    val totalTasks: Int = 0, // 计划下的任务总个数
    val completedTasks: Int = 0, // 已完成任务数
    val createdAt: Long,
    val updatedAt: Long,
    val tags: List<String> = emptyList()
)