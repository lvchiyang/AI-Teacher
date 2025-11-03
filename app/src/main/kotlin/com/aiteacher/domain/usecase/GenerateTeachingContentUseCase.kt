package com.aiteacher.domain.usecase

import com.aiteacher.ai.agent.TeacherAgent
import com.aiteacher.data.local.repository.TeachingTaskRepository
import com.aiteacher.data.local.entity.TeachingTaskEntity
import com.aiteacher.domain.model.Knowledge
import com.aiteacher.domain.model.KnowledgeItem
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * 生成教学内容用例
 * 实现根据PlanId查找第一个未完成的TeachingTask并调用TeacherAgent生成讲解内容
 */
class GenerateTeachingContentUseCase : KoinComponent {
    
    private val teachingTaskRepository: TeachingTaskRepository by inject()
    private val teacherAgent: TeacherAgent by inject()
    
    /**
     * 根据PlanId查找第一个未完成的TeachingTask并生成讲解内容的参数数据类
     */
    data class Params(
        val planId: String // 教学计划ID
    )
    
    /**
     * 执行生成教学内容的操作
     * @param params 参数
     * @return Result包装的String，表示生成的讲解内容
     */
    suspend operator fun invoke(params: Params): Result<String> {
        return try {
            // 1. 根据PlanId获取未完成的教学任务
            val incompleteTasksResult = teachingTaskRepository.getIncompleteTeachingTasksByPlanId(params.planId)
            
            if (incompleteTasksResult.isFailure) {
                return Result.failure(incompleteTasksResult.exceptionOrNull() ?: Exception("Failed to get incomplete teaching tasks"))
            }
            
            val tasks = incompleteTasksResult.getOrNull() ?: emptyList()
            
            // 2. 获取第一个未完成的任务（按天数排序，应该就是最早的未完成任务）
            val firstIncompleteTask = tasks.firstOrNull()
            
            if (firstIncompleteTask == null) {
                return Result.failure(Exception("No incomplete teaching task found for planId: ${params.planId}"))
            }
            
            // 3. 调用TeacherAgent生成讲解内容
            // 这里假设我们从任务中提取知识点信息
            // 注意：实际实现中可能需要根据任务的具体内容来提取或构建Knowledge对象
            val knowledge = firstIncompleteTask.toKnowledge()
            
            val teachingContentResult = teacherAgent.startTeachingSession(knowledge)
            
            if (teachingContentResult.isFailure) {
                return Result.failure(teachingContentResult.exceptionOrNull() ?: Exception("Failed to generate teaching content"))
            }
            
            val teachingContent = teachingContentResult.getOrNull() ?: ""
            
            // 4. 更新教学任务的内容字段
            val updatedTask = firstIncompleteTask.copy(
                content = teachingContent,
                updatedAt = System.currentTimeMillis()
            )
            
            // 5. 保存更新后的任务到数据库
            teachingTaskRepository.updateTeachingTask(updatedTask)
            
            Result.success(teachingContent)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 继续与教师代理进行教学对话
     * @param userInput 用户输入的内容
     * @return Result包装的String，表示教师代理的回复
     */
    suspend fun continueTeachingSession(userInput: String): Result<String> {
        return try {
            // 使用TeacherAgent的runReAct方法继续对话
            teacherAgent.runReAct(userInput)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 将TeachingTaskEntity转换为Knowledge
     * 这是一个简化的实现，实际应用中可能需要更复杂的逻辑
     */
    private fun TeachingTaskEntity.toKnowledge(): Knowledge {
        // 从relatedKnowledge中提取第一个知识点作为主要知识点
        val firstKnowledgeItem = this.relatedKnowledge.firstOrNull()
        
        return Knowledge(
            knowledgeId = firstKnowledgeItem?.knowledgeId ?: "default_knowledge_id",
            subject = firstKnowledgeItem?.subject ?: "default_subject",
            grade = 7, // 默认年级，实际应用中可能需要从其他地方获取
            chapter = this.title,
            concept = this.description,
            applicationMethods = this.topics,
            keywords = this.topics
        )
    }
}