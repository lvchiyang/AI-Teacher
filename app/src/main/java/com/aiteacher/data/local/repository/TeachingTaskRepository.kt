package com.aiteacher.data.local.repository

import com.aiteacher.data.local.dao.TeachingTaskDao
import com.aiteacher.data.local.entity.TeachingTaskEntity
import kotlinx.coroutines.flow.Flow

/**
 * 教学任务数据仓库
 * 处理教学任务数据的业务逻辑
 */
class TeachingTaskRepository(private val teachingTaskDao: TeachingTaskDao) {
    
    /**
     * 根据ID获取教学任务
     */
    suspend fun getTeachingTaskById(taskId: String): TeachingTaskEntity? {
        return teachingTaskDao.getTeachingTaskById(taskId)
    }
    
    /**
     * 根据ID获取教学任务（Flow版本）
     */
    fun getTeachingTaskByIdFlow(taskId: String): Flow<TeachingTaskEntity?> {
        return teachingTaskDao.getTeachingTaskByIdFlow(taskId)
    }
    
    /**
     * 根据计划ID获取教学任务列表
     */
    suspend fun getTeachingTasksByPlanId(planId: String): List<TeachingTaskEntity> {
        return teachingTaskDao.getTeachingTasksByPlanId(planId)
    }
    
    /**
     * 根据计划ID获取教学任务列表（Flow版本）
     */
    fun getTeachingTasksByPlanIdFlow(planId: String): Flow<List<TeachingTaskEntity>> {
        return teachingTaskDao.getTeachingTasksByPlanIdFlow(planId)
    }
    
    /**
     * 根据计划ID和天数获取教学任务
     */
    suspend fun getTeachingTaskByPlanIdAndDay(planId: String, day: Int): TeachingTaskEntity? {
        return teachingTaskDao.getTeachingTaskByPlanIdAndDay(planId, day)
    }
    
    /**
     * 获取计划中未完成的教学任务
     */
    suspend fun getIncompleteTeachingTasksByPlanId(planId: String): List<TeachingTaskEntity> {
        return teachingTaskDao.getIncompleteTeachingTasksByPlanId(planId)
    }
    
    /**
     * 获取计划中已完成的教学任务
     */
    suspend fun getCompletedTeachingTasksByPlanId(planId: String): List<TeachingTaskEntity> {
        return teachingTaskDao.getCompletedTeachingTasksByPlanId(planId)
    }
    
    /**
     * 根据完成状态获取教学任务
     */
    suspend fun getTeachingTasksByCompletionStatus(completed: Boolean): List<TeachingTaskEntity> {
        return teachingTaskDao.getTeachingTasksByCompletionStatus(completed)
    }
    
    /**
     * 获取所有教学任务
     */
    suspend fun getAllTeachingTasks(): List<TeachingTaskEntity> {
        return teachingTaskDao.getAllTeachingTasks()
    }
    
    /**
     * 获取所有教学任务（Flow版本）
     */
    fun getAllTeachingTasksFlow(): Flow<List<TeachingTaskEntity>> {
        return teachingTaskDao.getAllTeachingTasksFlow()
    }
    
    /**
     * 保存教学任务
     */
    suspend fun saveTeachingTask(task: TeachingTaskEntity) {
        teachingTaskDao.insertTeachingTask(task)
    }
    
    /**
     * 批量保存教学任务
     */
    suspend fun saveTeachingTasks(tasks: List<TeachingTaskEntity>) {
        teachingTaskDao.insertTeachingTasks(tasks)
    }
    
    /**
     * 更新教学任务
     */
    suspend fun updateTeachingTask(task: TeachingTaskEntity) {
        teachingTaskDao.updateTeachingTask(task)
    }
    
    /**
     * 删除教学任务
     */
    suspend fun deleteTeachingTask(taskId: String) {
        teachingTaskDao.deleteTeachingTaskById(taskId)
    }
    
    /**
     * 根据计划ID删除教学任务
     */
    suspend fun deleteTeachingTasksByPlanId(planId: String) {
        teachingTaskDao.deleteTeachingTasksByPlanId(planId)
    }
}