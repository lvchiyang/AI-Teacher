package com.aiteacher.domain.usecase

import com.aiteacher.domain.model.*

/**
 * 教学任务用例
 * 教学Agent的业务逻辑
 */
class TeachingTaskUseCase {
    
    /**
     * 创建教学任务
     */
    suspend fun createTeachingTask(
        planId: String,
        day: Int,
        title: String,
        description: String
    ): Result<TeachingTask> {
        return try {
            val task = TeachingTask(
                taskId = generateTaskId(),
                planId = planId,
                day = day,
                date = currentDateString(),
                title = title,
                description = description,
                topics = emptyList(),
                relatedKnowledge = emptyList(),
                estimatedTime = 30,
                content = "",
                resources = emptyList(),
                completed = false,
                completionDate = null,
                grade = 0,
                maxGrade = 0,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                noResponseCount = 0
            )
            
            Result.success(task)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 开始教学任务
     */
    suspend fun startTeachingTask(taskId: String): Result<TeachingTask> {
        return try {
            // 这里应该从数据库获取任务并更新状态
            // 暂时返回模拟数据
            val task = TeachingTask(
                taskId = taskId,
                planId = "plan_1",
                day = 1,
                date = currentDateString(),
                title = "开始教学",
                description = "描述",
                topics = emptyList(),
                relatedKnowledge = emptyList(),
                estimatedTime = 30,
                content = "",
                resources = emptyList(),
                completed = false,
                completionDate = null,
                grade = 0,
                maxGrade = 0,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                noResponseCount = 0
            )
            
            Result.success(task)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 处理学生回答
     */
    suspend fun handleStudentAnswer(
        taskId: String,
        answer: String
    ): Result<TeachingTaskResult> {
        return try {
            // MVP阶段：简化答案判断逻辑，让教学更容易通过
            val isCorrect = answer.isNotEmpty() && answer.trim().length >= 1
            
            val result = TeachingTaskResult(
                isCorrect = isCorrect,
                feedback = if (isCorrect) "回答正确！继续学习下一个知识点" else "请输入你的答案",
                nextAction = if (isCorrect) "继续下一题" else "重新讲解",
                shouldUpdateProgress = isCorrect
            )
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 完成任务
     */
    suspend fun completeTeachingTask(
        taskId: String,
        planId: String
    ): Result<Unit> {
        return try {
            // MVP简化：暂时不更新数据库，直接返回成功
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 生成任务ID
     */
    private fun generateTaskId(): String {
        return "task_${System.currentTimeMillis()}"
    }

    private fun currentDateString(): String {
        // 简单占位：YYYY-MM-DD
        val now = java.time.LocalDate.now()
        return now.toString()
    }
}

/**
 * 教学任务结果模型
 */
 data class TeachingTaskResult(
    val isCorrect: Boolean,
    val feedback: String,
    val nextAction: String,
    val shouldUpdateProgress: Boolean
 )
