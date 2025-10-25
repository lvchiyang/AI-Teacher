package com.aiteacher.domain.repository

import com.aiteacher.domain.model.TeachingTask

/**
 * 教学任务数据仓库接口
 */
interface TeachingTaskRepository {
    suspend fun getAllTeachingTasks(): List<TeachingTask>
    suspend fun getTeachingTaskById(taskId: String): TeachingTask?
    suspend fun getTeachingTasksByPlanId(planId: String): List<TeachingTask>
    suspend fun getTeachingTasksByPlanIdAndDay(planId: String, day: Int): List<TeachingTask>
    suspend fun insertTeachingTask(teachingTask: TeachingTask)
    suspend fun updateTeachingTask(teachingTask: TeachingTask)
    suspend fun deleteTeachingTask(taskId: String)
    suspend fun markTaskAsCompleted(taskId: String, completionDate: String?): Boolean
}