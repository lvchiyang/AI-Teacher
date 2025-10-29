package com.aiteacher.domain.model

/**
 * 教学任务领域模型
 */
data class TeachingTask(
    val taskId: String,
    val planId: String,
    val day: Int,
    val date: String,
    val title: String,
    val description: String,
    val topics: List<String>,
    val relatedKnowledge: List<KnowledgeItem>,
    val estimatedTime: Int, // 预估学习时间（分钟）
    val content: String,
    val resources: List<LearningResource>,
    val completed: Boolean,
    val completionDate: String?,
    val grade: Int,
    val maxGrade: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val noResponseCount: Int = 0
)