package com.aiteacher.data.local.repository

import com.aiteacher.data.local.dao.TeachingPlanDao
import com.aiteacher.data.local.entity.TeachingPlanEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 教学计划数据仓库
 * 处理教学计划数据的业务逻辑
 */
class TeachingPlanRepository(private val teachingPlanDao: TeachingPlanDao) {
    
    /**
     * 根据ID获取教学计划
     */
    suspend fun getTeachingPlanById(planId: String): Result<TeachingPlanEntity?> {
        return try {
            val plan = teachingPlanDao.getTeachingPlanById(planId)
            Result.success(plan)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据ID获取教学计划（Flow版本）
     */
    fun getTeachingPlanByIdFlow(planId: String): Flow<Result<TeachingPlanEntity?>> {
        return teachingPlanDao.getTeachingPlanByIdFlow(planId).map { plan ->
            try {
                Result.success(plan)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 根据学生ID获取教学计划
     */
    suspend fun getTeachingPlansByStudentId(studentId: String): Result<List<TeachingPlanEntity>> {
        return try {
            val plans = teachingPlanDao.getTeachingPlansByStudentId(studentId)
            Result.success(plans)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据学生ID获取教学计划（Flow版本）
     */
    fun getTeachingPlansByStudentIdFlow(studentId: String): Flow<Result<List<TeachingPlanEntity>>> {
        return teachingPlanDao.getTeachingPlansByStudentIdFlow(studentId).map { plans ->
            try {
                Result.success(plans)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 根据状态获取教学计划
     */
    suspend fun getTeachingPlansByStatus(status: String): Result<List<TeachingPlanEntity>> {
        return try {
            val plans = teachingPlanDao.getTeachingPlansByStatus(status)
            Result.success(plans)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取所有教学计划
     */
    suspend fun getAllTeachingPlans(): Result<List<TeachingPlanEntity>> {
        return try {
            val plans = teachingPlanDao.getAllTeachingPlans()
            Result.success(plans)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取所有教学计划（Flow版本）
     */
    fun getAllTeachingPlansFlow(): Flow<Result<List<TeachingPlanEntity>>> {
        return teachingPlanDao.getAllTeachingPlansFlow().map { plans ->
            try {
                Result.success(plans)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 保存教学计划
     */
    suspend fun saveTeachingPlan(plan: TeachingPlanEntity): Result<Unit> {
        return try {
            teachingPlanDao.insertTeachingPlan(plan)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 更新教学计划
     */
    suspend fun updateTeachingPlan(plan: TeachingPlanEntity): Result<Unit> {
        return try {
            teachingPlanDao.updateTeachingPlan(plan)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 删除教学计划
     */
    suspend fun deleteTeachingPlan(planId: String): Result<Unit> {
        return try {
            teachingPlanDao.deleteTeachingPlanById(planId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据学生ID删除教学计划
     */
    suspend fun deleteTeachingPlansByStudentId(studentId: String): Result<Unit> {
        return try {
            teachingPlanDao.deleteTeachingPlansByStudentId(studentId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据标签获取教学计划
     */
    suspend fun getTeachingPlansByTag(tag: String): Result<List<TeachingPlanEntity>> {
        return try {
            val plans = teachingPlanDao.getTeachingPlansByTag(tag)
            Result.success(plans)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取最近创建的教学计划
     */
    suspend fun getRecentTeachingPlans(limit: Int): Result<List<TeachingPlanEntity>> {
        return try {
            val plans = teachingPlanDao.getRecentTeachingPlans(limit)
            Result.success(plans)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}