package com.aiteacher.data.local.dao

import androidx.room.*
import com.aiteacher.data.local.entity.TeachingPlanEntity
import kotlinx.coroutines.flow.Flow

/**
 * 教学计划数据访问对象
 */
@Dao
interface TeachingPlanDao {
    
    @Query("SELECT * FROM teaching_plans WHERE plan_id = :planId")
    suspend fun getTeachingPlanById(planId: String): TeachingPlanEntity?
    
    @Query("SELECT * FROM teaching_plans WHERE plan_id = :planId")
    fun getTeachingPlanByIdFlow(planId: String): Flow<TeachingPlanEntity?>
    
    @Query("SELECT * FROM teaching_plans WHERE student_id = :studentId ORDER BY created_at DESC")
    suspend fun getTeachingPlansByStudentId(studentId: String): List<TeachingPlanEntity>
    
    @Query("SELECT * FROM teaching_plans WHERE student_id = :studentId ORDER BY created_at DESC")
    fun getTeachingPlansByStudentIdFlow(studentId: String): Flow<List<TeachingPlanEntity>>
    
    @Query("SELECT * FROM teaching_plans WHERE status = :status ORDER BY created_at DESC")
    suspend fun getTeachingPlansByStatus(status: String): List<TeachingPlanEntity>
    
    @Query("SELECT * FROM teaching_plans ORDER BY created_at DESC")
    suspend fun getAllTeachingPlans(): List<TeachingPlanEntity>
    
    @Query("SELECT * FROM teaching_plans ORDER BY created_at DESC")
    fun getAllTeachingPlansFlow(): Flow<List<TeachingPlanEntity>>
    
    @Query("SELECT * FROM teaching_plans WHERE tags LIKE '%' || :tag || '%' ORDER BY created_at DESC")
    suspend fun getTeachingPlansByTag(tag: String): List<TeachingPlanEntity>
    
    @Query("SELECT * FROM teaching_plans ORDER BY created_at DESC LIMIT :limit")
    suspend fun getRecentTeachingPlans(limit: Int): List<TeachingPlanEntity>
    
    @Query("SELECT * FROM teaching_plans WHERE created_at BETWEEN :startDate AND :endDate ORDER BY created_at DESC")
    suspend fun getTeachingPlansByDateRange(startDate: Long, endDate: Long): List<TeachingPlanEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeachingPlan(plan: TeachingPlanEntity)
    
    @Update
    suspend fun updateTeachingPlan(plan: TeachingPlanEntity)
    
    @Delete
    suspend fun deleteTeachingPlan(plan: TeachingPlanEntity)
    
    @Query("DELETE FROM teaching_plans WHERE plan_id = :planId")
    suspend fun deleteTeachingPlanById(planId: String)
    
    @Query("DELETE FROM teaching_plans WHERE student_id = :studentId")
    suspend fun deleteTeachingPlansByStudentId(studentId: String)
}