package com.aiteacher.data.local.dao

import androidx.room.*
import com.aiteacher.data.local.entity.TeachingPlanEntity
import kotlinx.coroutines.flow.Flow

/**
 * 教学计划数据访问对象
 */
@Dao
interface TeachingPlanDao {
    
    @Query("SELECT * FROM teaching_plans WHERE planId = :planId")
    suspend fun getTeachingPlanById(planId: String): TeachingPlanEntity?
    
    @Query("SELECT * FROM teaching_plans WHERE planId = :planId")
    fun getTeachingPlanByIdFlow(planId: String): Flow<TeachingPlanEntity?>
    
    @Query("SELECT * FROM teaching_plans WHERE studentId = :studentId ORDER BY createdAt DESC")
    suspend fun getTeachingPlansByStudentId(studentId: String): List<TeachingPlanEntity>
    
    @Query("SELECT * FROM teaching_plans WHERE studentId = :studentId ORDER BY createdAt DESC")
    fun getTeachingPlansByStudentIdFlow(studentId: String): Flow<List<TeachingPlanEntity>>
    
    @Query("SELECT * FROM teaching_plans WHERE status = :status ORDER BY createdAt DESC")
    suspend fun getTeachingPlansByStatus(status: String): List<TeachingPlanEntity>
    
    @Query("SELECT * FROM teaching_plans ORDER BY createdAt DESC")
    suspend fun getAllTeachingPlans(): List<TeachingPlanEntity>
    
    @Query("SELECT * FROM teaching_plans ORDER BY createdAt DESC")
    fun getAllTeachingPlansFlow(): Flow<List<TeachingPlanEntity>>
    
    @Query("SELECT * FROM teaching_plans WHERE tags LIKE '%' || :tag || '%' ORDER BY createdAt DESC")
    suspend fun getTeachingPlansByTag(tag: String): List<TeachingPlanEntity>
    
    @Query("SELECT * FROM teaching_plans ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentTeachingPlans(limit: Int): List<TeachingPlanEntity>
    
    @Query("SELECT * FROM teaching_plans WHERE createdAt BETWEEN :startDate AND :endDate ORDER BY createdAt DESC")
    suspend fun getTeachingPlansByDateRange(startDate: Long, endDate: Long): List<TeachingPlanEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeachingPlan(plan: TeachingPlanEntity)
    
    @Update
    suspend fun updateTeachingPlan(plan: TeachingPlanEntity)
    
    @Delete
    suspend fun deleteTeachingPlan(plan: TeachingPlanEntity)
    
    @Query("DELETE FROM teaching_plans WHERE planId = :planId")
    suspend fun deleteTeachingPlanById(planId: String)
    
    @Query("DELETE FROM teaching_plans WHERE studentId = :studentId")
    suspend fun deleteTeachingPlansByStudentId(studentId: String)
}