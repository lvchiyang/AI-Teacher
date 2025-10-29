package com.aiteacher.data.local.dao

import androidx.room.*
import com.aiteacher.data.local.entity.TestingTaskEntity
import kotlinx.coroutines.flow.Flow

/**
 * 测试任务数据访问对象
 */
@Dao
interface TestingTaskDao {
    
    @Query("SELECT * FROM testing_tasks WHERE task_id = :taskId")
    suspend fun getTestingTaskById(taskId: String): TestingTaskEntity?
    
    @Query("SELECT * FROM testing_tasks WHERE task_id = :taskId")
    fun getTestingTaskByIdFlow(taskId: String): Flow<TestingTaskEntity?>
    
    @Query("SELECT * FROM testing_tasks WHERE student_id = :studentId ORDER BY created_at DESC")
    suspend fun getTestingTasksByStudentId(studentId: String): List<TestingTaskEntity>
    
    @Query("SELECT * FROM testing_tasks WHERE student_id = :studentId ORDER BY created_at DESC")
    fun getTestingTasksByStudentIdFlow(studentId: String): Flow<List<TestingTaskEntity>>
    
    @Query("SELECT * FROM testing_tasks WHERE student_id = :studentId AND completed = 0 ORDER BY created_at DESC")
    suspend fun getIncompleteTestingTasksByStudentId(studentId: String): List<TestingTaskEntity>
    
    @Query("SELECT * FROM testing_tasks WHERE student_id = :studentId AND completed = 1 ORDER BY created_at DESC")
    suspend fun getCompletedTestingTasksByStudentId(studentId: String): List<TestingTaskEntity>
    
    @Query("SELECT * FROM testing_tasks WHERE completed = :completed ORDER BY created_at DESC")
    suspend fun getTestingTasksByCompletionStatus(completed: Boolean): List<TestingTaskEntity>
    
    @Query("SELECT * FROM testing_tasks ORDER BY created_at DESC")
    suspend fun getAllTestingTasks(): List<TestingTaskEntity>
    
    @Query("SELECT * FROM testing_tasks ORDER BY created_at DESC")
    fun getAllTestingTasksFlow(): Flow<List<TestingTaskEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestingTask(task: TestingTaskEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestingTasks(tasks: List<TestingTaskEntity>)
    
    @Update
    suspend fun updateTestingTask(task: TestingTaskEntity)
    
    @Delete
    suspend fun deleteTestingTask(task: TestingTaskEntity)
    
    @Query("DELETE FROM testing_tasks WHERE task_id = :taskId")
    suspend fun deleteTestingTaskById(taskId: String)
    
    @Query("DELETE FROM testing_tasks WHERE student_id = :studentId")
    suspend fun deleteTestingTasksByStudentId(studentId: String)
}