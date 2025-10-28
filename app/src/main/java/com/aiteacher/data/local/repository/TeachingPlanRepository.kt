package com.aiteacher.data.local.repository

import com.aiteacher.data.local.dao.TeachingPlanDao
import com.aiteacher.data.local.entity.TeachingPlanEntity
import com.aiteacher.domain.model.TeachingPlan
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
    suspend fun getTeachingPlanById(planId: String): Result<TeachingPlan?> {
        return try {
            val plan = teachingPlanDao.getTeachingPlanById(planId)
            Result.success(plan?.toDomainModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据ID获取教学计划（Flow版本）
     */
    fun getTeachingPlanByIdFlow(planId: String): Flow<Result<TeachingPlan?>> {
        return teachingPlanDao.getTeachingPlanByIdFlow(planId).map { plan ->
            try {
                Result.success(plan?.toDomainModel())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 根据学生ID获取教学计划
     */
    suspend fun getTeachingPlansByStudentId(studentId: String): Result<List<TeachingPlan>> {
        return try {
            val plans = teachingPlanDao.getTeachingPlansByStudentId(studentId)
            Result.success(plans.map { it.toDomainModel() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据学生ID获取教学计划（Flow版本）
     */
    fun getTeachingPlansByStudentIdFlow(studentId: String): Flow<Result<List<TeachingPlan>>> {
        return teachingPlanDao.getTeachingPlansByStudentIdFlow(studentId).map { plans ->
            try {
                Result.success(plans.map { it.toDomainModel() })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 根据状态获取教学计划
     */
    suspend fun getTeachingPlansByStatus(status: String): Result<List<TeachingPlan>> {
        return try {
            val plans = teachingPlanDao.getTeachingPlansByStatus(status)
            Result.success(plans.map { it.toDomainModel() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取所有教学计划
     */
    suspend fun getAllTeachingPlans(): Result<List<TeachingPlan>> {
        return try {
            val plans = teachingPlanDao.getAllTeachingPlans()
            Result.success(plans.map { it.toDomainModel() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取所有教学计划（Flow版本）
     */
    fun getAllTeachingPlansFlow(): Flow<Result<List<TeachingPlan>>> {
        return teachingPlanDao.getAllTeachingPlansFlow().map { plans ->
            try {
                Result.success(plans.map { it.toDomainModel() })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 保存教学计划
     */
    suspend fun saveTeachingPlan(plan: TeachingPlan): Result<Unit> {
        return try {
            val entity = plan.toEntity()
            teachingPlanDao.insertTeachingPlan(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 更新教学计划
     */
    suspend fun updateTeachingPlan(plan: TeachingPlan): Result<Unit> {
        return try {
            val entity = plan.toEntity()
            teachingPlanDao.updateTeachingPlan(entity)
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
    suspend fun getTeachingPlansByTag(tag: String): Result<List<TeachingPlan>> {
        return try {
            val plans = teachingPlanDao.getTeachingPlansByTag(tag)
            Result.success(plans.map { it.toDomainModel() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取最近创建的教学计划
     */
    suspend fun getRecentTeachingPlans(limit: Int): Result<List<TeachingPlan>> {
        return try {
            val plans = teachingPlanDao.getRecentTeachingPlans(limit)
            Result.success(plans.map { it.toDomainModel() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 更新教学计划进度
     */
    suspend fun updateProgress(planId: String, totalTasks: Int, completedTasks: Int): Result<Unit> {
        return try {
            val plan = teachingPlanDao.getTeachingPlanById(planId)
            if (plan != null) {
                val updatedPlan = plan.copy(
                    totalTasks = totalTasks,
                    completedTasks = completedTasks,
                    updatedAt = System.currentTimeMillis()
                )
                teachingPlanDao.updateTeachingPlan(updatedPlan)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Teaching plan not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * 扩展函数：TeachingPlanEntity 转 TeachingPlan
 */
private fun TeachingPlanEntity.toDomainModel(): TeachingPlan {
    return TeachingPlan(
        planId = this.planId,
        studentId = this.studentId,
        title = this.title,
        description = this.description,
        subject = this.subject,
        gradeLevel = this.gradeLevel,
        totalDays = this.totalDays,
        startDate = this.startDate,
        endDate = this.endDate,
        status = this.status,
        totalTasks = this.totalTasks,
        completedTasks = this.completedTasks,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        tags = this.tags
    )
}

/**
 * 扩展函数：TeachingPlan 转 TeachingPlanEntity
 */
private fun TeachingPlan.toEntity(): TeachingPlanEntity {
    return TeachingPlanEntity(
        planId = this.planId,
        studentId = this.studentId,
        title = this.title,
        description = this.description,
        subject = this.subject,
        gradeLevel = this.gradeLevel,
        totalDays = this.totalDays,
        startDate = this.startDate,
        endDate = this.endDate,
        status = this.status,
        totalTasks = this.totalTasks,
        completedTasks = this.completedTasks,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        tags = this.tags
    )
}