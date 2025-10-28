package com.aiteacher.domain.usecase

import com.aiteacher.domain.model.*
import com.aiteacher.data.local.repository.TeachingTaskRepository

/**
 * 教学任务用例
 * 教学Agent的业务逻辑
 */
class TeachingTaskUseCase(
    private val teachingTaskRepository: TeachingTaskRepository
) {
    
    /**
     * 创建教学任务
     */
    suspend fun createTeachingTask(
        planId: String,
        day: Int
    ): Result<TeachingTask> {
        return try {
            val task = TeachingTask(
                taskId = generateTaskId(),
                planId = planId,
                day = day,
                date = "2025-11-01",
                title = "代数基础概念",
                description = "学习代数的基本概念和表达式",
                topics = listOf("代数表达式", "变量与常量", "基本运算"),
                relatedKnowledge = listOf(
                    KnowledgeItem(
                        knowledgeId = "M7-001",
                        topic = "代数表达式",
                        subject = "数学"
                    ),
                    KnowledgeItem(
                        knowledgeId = "M7-002",
                        topic = "变量与常量",
                        subject = "数学"
                    )
                ),
                estimatedTime = 60,
                content = "今天我们将学习代数的基本概念...",
                resources = listOf(
                    LearningResource(
                        type = "video",
                        url = "https://example.com/video/1",
                        title = "代数基础讲解视频"
                    ),
                    LearningResource(
                        type = "document",
                        url = "https://example.com/doc/1",
                        title = "代数基础讲义"
                    )
                ),
                completed = false,
                completionDate = null,
                grade = 0,
                maxGrade = 100,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
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
                date = "2025-11-01",
                title = "代数基础概念",
                description = "学习代数的基本概念和表达式",
                topics = listOf("代数表达式", "变量与常量", "基本运算"),
                relatedKnowledge = listOf(
                    KnowledgeItem(
                        knowledgeId = "M7-001",
                        topic = "代数表达式",
                        subject = "数学"
                    )
                ),
                estimatedTime = 60,
                content = "开始教学知识点 M7-001",
                resources = emptyList(),
                completed = false,
                completionDate = null,
                grade = 0,
                maxGrade = 100,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
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
    suspend fun completeTeachingTask(taskId: String): Result<Unit> {
        return try {
            // 检查数据库是否存在taskId指向的任务
            val taskResult = startTeachingTask(taskId)
            if (taskResult.isFailure) {
                return Result.failure(Exception("无法获取任务信息"))
            }
            
            val task = taskResult.getOrNull()
            if (task == null) {
                return Result.failure(Exception("任务不存在"))
            }
            val completedTask = task.copy(completed = true, completionDate = System.currentTimeMillis().toString())
            teachingTaskRepository.updateTeachingTask(completedTask)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据计划ID获取未完成的教学任务
     */
    suspend fun getIncompleteTeachingTasksByPlanId(planId: String): Result<List<TeachingTask>> {
        return try {
            // 这里应该从数据库获取未完成的任务
            // 暂时返回空列表
            Result.success(emptyList())
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