package com.aiteacher.domain.repository

import com.aiteacher.domain.model.TeachingPlan

/**
 * 教学计划数据仓库接口
 */
interface TeachingPlanRepository {
    suspend fun getAllTeachingPlans(): List<TeachingPlan>
    suspend fun getTeachingPlanById(planId: String): TeachingPlan?
    suspend fun getTeachingPlansByStudentId(studentId: String): List<TeachingPlan>
    suspend fun insertTeachingPlan(teachingPlan: TeachingPlan)
    suspend fun updateTeachingPlan(teachingPlan: TeachingPlan)
    suspend fun deleteTeachingPlan(planId: String)
    suspend fun updateProgress(planId: String, totalTasks: Int, completedTasks: Int)
}