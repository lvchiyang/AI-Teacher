package com.aiteacher.data.local.dao

import androidx.room.*
import com.aiteacher.data.local.entity.TeachingTaskEntity
import kotlinx.coroutines.flow.Flow

/**
 * 教学任务数据访问对象
 */
@Dao
interface TeachingTaskDao {
    
    @Query("SELECT * FROM teaching_tasks WHERE task_id = :taskId")
    suspend fun getTeachingTaskById(taskId: String): TeachingTaskEntity?
    
    @Query("SELECT * FROM teaching_tasks WHERE task_id = :taskId")
    fun getTeachingTaskByIdFlow(taskId: String): Flow<TeachingTaskEntity?>
    
    @Query("SELECT * FROM teaching_tasks WHERE plan_id = :planId ORDER BY day ASC")
    suspend fun getTeachingTasksByPlanId(planId: String): List<TeachingTaskEntity>
    
    @Query("SELECT * FROM teaching_tasks WHERE plan_id = :planId ORDER BY day ASC")
    fun getTeachingTasksByPlanIdFlow(planId: String): Flow<List<TeachingTaskEntity>>
    
    @Query("SELECT * FROM teaching_tasks WHERE plan_id = :planId AND day = :day")
    suspend fun getTeachingTaskByPlanIdAndDay(planId: String, day: Int): TeachingTaskEntity?
    
    @Query("SELECT * FROM teaching_tasks WHERE plan_id = :planId AND completed = 0 ORDER BY day ASC")
    suspend fun getIncompleteTeachingTasksByPlanId(planId: String): List<TeachingTaskEntity>
    
    @Query("SELECT * FROM teaching_tasks WHERE plan_id = :planId AND completed = 1 ORDER BY day ASC")
    suspend fun getCompletedTeachingTasksByPlanId(planId: String): List<TeachingTaskEntity>
    
    @Query("SELECT * FROM teaching_tasks WHERE completed = :completed ORDER BY created_at DESC")
    suspend fun getTeachingTasksByCompletionStatus(completed: Boolean): List<TeachingTaskEntity>
    
    @Query("SELECT * FROM teaching_tasks ORDER BY created_at DESC")
    suspend fun getAllTeachingTasks(): List<TeachingTaskEntity>
    
    @Query("SELECT * FROM teaching_tasks ORDER BY created_at DESC")
    fun getAllTeachingTasksFlow(): Flow<List<TeachingTaskEntity>>
    
    @Query("SELECT * FROM teaching_tasks WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getTeachingTasksByDateRange(startDate: String, endDate: String): List<TeachingTaskEntity>
    
    @Query("SELECT * FROM teaching_tasks ORDER BY created_at DESC LIMIT :limit")
    suspend fun getRecentTeachingTasks(limit: Int): List<TeachingTaskEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeachingTask(task: TeachingTaskEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeachingTasks(tasks: List<TeachingTaskEntity>)
    
    @Update
    suspend fun updateTeachingTask(task: TeachingTaskEntity)
    
    @Delete
    suspend fun deleteTeachingTask(task: TeachingTaskEntity)
    
    @Query("DELETE FROM teaching_tasks WHERE task_id = :taskId")
    suspend fun deleteTeachingTaskById(taskId: String)
    
    @Query("DELETE FROM teaching_tasks WHERE plan_id = :planId")
    suspend fun deleteTeachingTasksByPlanId(planId: String)
}