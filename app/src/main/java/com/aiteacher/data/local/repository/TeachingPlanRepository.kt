package com.aiteacher.data.local.repository

import com.aiteacher.data.local.dao.TeachingPlanDao
import com.aiteacher.data.local.entity.TeachingPlanEntity
import kotlinx.coroutines.flow.Flow

/**
 * 教学计划数据仓库
 * 处理教学计划数据的业务逻辑
 */
class TeachingPlanRepository(private val teachingPlanDao: TeachingPlanDao) {
    
    /**
     * 根据ID获取教学计划
     */
    suspend fun getTeachingPlanById(planId: String): TeachingPlanEntity? {
        return teachingPlanDao.getTeachingPlanById(planId)
    }
    
    /**
     * 根据ID获取教学计划（Flow版本）
     */
    fun getTeachingPlanByIdFlow(planId: String): Flow<TeachingPlanEntity?> {
        return teachingPlanDao.getTeachingPlanByIdFlow(planId)
    }
    
    /**
     * 根据学生ID获取教学计划
     */
    suspend fun getTeachingPlansByStudentId(studentId: String): List<TeachingPlanEntity> {
        return teachingPlanDao.getTeachingPlansByStudentId(studentId)
    }
    
    /**
     * 根据学生ID获取教学计划（Flow版本）
     */
    fun getTeachingPlansByStudentIdFlow(studentId: String): Flow<List<TeachingPlanEntity>> {
        return teachingPlanDao.getTeachingPlansByStudentIdFlow(studentId)
    }
    
    /**
     * 根据状态获取教学计划
     */
    suspend fun getTeachingPlansByStatus(status: String): List<TeachingPlanEntity> {
        return teachingPlanDao.getTeachingPlansByStatus(status)
    }
    
    /**
     * 获取所有教学计划
     */
    suspend fun getAllTeachingPlans(): List<TeachingPlanEntity> {
        return teachingPlanDao.getAllTeachingPlans()
    }
    
    /**
     * 获取所有教学计划（Flow版本）
     */
    fun getAllTeachingPlansFlow(): Flow<List<TeachingPlanEntity>> {
        return teachingPlanDao.getAllTeachingPlansFlow()
    }
    
    /**
     * 保存教学计划
     */
    suspend fun saveTeachingPlan(plan: TeachingPlanEntity) {
        teachingPlanDao.insertTeachingPlan(plan)
    }
    
    /**
     * 更新教学计划
     */
    suspend fun updateTeachingPlan(plan: TeachingPlanEntity) {
        teachingPlanDao.updateTeachingPlan(plan)
    }
    
    /**
     * 删除教学计划
     */
    suspend fun deleteTeachingPlan(planId: String) {
        teachingPlanDao.deleteTeachingPlanById(planId)
    }
    
    /**
     * 根据学生ID删除教学计划
     */
    suspend fun deleteTeachingPlansByStudentId(studentId: String) {
        teachingPlanDao.deleteTeachingPlansByStudentId(studentId)
    }
}