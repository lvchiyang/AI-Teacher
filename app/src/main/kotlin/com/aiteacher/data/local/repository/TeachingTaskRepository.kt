package com.aiteacher.data.local.repository

import com.aiteacher.data.local.dao.TeachingTaskDao
import com.aiteacher.data.local.entity.TeachingTaskEntity
import com.aiteacher.domain.model.TeachingTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 教学任务数据仓库
 * 处理教学任务数据的业务逻辑
 */
class TeachingTaskRepository(private val teachingTaskDao: TeachingTaskDao) {
    
    /**
     * 根据ID获取教学任务
     */
    suspend fun getTeachingTaskById(taskId: String): Result<TeachingTaskEntity?> {
        return try {
            val task = teachingTaskDao.getTeachingTaskById(taskId)
            Result.success(task)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据ID获取教学任务（Flow版本）
     */
    fun getTeachingTaskByIdFlow(taskId: String): Flow<Result<TeachingTaskEntity?>> {
        return teachingTaskDao.getTeachingTaskByIdFlow(taskId).map { task ->
            try {
                Result.success(task)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 根据计划ID获取教学任务列表
     */
    suspend fun getTeachingTasksByPlanId(planId: String): Result<List<TeachingTaskEntity>> {
        return try {
            val tasks = teachingTaskDao.getTeachingTasksByPlanId(planId)
            Result.success(tasks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据计划ID获取教学任务列表（Flow版本）
     */
    fun getTeachingTasksByPlanIdFlow(planId: String): Flow<Result<List<TeachingTaskEntity>>> {
        return teachingTaskDao.getTeachingTasksByPlanIdFlow(planId).map { tasks ->
            try {
                Result.success(tasks)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 根据计划ID和天数获取教学任务
     */
    suspend fun getTeachingTaskByPlanIdAndDay(planId: String, day: Int): Result<TeachingTaskEntity?> {
        return try {
            val task = teachingTaskDao.getTeachingTaskByPlanIdAndDay(planId, day)
            Result.success(task)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取计划中未完成的教学任务
     */
    suspend fun getIncompleteTeachingTasksByPlanId(planId: String): Result<List<TeachingTaskEntity>> {
        return try {
            val tasks = teachingTaskDao.getIncompleteTeachingTasksByPlanId(planId)
            Result.success(tasks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取计划中已完成的教学任务
     */
    suspend fun getCompletedTeachingTasksByPlanId(planId: String): Result<List<TeachingTaskEntity>> {
        return try {
            val tasks = teachingTaskDao.getCompletedTeachingTasksByPlanId(planId)
            Result.success(tasks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据完成状态获取教学任务
     */
    suspend fun getTeachingTasksByCompletionStatus(completed: Boolean): Result<List<TeachingTaskEntity>> {
        return try {
            val tasks = teachingTaskDao.getTeachingTasksByCompletionStatus(completed)
            Result.success(tasks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取所有教学任务
     */
    suspend fun getAllTeachingTasks(): Result<List<TeachingTaskEntity>> {
        return try {
            val tasks = teachingTaskDao.getAllTeachingTasks()
            Result.success(tasks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取所有教学任务（Flow版本）
     */
    fun getAllTeachingTasksFlow(): Flow<Result<List<TeachingTaskEntity>>> {
        return teachingTaskDao.getAllTeachingTasksFlow().map { tasks ->
            try {
                Result.success(tasks)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 保存教学任务
     */
    suspend fun saveTeachingTask(task: TeachingTaskEntity): Result<Unit> {
        return try {
            teachingTaskDao.insertTeachingTask(task)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 批量保存教学任务
     */
    suspend fun saveTeachingTasks(tasks: List<TeachingTaskEntity>): Result<Unit> {
        return try {
            teachingTaskDao.insertTeachingTasks(tasks)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 更新教学任务
     */
    suspend fun updateTeachingTask(task: TeachingTaskEntity): Result<Unit> {
        return try {
            teachingTaskDao.updateTeachingTask(task)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 删除教学任务
     */
    suspend fun deleteTeachingTask(taskId: String): Result<Unit> {
        return try {
            teachingTaskDao.deleteTeachingTaskById(taskId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据计划ID删除教学任务
     */
    suspend fun deleteTeachingTasksByPlanId(planId: String): Result<Unit> {
        return try {
            teachingTaskDao.deleteTeachingTasksByPlanId(planId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据日期范围获取教学任务
     */
    suspend fun getTeachingTasksByDateRange(startDate: String, endDate: String): Result<List<TeachingTaskEntity>> {
        return try {
            val tasks = teachingTaskDao.getTeachingTasksByDateRange(startDate, endDate)
            Result.success(tasks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取最近更新的教学任务
     */
    suspend fun getRecentTeachingTasks(limit: Int): Result<List<TeachingTaskEntity>> {
        return try {
            val tasks = teachingTaskDao.getRecentTeachingTasks(limit)
            Result.success(tasks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 标记任务为已完成
     */
    suspend fun markTaskAsCompleted(taskId: String, completionDate: String?): Result<Boolean> {
        return try {
            val task = teachingTaskDao.getTeachingTaskById(taskId)
            if (task != null) {
                val updatedTask = task.copy(
                    completed = true,
                    completionDate = completionDate,
                    updatedAt = System.currentTimeMillis()
                )
                teachingTaskDao.updateTeachingTask(updatedTask)
                Result.success(true)
            } else {
                Result.success(false)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * 扩展函数：TeachingTaskEntity 转 TeachingTask
 */
private fun TeachingTaskEntity.toDomainModel(): TeachingTask {
    return TeachingTask(
        taskId = this.taskId,
        planId = this.planId,
        day = this.day,
        date = this.date,
        title = this.title,
        description = this.description,
        topics = this.topics,
        relatedKnowledge = this.relatedKnowledge,
        estimatedTime = this.estimatedTime,
        content = this.content,
        resources = this.resources,
        completed = this.completed,
        completionDate = this.completionDate,
        grade = this.grade,
        maxGrade = this.maxGrade,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

/**
 * 扩展函数：TeachingTask 转 TeachingTaskEntity
 */
private fun TeachingTask.toEntity(): TeachingTaskEntity {
    return TeachingTaskEntity(
        taskId = this.taskId,
        planId = this.planId,
        day = this.day,
        date = this.date,
        title = this.title,
        description = this.description,
        topics = this.topics,
        relatedKnowledge = this.relatedKnowledge,
        estimatedTime = this.estimatedTime,
        content = this.content,
        resources = this.resources,
        completed = this.completed,
        completionDate = this.completionDate,
        grade = this.grade,
        maxGrade = this.maxGrade,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}
